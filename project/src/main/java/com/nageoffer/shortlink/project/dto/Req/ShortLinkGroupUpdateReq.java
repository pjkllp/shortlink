package com.nageoffer.shortlink.project.dto.Req;

import lombok.Data;

/**
 * 短链接分组修改请求参数
 */
@Data
public class ShortLinkGroupUpdateReq {
    /**
     * 分组标识
     */
    private String gid;

    /**
     * 分组名称
     */
    private String name;
}
