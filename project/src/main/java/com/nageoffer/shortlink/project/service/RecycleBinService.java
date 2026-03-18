package com.nageoffer.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nageoffer.shortlink.project.dao.entity.ShortLinkDO;
import com.nageoffer.shortlink.project.dto.Req.RecycleBinPageReqDTO;
import com.nageoffer.shortlink.project.dto.Req.RecycleBinSaveReqDTO;
import com.nageoffer.shortlink.project.dto.Resp.RecycleBinPageRespDTO;

/**
 * 回收站管理服务
 */
public interface RecycleBinService extends IService<ShortLinkDO> {

    /**
     * 将短链接移至回收站
     * @param requestParam 回收站保存请求参数
     * @throws InterruptedException 保存回收站失败
     */
    void recycleBinSave(RecycleBinSaveReqDTO requestParam) throws InterruptedException;

    IPage<RecycleBinPageRespDTO> recycleBinPage(RecycleBinPageReqDTO requestParam);
}
