package com.nageoffer.shortlink.admin.handler;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.nageoffer.shortlink.admin.common.convention.result.Result;
import com.nageoffer.shortlink.admin.remote.dto.Req.ShortLinkCreateReqDTO;
import com.nageoffer.shortlink.admin.remote.dto.Resp.ShortLinkCreateRespDTO;

public class ShortLinkBlockHandler {
    // 限流兜底：静态方法 + 参数匹配 + BlockException
    public static Result<ShortLinkCreateRespDTO> createBlock(ShortLinkCreateReqDTO requestParam, BlockException e) {
        return Result.fail("请求太快啦，稍后再试~");
    }

    // 熔断降级兜底：静态方法 + 参数匹配 + Throwable
    public static Result<ShortLinkCreateRespDTO> createFallback(ShortLinkCreateReqDTO requestParam, Throwable e) {
        return Result.fail("服务繁忙，请稍后重试");
    }

}
