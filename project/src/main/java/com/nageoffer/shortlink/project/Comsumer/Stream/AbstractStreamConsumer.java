package com.nageoffer.shortlink.project.Comsumer.Stream;

import com.nageoffer.shortlink.project.Comsumer.doMessage;
import com.nageoffer.shortlink.project.dao.mapper.DlqMessageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.Map;
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
    protected void rebuildGroupAndRetry(String stream,String group){
        try {
            // Ensure stream exists before creating group
            String marker = "rebuild-" + System.currentTimeMillis();
            RecordId markerId = stringRedisTemplate.opsForStream().add(
                    StreamRecords.string(Map.of("marker", marker)).withStreamKey(stream)
            );
            // Delete marker entry only, avoid trimming real stream messages
            if (markerId != null) {
                stringRedisTemplate.opsForStream().delete(stream, markerId);
            }
        } catch (Exception ex) {
            log.warn("重建时初始化stream失败，stream:{}", stream, ex);
        }
        try {
            stringRedisTemplate.opsForStream().createGroup(stream,group);
            log.info("{}流和消费者{}创建成功",stream,group);
        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("BUSYGROUP")) {
                log.debug("{}消费者组{}已存在，跳过重建", stream, group);
                return;
            }
            log.error("重建消费者组失败，stream:{} group:{}", stream, group, e);
        }
    }
}
