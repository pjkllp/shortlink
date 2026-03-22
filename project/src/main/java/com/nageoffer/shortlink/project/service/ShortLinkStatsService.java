package com.nageoffer.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nageoffer.shortlink.project.common.convention.result.Result;
import com.nageoffer.shortlink.project.dto.Req.ShortLinkStatsAccessRecordReqDTO;
import com.nageoffer.shortlink.project.dto.Req.ShortLinkStatsReqDTO;
import com.nageoffer.shortlink.project.dto.Resp.ShortLinkStatsAccessRecordRespDTO;
import com.nageoffer.shortlink.project.dto.Resp.ShortLinkStatsRespDTO;

/**
 * 短链接监控接口层
 */
public interface ShortLinkStatsService {

    /**
     * 获取单个短链接监控数据
     *
     * @param requestParam 获取短链接监控数据入参
     * @return 短链接监控数据
     */
    ShortLinkStatsRespDTO oneShortLinkStats(ShortLinkStatsReqDTO requestParam);

    Page<ShortLinkStatsAccessRecordRespDTO> statusAccessRecord(ShortLinkStatsAccessRecordReqDTO requestParam);
}