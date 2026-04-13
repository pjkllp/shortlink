package com.nageoffer.shortlink.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.nageoffer.shortlink.project.Comsumer.doMessage;
import com.nageoffer.shortlink.project.dao.entity.*;
import com.nageoffer.shortlink.project.dao.mapper.*;
import com.nageoffer.shortlink.project.service.ScheduledService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class ScheduledServiceImpl implements ScheduledService {

    private static final String ACCESS_LOG_STREAM_KEY = "access_log_stream:";
    private static final String STATS_STREAM_KEY = "short-link-stats-stream";
    private static final long ACCESS_LOG_STREAM_MAX_LEN = 50000L;
    private static final long STATS_STREAM_MAX_LEN = 10000L;

    private final DlqMessageMapper dlqMessageMapper;
    private final doMessage doMessage;
    private final StringRedisTemplate stringRedisTemplate;

    @Scheduled(fixedDelay = 30000)
    @Override
    @Transactional(rollbackFor = Exception.class,propagation = Propagation.NOT_SUPPORTED)
    public void replayFailedMessages() {
        LambdaQueryWrapper<DlqMessageDO> queryWrapper = Wrappers.lambdaQuery(DlqMessageDO.class)
                .eq(DlqMessageDO::getStatus, 0)
                .le(DlqMessageDO::getRetryCount, 2);
        List<DlqMessageDO> dlqMessageDOS = dlqMessageMapper.selectList(queryWrapper);
        for(DlqMessageDO item:dlqMessageDOS){
            try {
                doMessage.onMessage(item.getMessageStr());
                LambdaUpdateWrapper<DlqMessageDO> updateWrapper = Wrappers.lambdaUpdate(DlqMessageDO.class)
                        .eq(DlqMessageDO::getDlqTopic, item.getDlqTopic())
                        .eq(DlqMessageDO::getEventId, item.getEventId())
                        .set(true,DlqMessageDO::getStatus,1)
                        .setSql("retry_count = retry_count + 1");
                dlqMessageMapper.update(null,updateWrapper);
            } catch (Exception e) {
                LambdaUpdateWrapper<DlqMessageDO> updateWrapper = Wrappers.lambdaUpdate(DlqMessageDO.class)
                        .eq(DlqMessageDO::getEventId, item.getEventId())
                        .eq(DlqMessageDO::getDlqTopic, item.getDlqTopic());
                int retry = item.getRetryCount() + 1;
                DlqMessageDO dlqMessageDO = DlqMessageDO.builder()
                        .retryCount(retry)
                        .errorMessage(e.getMessage()!=null&&e.getMessage().length()>1000? e.getMessage().substring(0,1000): e.getMessage())
                        .status(retry >= 3 ? 2 : 0)
                        .build();
                dlqMessageMapper.update(dlqMessageDO,updateWrapper);
                log.error("消息重放处理失败，原因：{}",e.getMessage());
            }
        }
    }

    /**
     * 定期裁剪 Redis Stream，防止消息无限增长挤占内存。
     */
    @Scheduled(fixedDelay = 300000)
    public void trimRedisStreams() {
        try {
            Long accessSize = trimStreamApprox(ACCESS_LOG_STREAM_KEY, ACCESS_LOG_STREAM_MAX_LEN);
            Long statsSize = trimStreamApprox(STATS_STREAM_KEY, STATS_STREAM_MAX_LEN);
            log.info("Redis Stream trim完成，{}当前长度:{}，{}当前长度:{}",
                    ACCESS_LOG_STREAM_KEY, accessSize, STATS_STREAM_KEY, statsSize);
        } catch (Exception e) {
            log.error("Redis Stream trim失败，原因：{}", e.getMessage(), e);
        }
    }

    /**
     * 执行 XTRIM key MAXLEN ~ N（近似裁剪）.
     */
    private Long trimStreamApprox(String streamKey, long maxLen) {
        return stringRedisTemplate.execute((RedisConnection connection) -> {
            Object result = connection.execute(
                    "XTRIM",
                    streamKey.getBytes(StandardCharsets.UTF_8),
                    "MAXLEN".getBytes(StandardCharsets.UTF_8),
                    "~".getBytes(StandardCharsets.UTF_8),
                    String.valueOf(maxLen).getBytes(StandardCharsets.UTF_8)
            );
            if (result instanceof Long) {
                return (Long) result;
            }
            return null;
        });
    }


}
