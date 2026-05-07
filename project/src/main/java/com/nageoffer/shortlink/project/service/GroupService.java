package com.nageoffer.shortlink.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nageoffer.shortlink.project.dao.entity.GroupDO;
import com.nageoffer.shortlink.project.dto.Req.ShortLinkGroupSortReqDTO;
import com.nageoffer.shortlink.project.dto.Req.ShortLinkGroupUpdateReq;
import com.nageoffer.shortlink.project.dto.Resp.ShortLinkGroupRespDTO;

import java.util.List;

public interface GroupService extends IService<GroupDO> {
    void saveGroup(String groupName);

    void saveGroup(String username, String groupName);

    List<ShortLinkGroupRespDTO> listGroup();

    void updateGroup(ShortLinkGroupUpdateReq requestParam);

    void deleteGroup(String gid);

    void sortGroup(List<ShortLinkGroupSortReqDTO> requestParam);
}
