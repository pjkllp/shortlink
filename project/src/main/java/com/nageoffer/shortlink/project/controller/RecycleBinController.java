package com.nageoffer.shortlink.project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nageoffer.shortlink.project.common.convention.result.Result;
import com.nageoffer.shortlink.project.dto.Req.RecycleBinPageReqDTO;
import com.nageoffer.shortlink.project.dto.Req.RecycleBinRecoverReqDTO;
import com.nageoffer.shortlink.project.dto.Req.RecycleBinRemoveReqDTO;
import com.nageoffer.shortlink.project.dto.Req.RecycleBinSaveReqDTO;
import com.nageoffer.shortlink.project.dto.Resp.RecycleBinPageRespDTO;
import com.nageoffer.shortlink.project.service.RecycleBinService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 回收站管理控制层
 */
@RequiredArgsConstructor
@RestController
public class RecycleBinController {

    private final RecycleBinService recycleBinService;

    /**
     * 将短链接移至回收站
     */
    @PostMapping("/api/short-link/project/v1/recycle-bin/save")
    public Result<Void> saveRecycleBin(@RequestBody RecycleBinSaveReqDTO requestParam) throws InterruptedException {
        recycleBinService.recycleBinSave(requestParam);
        return Result.success("添加回收站成功");
    }

    /**
     * 分页查询短链接
     */
    @GetMapping("/api/short-link/project/v1/recycle-bin/page")
    public Result<Page<RecycleBinPageRespDTO>> pageRecycleBin(RecycleBinPageReqDTO requestParam){
        return Result.success(recycleBinService.recycleBinPage(requestParam));
    }

    /**
     * 移除回收站
     */
    @PostMapping("/api/short-link/project/v1/recycle-bin/recover")
    public Result<Void> recover(@RequestBody RecycleBinRecoverReqDTO requestParam){
        recycleBinService.recycleBinRecover(requestParam);
        return Result.success("移除成功");
    }

    @PostMapping("/api/short-link/project/v1/recycle-bin/remove")
    public Result<Void> remove(@RequestBody RecycleBinRemoveReqDTO requestParam){
        recycleBinService.recycleBinRemove(requestParam);
        return Result.success("删除成功");
    }
}
