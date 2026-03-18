package com.nageoffer.shortlink.project.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nageoffer.shortlink.project.dao.entity.LinkLocalStatsDO;
import com.nageoffer.shortlink.project.dao.mapper.LinkLocalStatsMapper;
import com.nageoffer.shortlink.project.service.LinkLocalStatsService;
import org.springframework.stereotype.Service;

@Service
public class LinkLocalStatsServiceImpl extends ServiceImpl<LinkLocalStatsMapper,LinkLocalStatsDO> implements LinkLocalStatsService {
    @Override
    public void shortLinkLocalStats(LinkLocalStatsDO linkLocalStatsDO) {

    }
}
