package com.nageoffer.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nageoffer.shortlink.project.dao.entity.LinkBrowserStatsDO;

public interface LinkBrowserStatsMapper extends BaseMapper<LinkBrowserStatsDO> {
    void shortLinkBrowserStats(LinkBrowserStatsDO linkBrowserStatsDO);
}
