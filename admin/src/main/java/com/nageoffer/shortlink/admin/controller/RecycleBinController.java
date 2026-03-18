package com.nageoffer.shortlink.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nageoffer.shortlink.admin.common.convention.result.Result;
import com.nageoffer.shortlink.admin.dto.req.RecycleBinSaveReqDTO;
import com.nageoffer.shortlink.admin.remote.dto.Req.RecycleBinPageReqDTO;
import com.nageoffer.shortlink.admin.remote.dto.Req.RecycleBinRecoverReqDTO;
import com.nageoffer.shortlink.admin.remote.dto.Req.RecycleBinRemoveReqDTO;
import com.nageoffer.shortlink.admin.remote.dto.Resp.RecycleBinPageRespDTO;
import com.nageoffer.shortlink.admin.remote.dto.Service.ShortLinkRemoteService;
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
    private final ShortLinkRemoteService shortLinkRemoteService;
    @PostMapping("/api/short-link/admin/v1/recycle-bin/save")
    public Result<Void> saveRecycleBin(@RequestBody RecycleBinSaveReqDTO requestParam) throws InterruptedException {
        return shortLinkRemoteService.recycleBinSave(requestParam);
    }

    @PostMapping("/api/short-link/admin/v1/recycle-bin/page")
    public Result<IPage<RecycleBinPageRespDTO>> recycleBinPage(@RequestBody RecycleBinPageReqDTO requestParam){
        return shortLinkRemoteService.recycleBinPage(requestParam);
    }

    @PostMapping("/api/short-link/admin/v1/recycle-bin/recover")
    public Result<Void> recycleBinRecover(@RequestBody RecycleBinRecoverReqDTO requestParam){
        return shortLinkRemoteService.recycleBinRecover(requestParam);
    }

    @PostMapping("/api/short-link/admin/v1/recycle-bin/remove")
    public Result<Void> recycleBinRemove(@RequestBody RecycleBinRemoveReqDTO requestParam){
        return shortLinkRemoteService.recycleBinRemove(requestParam);
    }
}
