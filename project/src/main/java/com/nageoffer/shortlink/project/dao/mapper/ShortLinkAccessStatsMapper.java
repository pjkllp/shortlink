package com.nageoffer.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nageoffer.shortlink.project.dao.entity.ShortLinkAccessStatsDO;

public interface ShortLinkAccessStatsMapper extends BaseMapper<ShortLinkAccessStatsDO> {
    void shortLinkStats(ShortLinkAccessStatsDO linkAccessStatsDO);
}
