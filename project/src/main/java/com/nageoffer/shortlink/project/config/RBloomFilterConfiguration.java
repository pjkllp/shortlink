package com.nageoffer.shortlink.project.config;

import com.nageoffer.shortlink.project.dao.mapper.ShortLinkMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class RBloomFilterConfiguration {

    private final ShortLinkMapper shortLinkMapper;

    @Bean
    public RBloomFilter<String> shortLinkCreateCachePenetrationBloomFilter(RedissonClient redissonClient) {
        RBloomFilter<String> bloomFilter = redissonClient.getBloomFilter("shortLinkCreateCachePenetrationBloomFilter");
        boolean initSuccess = bloomFilter.tryInit(1000000L, 0.001);
        if (initSuccess) {
            log.info("布隆过滤器初始化成功，预期容量：1000000，误判率：0.001");
        } else {
            log.info("布隆过滤器已存在，跳过初始化");
        }
        return bloomFilter;
    }
}