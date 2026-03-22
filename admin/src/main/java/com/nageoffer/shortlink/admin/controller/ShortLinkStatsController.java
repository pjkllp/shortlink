package com.nageoffer.shortlink.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nageoffer.shortlink.admin.common.convention.result.Result;
import com.nageoffer.shortlink.admin.remote.Service.ShortLinkActualRemoteService;
import com.nageoffer.shortlink.admin.remote.dto.Req.ShortLinkStatsAccessRecordReqDTO;
import com.nageoffer.shortlink.admin.remote.dto.Req.ShortLinkStatsReqDTO;
import com.nageoffer.shortlink.admin.remote.dto.Resp.ShortLinkStatsAccessRecordRespDTO;
import com.nageoffer.shortlink.admin.remote.dto.Resp.ShortLinkStatsRespDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ShortLinkStatsController {

    private final ShortLinkActualRemoteService shortLinkActualRemoteService;

    @GetMapping("/api/short-link/admin/v1/statsAccessRecord")
    public Result<Page<ShortLinkStatsAccessRecordRespDTO>> statsAccessRecord(ShortLinkStatsAccessRecordReqDTO requestParam){
        return shortLinkActualRemoteService.statsAccessRecord(requestParam);
    }

    @GetMapping("/api/short-link/admin/v1/stats")
    public Result<ShortLinkStatsRespDTO> stats(ShortLinkStatsReqDTO requestParam){
        return shortLinkActualRemoteService.stats(requestParam);
    }
}
