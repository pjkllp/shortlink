package com.nageoffer.shortlink.admin.remote.Service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nageoffer.shortlink.admin.common.convention.result.Result;
import com.nageoffer.shortlink.admin.dto.req.RecycleBinSaveReqDTO;
import com.nageoffer.shortlink.admin.remote.dto.Req.*;
import com.nageoffer.shortlink.admin.remote.dto.Resp.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient("shortlink-project")
@Component
public interface ShortLinkActualRemoteService {
    /**
     * 远程调用查询各分组短链接数量
     * @param requestParam 分组的集合
     * @return 分组数据实参
     */
    @GetMapping("/api/short-link/project/v1/count")
    Result<List<ShortLinkGroupCountQueryRespDTO>> count(@RequestParam("gids") List<String> requestParam);

    /**
     * 远程创建短链接
     * @param requestParam 远程创建短链接实参
     * @return
     */
    @PostMapping("/api/short-link/project/v1/link")
    Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO requestParam);

    /**
            * 短链接分页查询
     * @param requestParam 短链接分页查询参数
     * @return 短链接分页查询参数
     */
    @GetMapping("/api/short-link/project/v1/page")
    Result<Page<ShortLinkPageRespDTO>> pageShortLink(@SpringQueryMap ShortLinkPageReqDTO requestParam);


    /**
     * 根据url查询网址标题
     * @param url 网址
     * @return 网址标题
     */
    @GetMapping("/api/short-link/project/v1/title")
    Result<String> getTitle(@RequestParam("url") String url);

    /**
     * 回收站管理回收
     * @param requestParam 回收请求实体
     */
    @PostMapping("/api/short-link/project/v1/recycle-bin/save")
    Result<Void> recycleBinSave(@RequestBody RecycleBinSaveReqDTO requestParam);

    /**
     * 回收站分页查询,根据当前用户username来查分组，再调用远程来根据gids查所有短链接
     * @param requestParam 回收站分页查询请求参数
     * @return 回收站分页查询响应参数
     */
    @GetMapping("/api/short-link/project/v1/recycle-bin/page")
    Result<Page<RecycleBinPageRespDTO>> recycleBinPage(@SpringQueryMap RecycleBinPageReqDTO requestParam);

    /**
     * 将短链接从回收站恢复
     * @param requestParam 短链接回收站恢复参数
     * @return 远程响应
     */
    @PostMapping("/api/short-link/project/v1/recycle-bin/recover")
    Result<Void> recycleBinRecover(@RequestBody RecycleBinRecoverReqDTO requestParam);

    /**
     * 将短链接从回收站永久删除
     * @param requestParam 短链接删除参数
     * @return 远程响应
     */
    @PostMapping("/api/short-link/project/v1/recycle-bin/remove")
    Result<Void> recycleBinRemove(@RequestBody RecycleBinRemoveReqDTO requestParam);

    /**
     * 短链接访客记录远程调用
     * @param requestParam
     * @return
     */
    @GetMapping("/api/short-link/project/v1/statsAccessRecord")
    Result<Page<ShortLinkStatsAccessRecordRespDTO>> statsAccessRecord(@SpringQueryMap ShortLinkStatsAccessRecordReqDTO requestParam);

    /**
     * 短链接基本统计远程调用
     * @param requestParam
     * @return
     */
    @GetMapping("/api/short-link/project/v1/stats")
    Result<ShortLinkStatsRespDTO> stats(@SpringQueryMap ShortLinkStatsReqDTO requestParam);
}
