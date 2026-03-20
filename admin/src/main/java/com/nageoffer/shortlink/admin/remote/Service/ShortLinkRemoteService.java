package com.nageoffer.shortlink.admin.remote.Service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nageoffer.shortlink.admin.common.convention.result.Result;
import com.nageoffer.shortlink.admin.dto.req.RecycleBinSaveReqDTO;
import com.nageoffer.shortlink.admin.remote.dto.Req.*;
import com.nageoffer.shortlink.admin.remote.dto.Resp.RecycleBinPageRespDTO;
import com.nageoffer.shortlink.admin.remote.dto.Resp.ShortLinkCreateRespDTO;
import com.nageoffer.shortlink.admin.remote.dto.Resp.ShortLinkGroupCountQueryRespDTO;
import com.nageoffer.shortlink.admin.remote.dto.Resp.ShortLinkPageRespDTO;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface ShortLinkRemoteService{

    /**
     * 远程调用查询各分组短链接数量
     * @param requestParam 分组的集合
     * @return 分组数据实参
     */
    List<ShortLinkGroupCountQueryRespDTO> count(@RequestParam List<String> requestParam);

    /**
     * 远程创建短链接
     * @param requestParam 远程创建短链接实参
     * @return
     */
    Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO requestParam);

    /**
     * 短链接分页查询
     * @param requestParam 短链接分页查询参数
     * @return 短链接分页查询参数
     */
    Result<IPage<ShortLinkPageRespDTO>> pageShortLink(@RequestBody ShortLinkPageReqDTO requestParam);

    /**
     * 根据url查询网址标题
     * @param url 网址
     * @return 网址标题
     */
    Result<String> getTitle(String url);

    /**
     * 回收站管理回收
     * @param requestParam 回收请求实体
     */
    Result<Void> recycleBinSave(RecycleBinSaveReqDTO requestParam);

    /**
     * 回收站分页查询,根据当前用户username来查分组，再调用远程来根据gids查所有短链接
     * @param requestParam 回收站分页查询请求参数
     * @return 回收站分页查询响应参数
     */
    Result<IPage<RecycleBinPageRespDTO>> recycleBinPage(RecycleBinPageReqDTO requestParam);

    /**
     * 将短链接从回收站恢复
     * @param requestParam 短链接回收站恢复参数
     * @return 远程响应
     */
    Result<Void> recycleBinRecover(RecycleBinRecoverReqDTO requestParam);

    /**
     * 将短链接从回收站永久删除
     * @param requestParam 短链接删除参数
     * @return 远程响应
     */
    Result<Void> recycleBinRemove(RecycleBinRemoveReqDTO requestParam);
}
