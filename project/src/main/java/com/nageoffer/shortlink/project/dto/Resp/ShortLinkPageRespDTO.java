package com.nageoffer.shortlink.project.dto.Resp;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 短链接分页返回参
 */
@Data
public class ShortLinkPageRespDTO {
    /**
     * 域名
     */
    private String domain;

    /**
     * 短链
     */
    private String shortUri;

    /**
     * 完整短链
     */
    private String fullShortUrl;

    /**
     * 原始链接
     */
    private String originUri;

    /**
     * 有效期类型，0：永久有效，1：自定义
     */
    private Integer validDataType;

    /**
     * 有效
     */
    private Date validData;

    /**
     * 描述
     */
    private String description;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /**
     * 删除标识
     */
    @TableLogic
    private Integer delFlag;

    /**
     * 分组id
     */
    private String gid;

    /**
     * 网站标识
     */
    private String favicon;
}
