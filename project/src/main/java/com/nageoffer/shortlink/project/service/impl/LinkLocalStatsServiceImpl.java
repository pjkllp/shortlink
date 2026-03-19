package com.nageoffer.shortlink.project.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nageoffer.shortlink.project.dao.entity.LinkLocaleStatsDO;
import com.nageoffer.shortlink.project.dao.mapper.LinkLocaleStatsMapper;
import com.nageoffer.shortlink.project.service.LinkLocalStatsService;
import org.springframework.stereotype.Service;

@Service
public class LinkLocalStatsServiceImpl extends ServiceImpl<LinkLocaleStatsMapper, LinkLocaleStatsDO> implements LinkLocalStatsService {
    @Override
    public void shortLinkLocalStats(LinkLocaleStatsDO linkLocaleStatsDO) {

    }
}
