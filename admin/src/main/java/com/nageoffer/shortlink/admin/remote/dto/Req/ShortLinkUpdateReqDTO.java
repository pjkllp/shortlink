package com.nageoffer.shortlink.admin.remote.dto.Req;

import lombok.Data;

import java.util.Date;

@Data
public class ShortLinkUpdateReqDTO {

    /**
     * 原始链接
     */
    private String originUrl;

    /**
     * 完整短链
     */
    private String fullShortUrl;

    /**
     * 有效期类型，0：永久有效，1：自定义
     */
    private Integer validDataType;

    /**
     * 有效日期
     */
    private Date validData;

    /**
     * 描述
     */
    private String description;

    /**
     * 变更后的分组标识
     */
    private String gid;

    /**
     * 原始分组标识
     */
    private String originGid;
}
