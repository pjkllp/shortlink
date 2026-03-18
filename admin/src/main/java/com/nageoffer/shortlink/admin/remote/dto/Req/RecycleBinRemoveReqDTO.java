package com.nageoffer.shortlink.admin.remote.dto.Req;

import lombok.Data;

@Data
public class RecycleBinRemoveReqDTO {

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 完整短路径
     */
    private String fullShortUrl;
}
