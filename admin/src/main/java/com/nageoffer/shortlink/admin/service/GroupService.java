package com.nageoffer.shortlink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nageoffer.shortlink.admin.dao.entity.GroupDO;
import com.nageoffer.shortlink.admin.dto.req.ShortLinkGroupSortReqDTO;
import com.nageoffer.shortlink.admin.dto.req.ShortLinkGroupUpdateReq;
import com.nageoffer.shortlink.admin.dto.resp.ShortLinkGroupRespDTO;

import java.util.List;

public interface GroupService extends IService<GroupDO> {
    /**
     * 新增短链接分组
     * @param groupName 短链接分组名称
     */
    void saveGroup(String groupName);

    /**
     * 查询用户所有的短链接
     * @return
     */
    List<ShortLinkGroupRespDTO> listGroup();

    /**
     * 修改短链接分组名称
     * @param requestParam
     */
    void updateGroup(ShortLinkGroupUpdateReq requestParam);

    /**
     * 删除短链接分组
     * @param gid
     */
    void deleteGroup(String gid);

    /**
     * 短链接分组排序
     * @param requestParam
     */
    void sortGroup(List<ShortLinkGroupSortReqDTO> requestParam);
}
