package com.nageoffer.shortlink.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nageoffer.shortlink.admin.common.convention.result.Result;
import com.nageoffer.shortlink.admin.remote.dto.Req.ShortLinkCreateReqDTO;
import com.nageoffer.shortlink.admin.remote.dto.Req.ShortLinkPageReqDTO;
import com.nageoffer.shortlink.admin.remote.dto.Resp.ShortLinkPageRespDTO;
import com.nageoffer.shortlink.admin.remote.dto.ShortLinkRemoteService;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ShortLinkController {
    /**
     * 分页查询短链接
     * @param requestParam
     * @return
     */
    @PostMapping("/api/short-link/admin/v1/page")
    public String pageShortLink(@RequestBody ShortLinkPageReqDTO requestParam){
        ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {
        };
        return shortLinkRemoteService.pageShortLink(requestParam);
    }

    @PostMapping("/api/short-link/admin/v1/link")
    public String create(@RequestBody ShortLinkCreateReqDTO requestParam){
        ShortLinkRemoteService shortLinkRemoteService=new ShortLinkRemoteService() {
        };
        return shortLinkRemoteService.createShortLink(requestParam);
    }
}
