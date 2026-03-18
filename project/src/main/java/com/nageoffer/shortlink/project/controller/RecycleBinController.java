package com.nageoffer.shortlink.project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nageoffer.shortlink.project.common.convention.result.Result;
import com.nageoffer.shortlink.project.dto.Req.RecycleBinPageReqDTO;
import com.nageoffer.shortlink.project.dto.Req.RecycleBinSaveReqDTO;
import com.nageoffer.shortlink.project.dto.Resp.RecycleBinPageRespDTO;
import com.nageoffer.shortlink.project.service.RecycleBinService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 回收站管理控制层
 */
@RequiredArgsConstructor
@RestController
public class RecycleBinController {

    private final RecycleBinService recycleBinService;
    @PostMapping("/api/short-link/v1/recycle-bin/save")
    public Result<Void> saveRecycleBin(@RequestBody RecycleBinSaveReqDTO requestParam) throws InterruptedException {
        recycleBinService.recycleBinSave(requestParam);
        return Result.success("添加回收站成功");
    }

    @PostMapping("/api/short-link/v1/recycle-bin/page")
    public Result<IPage<RecycleBinPageRespDTO>> pageRecycleBin(@RequestBody RecycleBinPageReqDTO requestParam){
        return Result.success(recycleBinService.recycleBinPage(requestParam));
    }
}
