package com.nageoffer.shortlink.project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nageoffer.shortlink.project.common.convention.result.Result;
import com.nageoffer.shortlink.project.dto.Req.ShortLinkCreateReqDTO;
import com.nageoffer.shortlink.project.dto.Req.ShortLinkPageReqTO;
import com.nageoffer.shortlink.project.dto.Resp.ShortLinkCreateRespDTO;
import com.nageoffer.shortlink.project.dto.Resp.ShortLinkPageRespDTO;
import com.nageoffer.shortlink.project.service.ShortLinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class ShortLinkController {

    private final ShortLinkService shortLinkService;

    /**
     * 创建短链接
     * @return
     */
    @PostMapping("/api/short-link/project/v1/link")
    public ResponseEntity<Result<ShortLinkCreateRespDTO>> createShortLink(@RequestBody ShortLinkCreateReqDTO requestParam){
        return ResponseEntity.ok(Result.success("创建短链接成功",shortLinkService.createShortLink(requestParam)));
    }

    /**
     * 分页查询短链接
     * @param requestParam
     * @return
     */
    @PostMapping("/api/short-link/project/v1/page")
    public ResponseEntity<Result<IPage<ShortLinkPageRespDTO>>> pageShortLink(@RequestBody ShortLinkPageReqTO requestParam){
        return ResponseEntity.ok(Result.success(shortLinkService.pageShortLink(requestParam)));
    }
}
