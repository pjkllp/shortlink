package com.nageoffer.shortlink.project.dto.Req;

import lombok.Data;

@Data
public class ShortLinkGroupSortReqDTO {
    /**
     * 分组ID
     */
    private String gid;

    /**
     * 排序字段
     */
    private Integer sortOrder;
}
