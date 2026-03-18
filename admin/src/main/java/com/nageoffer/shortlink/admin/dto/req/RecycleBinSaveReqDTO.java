package com.nageoffer.shortlink.admin.dto.req;

import lombok.Data;

/**
 * 回收站保存功能请求参数
 */
@Data
public class RecycleBinSaveReqDTO {
    /**
     * 分组标识
     */
    private String gid;

    /**
     * 完整短链接
     */
    private String fullShortUrl;


}
