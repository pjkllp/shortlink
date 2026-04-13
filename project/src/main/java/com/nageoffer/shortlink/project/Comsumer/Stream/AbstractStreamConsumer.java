package com.nageoffer.shortlink.project.Comsumer.Stream;

import com.nageoffer.shortlink.project.Comsumer.doMessage;
import com.nageoffer.shortlink.project.dao.mapper.DlqMessageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractStreamConsumer implements StreamConsumer {

    public final StringRedisTemplate stringRedisTemplate;
    protected static final int WORKER_COUNT = 4;
    protected static final int BATCH_SIZE = 20;
    protected final doMessage doMessage;
    protected final DlqMessageMapper dlqMessageMapper;
    protected final AtomicBoolean consuming = new AtomicBoolean(false);


    @Override
    @SuppressWarnings("unchecked")
    public List<MapRecord<String,Object,Object>> getRecords(String group,String consumerName,String stream){
        return stringRedisTemplate.opsForStream().read(
                Consumer.from(group, consumerName),
                StreamReadOptions.empty().count(BATCH_SIZE).block(Duration.ofMillis(500)),
                StreamOffset.create(stream, ReadOffset.lastConsumed())
        );
    }

    protected void acknowledge(String stream,String group,MapRecord<String, Object, Object> record) {
        try {
            stringRedisTemplate.opsForStream().acknowledge(
                    stream,
                    group,
                    record.getId()
            );
        } catch (Exception e) {
            log.warn("ACK失败, recordId: {}", record.getId(), e);
        }
    }
}
