package com.nageoffer.shortlink.admin.remote.Service.fallback;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nageoffer.shortlink.admin.common.convention.result.Result;
import com.nageoffer.shortlink.admin.dto.req.*;
import com.nageoffer.shortlink.admin.dto.resp.ShortLinkGroupRespDTO;
import com.nageoffer.shortlink.admin.remote.Service.ShortLinkActualRemoteService;
import com.nageoffer.shortlink.admin.remote.dto.Req.*;
import com.nageoffer.shortlink.admin.remote.dto.Resp.*;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 远程兜底返回
 */
@Component
public class ShortLinkActualRemoteServiceFallback implements ShortLinkActualRemoteService {
    @Override
    public Result<List<ShortLinkGroupCountQueryRespDTO>> count(List<String> requestParam) {
        return Result.fail("服务不可用");
    }

    @Override
    public Result<ShortLinkCreateRespDTO> createShortLink(ShortLinkCreateReqDTO requestParam) {
        return Result.fail("服务不可用");
    }

    @Override
    public Result<Void> updateShortLink(ShortLinkUpdateReqDTO requestParam) {
        return Result.fail("服务不可用");
    }

    @Override
    public Result<Page<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO requestParam) {
        return Result.fail("服务不可用");
    }

    @Override
    public Result<String> getTitle(String url) {
        return Result.fail("服务不可用");
    }

    @Override
    public Result<Void> recycleBinSave(RecycleBinSaveReqDTO requestParam) {
        return Result.fail("服务不可用");
    }

    @Override
    public Result<Page<RecycleBinPageRespDTO>> recycleBinPage(RecycleBinPageReqDTO requestParam) {
        return Result.fail("服务不可用");
    }

    @Override
    public Result<Void> recycleBinRecover(RecycleBinRecoverReqDTO requestParam) {
        return Result.fail("服务不可用");
    }

    @Override
    public Result<Void> recycleBinRemove(RecycleBinRemoveReqDTO requestParam) {
        return Result.fail("服务不可用");
    }

    @Override
    public Result<Page<ShortLinkStatsAccessRecordRespDTO>> statsAccessRecord(ShortLinkStatsAccessRecordReqDTO requestParam) {
        return Result.fail("服务不可用");
    }

    @Override
    public Result<ShortLinkStatsRespDTO> stats(ShortLinkStatsReqDTO requestParam) {
        return Result.fail("服务不可用");
    }

    @Override
    public Result<Void> saveGroup(ShortLinkGroupSaveReqDTO requestParam) {
        return Result.fail("服务不可用");
    }

    @Override
    public Result<List<ShortLinkGroupRespDTO>> listGroup() {
        return Result.fail("服务不可用");
    }

    @Override
    public Result<Void> updateGroup(ShortLinkGroupUpdateReq requestParam) {
        return Result.fail("服务不可用");
    }

    @Override
    public Result<Void> deleteGroup(ShortLinkGroupDeleteReq requestParam) {
        return Result.fail("服务不可用");
    }

    @Override
    public Result<Void> sortGroup(List<ShortLinkGroupSortReqDTO> requestParam) {
        return Result.fail("服务不可用");
    }

    @Override
    public Result<Void> saveGroupForUsername(String username, String isAdmin, ShortLinkGroupSaveReqDTO requestParam) {
        return Result.fail("服务不可用");
    }
}
