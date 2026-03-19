package com.nageoffer.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nageoffer.shortlink.project.dao.entity.LinkOsStatsDO;

public interface LinkOsStatsMapper extends BaseMapper<LinkOsStatsDO> {

    void shortLinkOsStats(LinkOsStatsDO linkOsStatsDO);
}
