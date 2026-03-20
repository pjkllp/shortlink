package com.nageoffer.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nageoffer.shortlink.project.dao.entity.ShortLinkDO;
import com.nageoffer.shortlink.project.dto.Req.ShortLinkCreateReqDTO;
import com.nageoffer.shortlink.project.dto.Req.ShortLinkPageReqDTO;
import com.nageoffer.shortlink.project.dto.Req.ShortLinkUpdateReqDTO;
import com.nageoffer.shortlink.project.dto.Resp.ShortLinkCreateRespDTO;
import com.nageoffer.shortlink.project.dto.Resp.ShortLinkGroupCountQueryRespDTO;
import com.nageoffer.shortlink.project.dto.Resp.ShortLinkPageRespDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

public interface ShortLinkService extends IService<ShortLinkDO>{

    /**
     * 创建短链接
     * @param requestParam 短链接创建请求参数
     * @return 短链接创建响应参数
     */
    ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestParam) throws IOException;

    /**
     * 分页查询短链接
     * @param requestParam 短链接请求参数
     * @return 分页返回
     */
    IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqDTO requestParam);

    /**
     * 查询短链接各分组的短链接数量
     * @param gids
     * @return
     */
    List<ShortLinkGroupCountQueryRespDTO> listGroupShortLinkCount(List<String> gids);

    /**
     * 修改短链接
     * @param requestParam 短链接修改参数
     */
    void updateShortLink(ShortLinkUpdateReqDTO requestParam);


    /**
     * 短链接跳转重定向
     * @param shortUri
     * @param request
     * @param response
     * @throws IOException
     */
    void restoreUrl(String shortUri, HttpServletRequest request, HttpServletResponse response) throws IOException, InterruptedException;
}
