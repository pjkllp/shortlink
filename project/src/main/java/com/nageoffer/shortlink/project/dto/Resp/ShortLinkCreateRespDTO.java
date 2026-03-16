package com.nageoffer.shortlink.project.dto.Resp;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 * 短链接创建返回对象
 */
@Data
@Builder
public class ShortLinkCreateRespDTO {
    /**
     * 分组信息
     */
    private  String gid;

    /**
     * 域名
     */
    private String domain;

    /**
     * 完整短链接
     */
    private String fullShortUrl;

    /**
     * 原始链接
     */
    private String originUrl;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH-mm-ss",timezone = "GMT+8")
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH-mm-ss",timezone = "GMT+8")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}
