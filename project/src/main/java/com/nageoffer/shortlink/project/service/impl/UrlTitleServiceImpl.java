package com.nageoffer.shortlink.project.service.impl;

import cn.hutool.core.util.StrUtil;
import com.nageoffer.shortlink.project.service.UrlTitleService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

/**
 * 通过原链接获取标题
 */
@Service
public class UrlTitleServiceImpl implements UrlTitleService {

    @Override
    public String getTile(String url) {
        // 1. 基础校验
        if (StrUtil.isBlank(url)) {
            return "URL不能为空";
        }

        // 2. 修复 URL（如果缺少 http/https 头）
        if (!url.startsWith("http")) {
            url = "https://" + url;
        }

        try {
            // 3. 使用 Jsoup 直接解析（最推荐，自带编码自动检测）
            // 设置超时时间 5秒，避免请求卡死
            Document doc = Jsoup.connect(url)
                    .timeout(5000)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36") // 模拟浏览器
                    .get();

            String title = doc.title();
            return StrUtil.isBlank(title) ? "无标题" : title;

        } catch (Exception e) {
            return "获取失败";
        }
    }
}
