package com.nageoffer.shortlink.project.config;

import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RBloomFilterConfiguration {

    @Bean
    public RBloomFilter<String> shortLinkCreateCachePenetrationBloomFilter(RedissonClient redissonClient){
        RBloomFilter<String> cachePenetrationBloomFilter=redissonClient.getBloomFilter("shortLinkCreateCachePenetrationBloomFilter");
        //预计存多少数据，误判率
        cachePenetrationBloomFilter.tryInit(100000000L,0.001);
        return cachePenetrationBloomFilter;
    }
}
