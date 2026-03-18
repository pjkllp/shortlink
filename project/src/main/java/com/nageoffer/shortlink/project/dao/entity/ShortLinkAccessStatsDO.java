package com.nageoffer.shortlink.project.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 短链接访问统计实体类
 * 对应表：t_link_access_stats
 */
@Data
@TableName("t_link_access_stats")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShortLinkAccessStatsDO {

    /**
     * ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 完整短链接
     */
    @TableField("full_short_url")
    private String fullShortUrl;

    /**
     * 分组标识
     */
    @TableField("gid")
    private String gid;

    /**
     * 日期
     */
    @TableField("date")
    private Date date; // 数据库是date类型，Java用Date接收（也可用LocalDate）

    /**
     * 访问量
     */
    @TableField("pv")
    private Integer pv;

    /**
     * 独立访客数
     */
    @TableField("uv")
    private Integer uv;

    /**
     * 独立id数
     */
    @TableField("uip")
    private Integer uip;

    /**
     * 小时
     */
    @TableField("hour")
    private Integer hour;

    /**
     * 星期
     */
    @TableField("weekday")
    private Integer weekday;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT) // 插入时自动填充
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE) // 插入/更新时自动填充
    private Date updateTime;

    /**
     * 删除标识
     */
    @TableLogic // 逻辑删除注解（匹配del_flag字段）
    @TableField("del_flag")
    private Boolean delFlag;
}