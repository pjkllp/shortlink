package com.nageoffer.shortlink.project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nageoffer.shortlink.project.common.convention.result.Result;
import com.nageoffer.shortlink.project.dto.Req.ShortLinkGroupStatsAccessRecordReqDTO;
import com.nageoffer.shortlink.project.dto.Req.ShortLinkStatsAccessRecordReqDTO;
import com.nageoffer.shortlink.project.dto.Req.ShortLinkStatsReqDTO;
import com.nageoffer.shortlink.project.dto.Resp.ShortLinkStatsAccessRecordRespDTO;
import com.nageoffer.shortlink.project.dto.Resp.ShortLinkStatsRespDTO;
import com.nageoffer.shortlink.project.service.ShortLinkStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 短链接监控控制层
 */
@RestController
@RequiredArgsConstructor
public class ShortLinkStatsController {

    private final ShortLinkStatsService shortLinkStatsService;

    /**
     * 访问单个短链接指定时间内监控数据
     * @param requestParam
     * @return
     */
    @GetMapping("/api/short-link/project/v1/stats")
    public Result<ShortLinkStatsRespDTO> shortLinkStats(ShortLinkStatsReqDTO requestParam){
        ShortLinkStatsRespDTO shortLinkStatsRespDTO = shortLinkStatsService.oneShortLinkStats(requestParam);
        return Result.success(shortLinkStatsRespDTO);
    }

    /**
     * 统计短链接访问记录
     * @param requestParam 分页入参
     * @return 分页返回
     */
    @GetMapping("/api/short-link/project/v1/statsAccessRecord")
    public Result<IPage<ShortLinkStatsAccessRecordRespDTO>> statsAccessRecord(ShortLinkStatsAccessRecordReqDTO requestParam){
        return Result.success(shortLinkStatsService.statusAccessRecord(requestParam));
    }

}
