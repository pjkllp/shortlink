package com.nageoffer.shortlink.project.Handler;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.nageoffer.shortlink.project.common.convention.result.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class ShortLinkBlockHandler {
    // 限流兜底：静态方法 + 参数匹配 + BlockException
    public static void blockHandlerRestoreUrl(  String shortUri,
                                         HttpServletRequest request,
                                         HttpServletResponse response,
                                         BlockException ex) throws IOException {
        response.sendRedirect("/page/busy");
    }

    // 熔断降级兜底：静态方法 + 参数匹配 + Throwable
    public static void restoreUrlFallback( String shortUri,
                                         HttpServletRequest request,
                                         HttpServletResponse response,
                                         Throwable ex) throws IOException {
        response.sendRedirect("/page/busy");
    }

}
