package com.nageoffer.shortlink.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nageoffer.shortlink.project.dao.entity.LinkLocaleStatsDO;

public interface LinkLocalStatsService extends IService<LinkLocaleStatsDO> {

    /**
     * 记录地区访问量
     * @param linkLocaleStatsDO 地区访问实体
     */
    void shortLinkLocalStats(LinkLocaleStatsDO linkLocaleStatsDO);
}
