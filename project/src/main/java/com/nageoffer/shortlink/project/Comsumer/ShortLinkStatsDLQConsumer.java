package com.nageoffer.shortlink.project.Comsumer;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.nageoffer.shortlink.project.dao.entity.DlqMessageDO;
import com.nageoffer.shortlink.project.dao.mapper.DlqMessageMapper;
import com.nageoffer.shortlink.project.dto.ShortLinkStatsMessageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.Date;

/**
 * 短链接统计死信消费者：仅归档与告警，不再执行主业务落库。
 */
@Slf4j
@Service
@RequiredArgsConstructor
@RocketMQMessageListener(
        topic = "%DLQ%short-link-stats-consumer-group",
        consumerGroup = "short-link-stats-DLQ-consumer-group",
        consumeThreadNumber = 4,
        consumeThreadMax = 8
)
public class ShortLinkStatsDLQConsumer implements RocketMQListener<String> {

    private final DlqMessageMapper dlqMessageMapper;

    @Override
    public void onMessage(String messageStr) {
        try {
            String jsonStr = decodePayload(messageStr);
            ShortLinkStatsMessageDTO msg = JSON.parseObject(jsonStr, ShortLinkStatsMessageDTO.class);
            String eventId = StringUtils.isNotBlank(msg.getEventId()) ? msg.getEventId() : "UNKNOWN-" + System.currentTimeMillis();
            insertDlqMessage(eventId, messageStr, "CONSUME_RETRY_EXHAUSTED", "主消费重试耗尽，已转入DLQ");
            log.warn("DLQ消息归档成功，eventId={}", eventId);
        } catch (DuplicateKeyException duplicateKeyException) {
            log.warn("DLQ消息重复归档已忽略，message={}", messageStr);
        } catch (Exception e) {
            // DLQ链路不再抛异常，防止继续套娃
            log.error("DLQ消息归档失败，message={}", messageStr, e);
        }
    }

    private String decodePayload(String messageStr) {
        String cleanMsg = messageStr.replaceAll("^\"|\"$", "");
        try {
            byte[] decodeBytes = Base64.getDecoder().decode(cleanMsg);
            return new String(decodeBytes);
        } catch (IllegalArgumentException e) {
            return messageStr;
        }
    }

    private void insertDlqMessage(String eventId, String messageStr, String errorType, String errorMessage) {
        String safeErrMsg = errorMessage;
        if (safeErrMsg != null && safeErrMsg.length() > 1000) {
            safeErrMsg = safeErrMsg.substring(0, 1000);
        }
        DlqMessageDO dlqMessageDO = DlqMessageDO.builder()
                .dlqTopic("%DLQ%short-link-stats-consumer-group")
                .consumerGroup("short-link-stats-consumer-group")
                .eventId(eventId)
                .messageStr(messageStr)
                .errorType(errorType)
                .createTime(new Date())
                .errorMessage(safeErrMsg)
                .status(0)
                .retryCount(0)
                .updateTime(new Date())
                .build();
        dlqMessageMapper.insert(dlqMessageDO);
    }
}