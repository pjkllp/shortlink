package com.nageoffer.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nageoffer.shortlink.project.dao.entity.ShortLinkAccessStatsDO;
import io.lettuce.core.dynamic.annotation.Param;

public interface ShortLinkAccessStatsDOMapper extends BaseMapper<ShortLinkAccessStatsDO> {
    void shortLinkStats(ShortLinkAccessStatsDO linkAccessStatsDO);
}
