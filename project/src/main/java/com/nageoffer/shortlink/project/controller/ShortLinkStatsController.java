package com.nageoffer.shortlink.project.controller;

import com.nageoffer.shortlink.project.common.convention.result.Result;
import com.nageoffer.shortlink.project.dto.Req.ShortLinkStatsReqDTO;
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
    @GetMapping("/api/short-link/v1/stats")
    public Result<ShortLinkStatsRespDTO> shortLinkStats(ShortLinkStatsReqDTO requestParam){
        ShortLinkStatsRespDTO shortLinkStatsRespDTO = shortLinkStatsService.oneShortLinkStats(requestParam);
        return Result.success(shortLinkStatsRespDTO);
    }
}
