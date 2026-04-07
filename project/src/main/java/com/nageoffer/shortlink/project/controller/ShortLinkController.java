package com.nageoffer.shortlink.project.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nageoffer.shortlink.project.Handler.ShortLinkBlockHandler;
import com.nageoffer.shortlink.project.common.convention.result.Result;
import com.nageoffer.shortlink.project.dto.Req.ShortLinkCreateReqDTO;
import com.nageoffer.shortlink.project.dto.Req.ShortLinkPageReqDTO;
import com.nageoffer.shortlink.project.dto.Req.ShortLinkUpdateReqDTO;
import com.nageoffer.shortlink.project.dto.Resp.ShortLinkCreateRespDTO;
import com.nageoffer.shortlink.project.dto.Resp.ShortLinkGroupCountQueryRespDTO;
import com.nageoffer.shortlink.project.dto.Resp.ShortLinkPageRespDTO;
import com.nageoffer.shortlink.project.service.ShortLinkService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@RestController
public class ShortLinkController {

    private final ShortLinkService shortLinkService;

    /**
     * 创建短链接
     * @return
     */
    @PostMapping("/api/short-link/project/v1/link")
    public Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO requestParam) throws IOException {
        return Result.success("创建短链接成功",shortLinkService.createShortLink(requestParam));
    }

    /**
     * 分页查询短链接
     * @param requestParam
     * @return
     */
    @GetMapping("/api/short-link/project/v1/page")
    public Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO requestParam){
        return Result.success(shortLinkService.pageShortLink(requestParam));
    }

    /**
     * 查询分组下的短链接数
     */
    @GetMapping("/api/short-link/project/v1/count")
    public Result<List<ShortLinkGroupCountQueryRespDTO>> groupShortLinkCount(@RequestParam("gids") List<String> requestParam){
        return Result.success("查询成功",shortLinkService.listGroupShortLinkCount(requestParam));
    }

    @PutMapping("/api/short-link/project/v1/link")
    public Result<Void> updateShortLink(@RequestBody ShortLinkUpdateReqDTO requestParam){
        shortLinkService.updateShortLink(requestParam);
        return Result.success("修改成功");
    }

    @SentinelResource(
            value = "short-link-goto-link",
            //熔断降级
            blockHandlerClass = ShortLinkBlockHandler.class,
            blockHandler = "blockHandlerRestoreUrl",
            //异常兜底
            fallback = "restoreUrlFallback",
            fallbackClass = ShortLinkBlockHandler.class
    )
    @GetMapping("/{short-uri}")
    public Result<Void> restoreUrl(@PathVariable("short-uri")String shortUri, HttpServletRequest request, HttpServletResponse response) throws IOException, InterruptedException {
        shortLinkService.restoreUrl(shortUri,request,response);
        return Result.success();
    }

}
