package com.nageoffer.shortlink.project.controller;

import com.nageoffer.shortlink.project.common.convention.result.Result;
import com.nageoffer.shortlink.project.dto.Req.ShortLinkGroupDeleteReq;
import com.nageoffer.shortlink.project.dto.Req.ShortLinkGroupSaveReqDTO;
import com.nageoffer.shortlink.project.dto.Req.ShortLinkGroupSortReqDTO;
import com.nageoffer.shortlink.project.dto.Req.ShortLinkGroupUpdateReq;
import com.nageoffer.shortlink.project.dto.Resp.ShortLinkGroupRespDTO;
import com.nageoffer.shortlink.project.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class GroupController {
    private final GroupService groupService;

    @PostMapping("/api/short-link/project/v1/group")
    public Result<Void> save(@RequestBody ShortLinkGroupSaveReqDTO requestParam) {
        groupService.saveGroup(requestParam.getName());
        return Result.success("新建分组成功");
    }

    @GetMapping("/api/short-link/project/v1/group")
    public Result<List<ShortLinkGroupRespDTO>> listGroup() {
        return Result.success("查询成功", groupService.listGroup());
    }

    @PutMapping("/api/short-link/project/v1/group")
    public Result<Void> update(@RequestBody ShortLinkGroupUpdateReq requestParam) {
        groupService.updateGroup(requestParam);
        return Result.success("修改成功");
    }

    @DeleteMapping("/api/short-link/project/v1/group")
    public Result<Void> delete(@RequestBody ShortLinkGroupDeleteReq requestParam) {
        groupService.deleteGroup(requestParam.getGid());
        return Result.success("删除成功");
    }

    @PostMapping("/api/short-link/project/v1/group/sort")
    public Result<Void> sortGroup(@RequestBody List<ShortLinkGroupSortReqDTO> requestParam) {
        groupService.sortGroup(requestParam);
        return Result.success("排序成功");
    }

    @PostMapping("/api/short-link/project/v1/group/internal/create")
    public Result<Void> saveForUsername(@RequestHeader("username") String username,
                                        @RequestBody ShortLinkGroupSaveReqDTO requestParam) {
        groupService.saveGroup(username, requestParam.getName());
        return Result.success("新建分组成功");
    }
}
