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


}

