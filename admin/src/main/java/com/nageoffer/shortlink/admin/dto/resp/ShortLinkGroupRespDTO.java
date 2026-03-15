package com.nageoffer.shortlink.admin.dto.resp;

import lombok.Data;

/**
 * 分组返回实体
 */
@Data
public class ShortLinkGroupRespDTO {
    /**
     * 分组标识
     */
    private String gid;

    /**
     * 分组名称
     */
    private String name;

    /**
     * 创建分组用户名
     */
    private String username;

    /**
     * 分组排序参数
     */
    private int sortOrder;
}
