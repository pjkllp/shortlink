package com.nageoffer.shortlink.project.config;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nageoffer.shortlink.project.dao.entity.ShortLinkDO;
import com.nageoffer.shortlink.project.dao.mapper.ShortLinkMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class InitializationWork {

    private final RBloomFilter<String> shortLinkCreateCachePenetrationBloomFilter;

    private final ShortLinkMapper shortLinkMapper;

    @PostConstruct
    public void loadShortLinkToRBloom() {
        //如果布隆过滤器已有数据，直接跳过预加载
        long existingCount = shortLinkCreateCachePenetrationBloomFilter.count();
        Long count = shortLinkMapper.selectCount(null);
        if (existingCount > count) {
            log.info("布隆过滤器已有 {} 条数据，跳过预加载", existingCount);
            return;
        }

        log.info("开始预加载短链接到Redis布隆过滤器...");
        long startTime = System.currentTimeMillis();

        //异步执行，不阻塞Spring应用启动
        CompletableFuture.runAsync(() -> {
            int pageSize = 1000;
            long currentPage = 1;
            long totalCount = 0;

            while (true) {
                // 分页查询
                Page<ShortLinkDO> page = new Page<>(currentPage, pageSize);
                Page<ShortLinkDO> resultPage = shortLinkMapper.selectPage(page, null);

                if (resultPage.getRecords().isEmpty()) {
                    break;
                }

                // 提取短链接码列表
                List<String> shortUriList = resultPage.getRecords().stream()
                        .map(ShortLinkDO::getShortUri)
                        .toList();

                //批量添加
                shortLinkCreateCachePenetrationBloomFilter.add(shortUriList);

                totalCount += shortUriList.size();
                currentPage++;

                log.info("已加载 {} 条短链接到布隆过滤器", totalCount);
            }

            long endTime = System.currentTimeMillis();
            log.info("短链接预加载完成，共加载 {} 条，耗时 {}ms", totalCount, endTime - startTime);
        }).exceptionally(ex -> {
            log.error("短链接预加载失败", ex);
            return null;
        });
    }
}
