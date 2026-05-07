package com.nageoffer.shortlink.project.Comsumer.Stream.StreamImpl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.nageoffer.shortlink.project.Comsumer.Stream.AbstractStreamConsumer;
import com.nageoffer.shortlink.project.Comsumer.doMessage;
import com.nageoffer.shortlink.project.dao.entity.AccessLogDO;
import com.nageoffer.shortlink.project.dao.mapper.AccessLogMapper;
import com.nageoffer.shortlink.project.dao.mapper.DlqMessageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.Record;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 独立访问日志 Redis Stream 消费者
 * 从 ShortLinkStatsStreamConsumer 中迁移而来并修复 bug
 */
@Slf4j
@Service
public class AccessLogStreamConsumer extends AbstractStreamConsumer {

    private static final String ACCESS_LOG_STREAM = "access_log_stream:";
    private static final String ACCESS_LOG_STREAM_GROUP = "access_log_group";
    private static final String CONSUMER_PREFIX = "access_log_consumer-";

    private final AccessLogMapper accessLogMapper;

    private final AtomicBoolean consuming = new AtomicBoolean(false);

    public AccessLogStreamConsumer(StringRedisTemplate stringRedisTemplate
            , doMessage doMessage
            , DlqMessageMapper dlqMessageMapper
            ,AccessLogMapper accessLogMapper) {
        super(stringRedisTemplate, doMessage, dlqMessageMapper);
        this.accessLogMapper=accessLogMapper;
    }

    @PostConstruct
    public void initGroup() {
        try {
            stringRedisTemplate.opsForStream().createGroup(ACCESS_LOG_STREAM, ACCESS_LOG_STREAM_GROUP);
            log.info("访问日志 Redis Stream 消费组创建成功");
        } catch (Exception ex) {
            log.info("访问日志 Redis Stream 消费组已存在或不可创建");
        }
    }

    @Scheduled(fixedDelay = 1000)
    @Override
    public void consumeStream() {
        if (!consuming.compareAndSet(false, true)) {
            return;
        }
        try {
            CompletableFuture<?>[] futures=new CompletableFuture[WORKER_COUNT];
            for (int i = 0; i < WORKER_COUNT; i++) {
                String consumerName = CONSUMER_PREFIX + i;
                futures[i]=CompletableFuture.runAsync(()->consume(consumerName)
                        ,streamConsumerPool);
            }
            CompletableFuture.allOf(futures).exceptionally(
                    e->{
                        log.error("access stream 消费失败",e);
                        return null;
                    }).join();
        } finally {
            consuming.set(false);
        }
    }

    public void consume(String consumerName) {
        List<MapRecord<String, Object, Object>> records=null;
        try {
            records = getRecords(ACCESS_LOG_STREAM_GROUP, consumerName, ACCESS_LOG_STREAM);
        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("NOGROUP") && msg.contains("XREADGROUP")) {
                log.warn("{}流可能被缓存策略删除，有可能丢失数据，正在重建流...",ACCESS_LOG_STREAM);
                // 这是流/消费组不存在
                rebuildGroupAndRetry(ACCESS_LOG_STREAM,ACCESS_LOG_STREAM_GROUP);
                try {
                    // Re-read immediately to reduce post-rebuild idle window
                    records = getRecords(ACCESS_LOG_STREAM_GROUP, consumerName, ACCESS_LOG_STREAM);
                } catch (Exception retryEx) {
                    log.error("重建后重新读取访问日志流失败，consumer:{}", consumerName, retryEx);
                    return;
                }
            }
            else {
                log.error("读取访问日志流失败，consumer:{}", consumerName, e);
                return;
            }
        }

        if (records == null || records.isEmpty()) {
            return;
        }

        for (MapRecord<String, Object, Object> record : records) {
            Map<Object, Object> values = record.getValue();
            String payload = (String) values.get("payload");

            if (StrUtil.isBlank(payload)) {
                acknowledge(record);
                continue;
            }

            try {
                doMessage.recordAccessLog(payload);
                acknowledge(record);
                log.debug("访问日志消费成功");
            } catch (Exception e) {
                log.error("消费访问日志失败, payload: {}", payload, e);
                acknowledge(record); // 失败也ACK，避免消息长期卡在Pending队列
            }
        }
    }

    private void acknowledge(MapRecord<String,Object,Object> record){
        acknowledge(ACCESS_LOG_STREAM,ACCESS_LOG_STREAM_GROUP,record);
    }


}
