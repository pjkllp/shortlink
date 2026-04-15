package com.nageoffer.shortlink.project.config;

import com.nageoffer.shortlink.project.service.ScheduledService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.sql.Date;
import java.time.Instant;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Configuration
@EnableScheduling
public class DynamicTrimSchedulerConfig implements SchedulingConfigurer {

    private final ScheduledService scheduledService;
    private final StringRedisTemplate stringRedisTemplate;

    private final String QPS_ZSET_KEY="qps_zset_key";
    private final RedisScript<Long> redisScript = RedisScript.of(
            new ClassPathResource("/lua/judgeTrimStreamRate.lua"),
            Long.class);

    @Override
    public void configureTasks(@NonNull ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.addTriggerTask(
                this::runTrimStream,
                triggerContext -> {
                    Instant instant = triggerContext.lastCompletion();
                    Long qps = completionNextTime();
                    log.info("qps:{}",qps);
                    Long maybeRate = 5000L;
                    if (qps != null) {
                        if (qps < 10) {
                            maybeRate = 10000L;
                            log.info("trim为10s每次");
                        } else if (qps < 20) {
                            maybeRate = 8000L;
                            log.info("trim为8s每次");
                        } else {
                            maybeRate = 5000L;
                            log.info("trim为5s每次");
                        }
                    } else {
                        log.warn("qps统计为空，trim默认5s每次");
                    }
                    long actualRate = maybeRate==null?5000:
                            maybeRate>=5000?maybeRate:5000;
                    if (instant != null) {
                        return Date.from(instant.plusMillis(actualRate)).toInstant();
                    }
                    return Date.from(Instant.now().plusMillis(actualRate)).toInstant();
                }
        );
    }

    private void runTrimStream(){
        scheduledService.trimRedisStreams();
    }
    private Long completionNextTime(){
        return stringRedisTemplate.execute(
                redisScript,
                List.of(QPS_ZSET_KEY)
        );
    }
}
