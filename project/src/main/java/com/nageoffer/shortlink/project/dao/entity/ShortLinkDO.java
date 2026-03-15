package com.nageoffer.shortlink.project.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 短链接实体类
 * 对应数据表：t_link
 *
 * @author 你的名字
 * @date 2026/03/15
 */
@Data
@TableName("t_link")
@Accessors(chain = true)
public class ShortLinkDO implements Serializable {

    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 域名
     */
    private String domain;

    /**
     * 短链接
     */
    private String shortUri;

    /**
     * 完整短链接
     */
    private String fullShortUrl;

    /**
     * 原始链接
     */
    private String originUrl;

    /**
     * 点击量
     */
    private Integer clickNum;

    /**
     * 启用标识,0：启用，1：未启用
     */
    private Integer enableStatus;

    /**
     * 创建类型，0：接口创建，1：控制台创建
     */
    private Integer createdType;

    /**
     * 有效期类型，0：永久有效，1：自定义
     */
    private Integer validDataType;

    /**
     * 有效期
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
    private Date createTime;

    /**
     * 更新时间
     */
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
}