package com.nageoffer.shortlink.admin.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nageoffer.shortlink.admin.common.convention.result.Result;
import com.nageoffer.shortlink.admin.handler.ShortLinkBlockHandler;
import com.nageoffer.shortlink.admin.remote.Service.ShortLinkActualRemoteService;
import com.nageoffer.shortlink.admin.remote.dto.Req.ShortLinkCreateReqDTO;
import com.nageoffer.shortlink.admin.remote.dto.Req.ShortLinkPageReqDTO;
import com.nageoffer.shortlink.admin.remote.dto.Req.ShortLinkUpdateReqDTO;
import com.nageoffer.shortlink.admin.remote.dto.Resp.ShortLinkCreateRespDTO;
import com.nageoffer.shortlink.admin.remote.dto.Resp.ShortLinkPageRespDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ShortLinkController {
    /**
     * 分页查询短链接
     * @param requestParam
     * @return
     */

    private final ShortLinkActualRemoteService shortLinkActualRemoteService;
    @GetMapping("/api/short-link/admin/v1/page")
    public Result<Page<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO requestParam){
        return shortLinkActualRemoteService.pageShortLink(requestParam);
    }

    /**
     * 创建短链接
     * @param requestParam
     * @return
     */
    @SentinelResource(
            value = "short-link-create",
            blockHandlerClass = ShortLinkBlockHandler.class,
            fallbackClass = ShortLinkBlockHandler.class
    )
    @PostMapping("/api/short-link/admin/v1/link")
    public Result<ShortLinkCreateRespDTO> create(
            @RequestHeader("username") String username,
            @RequestBody ShortLinkCreateReqDTO requestParam
    ){
        return shortLinkActualRemoteService.createShortLink(requestParam);
    }

    /**
     * 修改短链接
     * @param requestParam 修改请求
     * @return 响应结果
     */
    @PutMapping("/api/short-link/admin/v1/link")
    public Result<Void> update(@RequestBody ShortLinkUpdateReqDTO requestParam){
        return shortLinkActualRemoteService.updateShortLink(requestParam);
    }
}
