package com.nageoffer.shortlink.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nageoffer.shortlink.project.dao.entity.LinkLocalStatsDO;

public interface LinkLocalStatsService extends IService<LinkLocalStatsDO> {

    /**
     * 记录地区访问量
     * @param linkLocalStatsDO 地区访问实体
     */
    void shortLinkLocalStats(LinkLocalStatsDO linkLocalStatsDO);
}
