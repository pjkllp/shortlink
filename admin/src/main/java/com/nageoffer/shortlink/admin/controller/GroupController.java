package com.nageoffer.shortlink.admin.controller;

import com.nageoffer.shortlink.admin.common.convention.result.Result;
import com.nageoffer.shortlink.admin.dto.req.ShortLinkGroupDeleteReq;
import com.nageoffer.shortlink.admin.dto.req.ShortLinkGroupSaveReqDTO;
import com.nageoffer.shortlink.admin.dto.req.ShortLinkGroupSortReqDTO;
import com.nageoffer.shortlink.admin.dto.req.ShortLinkGroupUpdateReq;
import com.nageoffer.shortlink.admin.dto.resp.ShortLinkGroupRespDTO;
import com.nageoffer.shortlink.admin.remote.Service.ShortLinkActualRemoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class GroupController {
    private final ShortLinkActualRemoteService shortLinkActualRemoteService;

    /**
     * 新增短链接分组
     * @param requestParam 短链接分组请求参数
     * @return 分组成功
     */
    @PostMapping("/api/short-link/admin/v1/group")
    public ResponseEntity<Result<Void>> save(@RequestBody ShortLinkGroupSaveReqDTO requestParam){
        return ResponseEntity.ok(shortLinkActualRemoteService.saveGroup(requestParam));
    }

    /**
     * 查询短链接分组集合
     * @return 某个用户的所有短链接分组
     */
    @GetMapping("/api/short-link/admin/v1/group")
    public ResponseEntity<Result<List<ShortLinkGroupRespDTO>>> listGroup(){
        return ResponseEntity.ok(shortLinkActualRemoteService.listGroup());
    }

    /**
     * 修改短链接分组名称
     * admin 仅做转发，实际校验与更新在 project 完成
     */
    @PutMapping("/api/short-link/admin/v1/group")
    public ResponseEntity<Result<Void>> update(@RequestBody ShortLinkGroupUpdateReq requestParam){
        return ResponseEntity.ok(shortLinkActualRemoteService.updateGroup(requestParam));
    }

    /**
     * 删除短链接分组
     * @param requestParam
     * @return
     */
    @DeleteMapping("/api/short-link/admin/v1/group")
    public ResponseEntity<Result<Void>> delete(@RequestBody ShortLinkGroupDeleteReq requestParam){
        return ResponseEntity.ok(shortLinkActualRemoteService.deleteGroup(requestParam));
    }

    /**
     * 短链接分组排序
     * @param requestParam
     * @return
     */
    @PostMapping("/api/short-link/admin/v1/group/sort")
    public ResponseEntity<Result<Void>> sortGroup(@RequestBody List<ShortLinkGroupSortReqDTO> requestParam){
        return ResponseEntity.ok(shortLinkActualRemoteService.sortGroup(requestParam));
    }


}
