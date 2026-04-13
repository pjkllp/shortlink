package com.nageoffer.shortlink.project.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nageoffer.shortlink.project.common.convention.result.Result;
import com.nageoffer.shortlink.project.dao.entity.AccessLogDO;
import com.nageoffer.shortlink.project.dto.Req.AccessLogPageReqDTO;
import com.nageoffer.shortlink.project.service.MonitorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/short-link/project/v1/monitor")
@RequiredArgsConstructor
public class MonitorController {

    private final MonitorService monitorService;

    @GetMapping("/page/access_log")
    public Result<Page<AccessLogDO>> page(AccessLogPageReqDTO requestParam) {
        return Result.success(monitorService.pageAccessLog(requestParam));
    }


}
