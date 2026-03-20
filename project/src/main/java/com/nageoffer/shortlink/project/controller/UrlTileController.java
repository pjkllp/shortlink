package com.nageoffer.shortlink.project.controller;

import cn.hutool.core.util.StrUtil;
import com.nageoffer.shortlink.project.common.convention.result.Result;
import com.nageoffer.shortlink.project.service.UrlTitleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * URL 标题获取控制器
 */
@RestController
@RequiredArgsConstructor
public class UrlTileController {

    private final UrlTitleService urlTitleService;

    /**
     * 获取网站标题
     * @param url 目标URL
     * @return 网站标题
     */
    @GetMapping("/api/short-link/project/v1/title")
    public Result<String> getUrlTitle(@RequestParam("url") String url) {
        String tile = urlTitleService.getTile(url);
        return Result.success("获取短链接成功",tile);
    }
}