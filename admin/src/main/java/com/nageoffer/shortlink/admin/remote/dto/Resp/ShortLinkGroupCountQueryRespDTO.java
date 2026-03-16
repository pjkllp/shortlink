package com.nageoffer.shortlink.admin.remote.dto.Resp;

import lombok.Data;

/**
 * 短链接分组查询返回
 */
@Data
public class ShortLinkGroupCountQueryRespDTO {
    /**
     * 分组标识
     */
    private String gid;

    /**
     * 短链接数
     */
    private Integer shortLinkCount;
}
