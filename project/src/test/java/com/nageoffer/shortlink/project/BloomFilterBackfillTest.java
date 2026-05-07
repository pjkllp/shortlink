package com.nageoffer.shortlink.project;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.nageoffer.shortlink.project.dao.entity.ShortLinkDO;
import com.nageoffer.shortlink.project.dao.mapper.ShortLinkMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.redisson.Redisson;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * 手动执行：将 t_link 中未删除 shortUri 回填到指定 Redis BloomFilter。
 */
@Slf4j
@SpringBootTest(classes = ShortLinkApplication.class)
public class BloomFilterBackfillTest {

    private static final String REDIS_ADDRESS = "redis://47.93.214.40:6379";
    private static final String REDIS_PASSWORD = "123456";
    private static final String BLOOM_FILTER_KEY = "shortLinkCreateCachePenetrationBloomFilter";
    private static final int PAGE_SIZE = 1000;

    @Autowired
    private ShortLinkMapper shortLinkMapper;

    @Test
    void backfillBloomFilterFromTLink() {
        Config config = new Config();
        config.useSingleServer()
    .setAddress(REDIS_ADDRESS)
    .setPassword(REDIS_PASSWORD)
    .setDatabase(0)
    .setConnectTimeout(10000)
    .setTimeout(10000)
    .setRetryAttempts(5)
    .setRetryInterval(2000)
    .setConnectionMinimumIdleSize(1)
    .setConnectionPoolSize(2);

        RedissonClient redissonClient = Redisson.create(config);
        try {
            RBloomFilter<String> bloomFilter = redissonClient.getBloomFilter(BLOOM_FILTER_KEY);
            // 与生产配置保持一致：预计容量和误判率
            bloomFilter.tryInit(1000000L, 0.001);

            long lastId = 0L;
            long total = 0L;
            while (true) {
                List<ShortLinkDO> rows = shortLinkMapper.selectList(
                        Wrappers.lambdaQuery(ShortLinkDO.class)
                                .orderByAsc(ShortLinkDO::getId)
                                .last("LIMIT " + PAGE_SIZE)
                );
                if (rows == null || rows.isEmpty()) {
                    break;
                }
                for (ShortLinkDO row : rows) {
                    if (row.getShortUri() != null && !row.getShortUri().isBlank()) {
                        bloomFilter.add(row.getShortUri());
                        total++;
                    }
                }
                lastId = rows.get(rows.size() - 1).getId();
                log.info("BloomFilter 回填进行中，lastId={}, 本批次={}，累计={}", lastId, rows.size(), total);
            }

            log.info("BloomFilter 回填完成，累计写入 shortUri 数量={}", total);
        } finally {
            redissonClient.shutdown();
        }
    }

    void test(){

    }
}

