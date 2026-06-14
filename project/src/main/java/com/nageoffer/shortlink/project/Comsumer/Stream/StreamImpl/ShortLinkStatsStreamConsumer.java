package com.nageoffer.shortlink.project.Comsumer.Stream.StreamImpl;

import com.nageoffer.shortlink.project.Comsumer.Stream.AbstractStreamConsumer;
import com.nageoffer.shortlink.project.Comsumer.doMessage;
import com.nageoffer.shortlink.project.dao.entity.DlqMessageDO;
import com.nageoffer.shortlink.project.dao.mapper.DlqMessageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Redis Stream 统计消息消费者（新增，不替代 RocketMQ）
 */
@Slf4j
@Service
public class ShortLinkStatsStreamConsumer extends AbstractStreamConsumer {

    public static final String STATS_STREAM_KEY = "short-link-stats-stream";
    public static final String STATS_STREAM_GROUP = "short-link-stats-stream-group";
    public static final String STATS_STREAM_CONSUMER_PREFIX = "short-link-stats-stream-consumer-";

    public ShortLinkStatsStreamConsumer(StringRedisTemplate stringRedisTemplate, doMessage doMessage, DlqMessageMapper dlqMessageMapper) {
        super(stringRedisTemplate, doMessage, dlqMessageMapper);
    }

    @PostConstruct
    public void initGroup() {
        try {
            stringRedisTemplate.opsForStream().createGroup(STATS_STREAM_KEY, STATS_STREAM_GROUP);
            log.info("初始化Redis Stream消费组成功，stream:{} group:{}", STATS_STREAM_KEY, STATS_STREAM_GROUP);
        } catch (Exception ex) {
            log.info("初始化创建流失败，可能是Redis Stream消费组已存在或暂不可创建，stream:{} group:{}", STATS_STREAM_KEY, STATS_STREAM_GROUP);
        }
    }

    @Scheduled(fixedDelay = 3000)
    @Override
    public void consumeStream() {
        if (!consuming.compareAndSet(false, true)) {
            return;
        }
        try {
            CompletableFuture<?>[] futures = new CompletableFuture<?>[WORKER_COUNT];
            for (int i = 0; i < WORKER_COUNT; i++) {
                final String consumerName = STATS_STREAM_CONSUMER_PREFIX + i;
                futures[i] = CompletableFuture.runAsync(() -> consume(consumerName)
                        , streamConsumerPool);
            }
            // 等待所有worker完成（异常不中断其他任务）
            CompletableFuture.allOf(futures).exceptionally(ex -> {
                log.error("Redis Stream workers执行异常", ex);
                return null;
            }).join();
        } finally {
            consuming.set(false);
        }
    }


    public void consume(String consumerName) {
        List<MapRecord<String, Object, Object>> records=null;
        try {
            records = getRecords(STATS_STREAM_GROUP, consumerName, STATS_STREAM_KEY);
        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("NOGROUP") && msg.contains("XREADGROUP")) {
                log.warn("{}流可能被缓存策略删除，有可能丢失数据，正在重建流...",STATS_STREAM_KEY);
                // 这是流/消费组不存在
                rebuildGroupAndRetry(STATS_STREAM_KEY,STATS_STREAM_GROUP);
                try {
                    // Re-read immediately to reduce post-rebuild idle window
                    records = getRecords(STATS_STREAM_GROUP, consumerName, STATS_STREAM_KEY);
                } catch (Exception retryEx) {
                    log.error("重建后重新读取统计流失败，consumer:{}", consumerName, retryEx);
                    return;
                }
            }
            else {
                log.error("读取统计流失败，consumer:{}", consumerName, e);
                return;
            }
        }
        if (records == null || records.isEmpty()) {
            return;
        }

        for (MapRecord<String, Object, Object> record : records) {
            Map<Object, Object> values = record.getValue();
            String payload =(String) values.get("payload");
            if (payload == null || payload.isBlank()) {
                acknowledge(record);
                continue;
            }
            try {
                doMessage.onMessage(payload);
                acknowledge(record);
            } catch (Exception ex) {
                log.error("消费Redis Stream统计消息失败，consumer:{} recordId:{}", consumerName, record.getId(), ex);
                archiveStreamFailedMessage(record, payload, ex);
                // 失败后直接归档并ack，防止长期卡在Pending
                acknowledge(record);
            }
        }
    }

    //进入数据库归档，后期可进行人工投放
    private void archiveStreamFailedMessage(MapRecord<String, Object, Object> record, String payload, Exception ex) {
        try {
            String errMsg = ex == null ? "UNKNOWN" : ex.getMessage();
            if (errMsg == null || errMsg.isBlank()) {
                errMsg =ex.getClass().getSimpleName();
            }
            if (errMsg.length() > 1000) {
                errMsg = errMsg.substring(0, 1000);
            }
            DlqMessageDO dlqMessageDO = DlqMessageDO.builder()
                    .eventId("STREAM-" + record.getId().getValue())
                    .dlqTopic("REDIS_STREAM:" + STATS_STREAM_KEY)
                    .messageStr(payload)
                    .consumerGroup(STATS_STREAM_GROUP)
                    .errorType(ex == null ? "UNKNOWN" : ex.getClass().getSimpleName())
                    .errorMessage(errMsg)
                    .status(0)
                    .retryCount(0)
                    .createTime(new Date())
                    .updateTime(new Date())
                    .build();
            dlqMessageMapper.insert(dlqMessageDO);
        } catch (Exception archiveEx) {
            log.error("Redis Stream失败消息归档失败，recordId:{}", record.getId(), archiveEx);
        }
    }

    private void acknowledge(MapRecord<String,Object,Object> record){
        acknowledge(STATS_STREAM_KEY,STATS_STREAM_GROUP,record);
    }
}
