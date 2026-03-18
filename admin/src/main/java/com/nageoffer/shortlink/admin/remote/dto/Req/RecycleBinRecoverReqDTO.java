package com.nageoffer.shortlink.admin.remote.dto.Req;

import lombok.Data;

/**
 * 移出短链接请求参数
 */
@Data
public class RecycleBinRecoverReqDTO {

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 完整短路径
     */
    private String fullShortUrl;

    /**
     * 原始路径
     */
    private String originUrl;
}
