package com.nageoffer.shortlink.project.Handler;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.nageoffer.shortlink.project.common.convention.result.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ShortLinkBlockHandler {
    // 限流兜底：静态方法 + 参数匹配 + BlockException
    public static Result blockHandlerRestoreUrl(  String shortUri,
                                         HttpServletRequest request,
                                         HttpServletResponse response,
                                         BlockException ex) {
        return Result.fail("请求太快啦，稍后再试~");
    }

    // 熔断降级兜底：静态方法 + 参数匹配 + Throwable
    public static Result restoreUrlFallback( String shortUri,
                                         HttpServletRequest request,
                                         HttpServletResponse response,
                                         BlockException ex) {
        return Result.fail("服务繁忙，请稍后重试");
    }

}
