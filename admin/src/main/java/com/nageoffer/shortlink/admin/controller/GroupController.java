package com.nageoffer.shortlink.admin.controller;

import com.nageoffer.shortlink.admin.common.convention.result.Result;
import com.nageoffer.shortlink.admin.dto.req.ShortLinkGroupDeleteReq;
import com.nageoffer.shortlink.admin.dto.req.ShortLinkGroupSaveReqDTO;
import com.nageoffer.shortlink.admin.dto.req.ShortLinkGroupSortReqDTO;
import com.nageoffer.shortlink.admin.dto.req.ShortLinkGroupUpdateReq;
import com.nageoffer.shortlink.admin.dto.resp.ShortLinkGroupRespDTO;
import com.nageoffer.shortlink.admin.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.annotations.Delete;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class GroupController {
    private final GroupService groupService;

    /**
     * 新增短链接分组
     * @param requestParam 短链接分组请求参数
     * @return 分组成功
     */
    @PostMapping("/api/short-link/v1/group")
    public ResponseEntity<Result<Void>> save(@RequestBody ShortLinkGroupSaveReqDTO requestParam){
        groupService.saveGroup(requestParam.getName());
        return ResponseEntity.ok(Result.success("新建分组成功"));
    }

    /**
     * 查询短链接分组集合
     * @return 某个用户的所有短链接分组
     */
    @GetMapping("/api/short-link/v1/group")
    public ResponseEntity<Result<List<ShortLinkGroupRespDTO>>> listGroup(){
        return ResponseEntity.ok(Result.success("查询成功",groupService.listGroup()));
    }

    /**
     * 修改短链接分组名称
     * @param requestParam
     * @return
     */
    @PutMapping("/api/short-link/v1/group")
    public ResponseEntity<Result<Void>> update(@RequestBody ShortLinkGroupUpdateReq requestParam){
        groupService.updateGroup(requestParam);
        return ResponseEntity.ok(Result.success("修改成功"));
    }

    /**
     * 删除短链接分组
     * @param requestParam
     * @return
     */
    @DeleteMapping("/api/short-link/v1/group")
    public ResponseEntity<Result<Void>> delete(@RequestBody ShortLinkGroupDeleteReq requestParam){
        groupService.deleteGroup(requestParam.getGid());
        return ResponseEntity.ok(Result.success("删除成功"));
    }

    /**
     * 短链接分组排序
     * @param requestParam
     * @return
     */
    @PostMapping("/api/short-link/v1/group/sort")
    public ResponseEntity<Result<Void>> sortGroup(@RequestBody List<ShortLinkGroupSortReqDTO> requestParam){
        groupService.sortGroup(requestParam);
        return ResponseEntity.ok(Result.success("排序成功"));
    }


}
