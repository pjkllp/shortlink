package com.nageoffer.shortlink.project.config;

import com.alibaba.fastjson.support.spring.messaging.MappingFastJsonMessageConverter;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;

@Configuration
public class RocketMQConfig {

    @Value("${rocketmq.name-server}")
    private String nameServer;

    @Value("${rocketmq.producer.group}")
    private String producerGroup;

    @Bean
    public RocketMQTemplate rocketMQTemplate() throws Exception {
        // 创建生产者实例
        DefaultMQProducer producer = new DefaultMQProducer(producerGroup);
        // 设置 NameServer 地址
        producer.setNamesrvAddr(nameServer);
        // 创建 RocketMQTemplate 并设置生产者
        RocketMQTemplate rocketMQTemplate = new RocketMQTemplate();
        rocketMQTemplate.setMessageConverter(new MappingFastJsonMessageConverter());
        rocketMQTemplate.setProducer(producer);
        return rocketMQTemplate;
    }
}