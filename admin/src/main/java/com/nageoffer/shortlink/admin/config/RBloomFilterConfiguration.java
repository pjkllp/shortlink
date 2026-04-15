package com.nageoffer.shortlink.admin.config;

import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RBloomFilterConfiguration {

    @Bean
    public RBloomFilter<String> userRegisterCachePenetrationBloomFilter(RedissonClient redissonClient){
        RBloomFilter<String> cachePenetrationBloomFilter=redissonClient.getBloomFilter("userRegisterCachePenetrationBloomFilter");
        //预计存多少数据，误判率
        cachePenetrationBloomFilter.tryInit(1000000L,0.001);
        return cachePenetrationBloomFilter;
    }
}
