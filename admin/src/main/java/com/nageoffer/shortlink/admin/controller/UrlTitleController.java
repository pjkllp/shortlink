package com.nageoffer.shortlink.admin.controller;

import com.nageoffer.shortlink.admin.common.convention.result.Result;
import com.nageoffer.shortlink.admin.remote.Service.ShortLinkActualRemoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 获取指定网站标题控制层
 */
@RequiredArgsConstructor
@RestController
public class UrlTitleController {
    private final ShortLinkActualRemoteService shortLinkActualRemoteService;

    @GetMapping("/api/short-link/admin/title")
    public Result<String> getTitle(@RequestParam("url") String url){
        return shortLinkActualRemoteService.getTitle(url);
    }
}
