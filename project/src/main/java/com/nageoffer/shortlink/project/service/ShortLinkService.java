package com.nageoffer.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nageoffer.shortlink.project.common.convention.result.Result;
import com.nageoffer.shortlink.project.dao.entity.ShortLinkDO;
import com.nageoffer.shortlink.project.dto.Req.ShortLinkCreateReqDTO;
import com.nageoffer.shortlink.project.dto.Req.ShortLinkPageReqTO;
import com.nageoffer.shortlink.project.dto.Resp.ShortLinkCreateRespDTO;
import com.nageoffer.shortlink.project.dto.Resp.ShortLinkGroupCountQueryReqDTO;
import com.nageoffer.shortlink.project.dto.Resp.ShortLinkPageRespDTO;

import java.util.List;
import java.util.Map;

public interface ShortLinkService extends IService<ShortLinkDO>{

    /**
     * 创建短链接
     * @param requestParam 短链接创建请求参数
     * @return 短链接创建响应参数
     */
    ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestParam);

    /**
     * 分页查询短链接
     * @param requestParam 短链接请求参数
     * @return 分页返回
     */
    IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqTO requestParam);

    /**
     * 查询短链接各分组的短链接数量
     * @param gids
     * @return
     */
    List<ShortLinkGroupCountQueryReqDTO> listGroupShortLinkCount(List<String> gids);
}
