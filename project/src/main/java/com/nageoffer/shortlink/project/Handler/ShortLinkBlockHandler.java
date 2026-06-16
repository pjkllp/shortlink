package com.nageoffer.shortlink.project.Handler;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ShortLinkBlockHandler {

    // 改为静态变量
    private static String protocol;
    private static String domain;

    @Value("${short-link.protocol}")
    public void setProtocol(String protocol) {
        ShortLinkBlockHandler.protocol = protocol;
    }

    @Value("${short-link.domain.default}")
    public void setDomain(String domain) {
        ShortLinkBlockHandler.domain = domain;
    }

    // 限流兜底
    public static void blockHandlerRestoreUrl(String shortUri,
                                       HttpServletRequest request,
                                       HttpServletResponse response,
                                       BlockException ex) throws IOException, InterruptedException {
        // 拼接完整公网地址，不再是相对路径
        String fullUrl = protocol + "://" + domain + "/page/busy";
        response.sendRedirect(fullUrl);
    }

    // 熔断兜底同步修改
    public static void restoreUrlFallback(String shortUri,
                                   HttpServletRequest request,
                                   HttpServletResponse response,
                                   Throwable ex) throws IOException, InterruptedException {
        String fullUrl = protocol + "://" + domain + "/page/busy";
        response.sendRedirect(fullUrl);
    }
}