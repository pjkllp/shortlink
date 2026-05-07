package com.nageoffer.shortlink.project.Comsumer.MQ.MQImpl;

import com.nageoffer.shortlink.project.Comsumer.doMessage;
import lombok.RequiredArgsConstructor;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * RocketMQ 监听适配器：仅在开启 MQ 时注册，具体处理委托给 ShortLinkStatsConsumer。
 */
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "stats.mq", name = "enabled", havingValue = "true", matchIfMissing = true)
@RocketMQMessageListener(
        topic = "short-link-stats-topic",
        consumerGroup = "short-link-stats-consumer-group",
        consumeThreadNumber = 4,
        consumeThreadMax = 8
)
public class ShortLinkStatsMQConsumer implements RocketMQListener<String> {

    private final doMessage doMessage;

    @Override
    public void onMessage(String message) {
        doMessage.onMessage(message);
    }
}

