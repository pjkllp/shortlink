package com.nageoffer.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nageoffer.shortlink.project.dao.entity.ShortLinkDO;
import com.nageoffer.shortlink.project.dto.Req.RecycleBinPageReqDTO;
import com.nageoffer.shortlink.project.dto.Req.RecycleBinRecoverReqDTO;
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

    /**
     * 分页查询在回收站的短链接
     * @param requestParam 入参
     * @return 分页后的响应实体
     */
    IPage<RecycleBinPageRespDTO> recycleBinPage(RecycleBinPageReqDTO requestParam);

    /**
     * 将回收站的短链接移除
     * @param requestParam 入参
     */
    void recycleBinRecover(RecycleBinRecoverReqDTO requestParam);
}
