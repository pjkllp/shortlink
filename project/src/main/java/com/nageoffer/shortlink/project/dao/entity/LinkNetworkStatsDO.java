package com.nageoffer.shortlink.project.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 短链接网络访问统计表实体类
 * 对应数据表：t_link_network_stats
 */
@Data
@TableName(value = "t_link_network_stats") // 精准匹配数据表名
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LinkNetworkStatsDO {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 完整短链接
     * 对应数据表字段：full_short_url
     */
    @TableField(value = "full_short_url")
    private String fullShortUrl;

    /**
     * 分组标识（默认值：default）
     */
    @TableField(value = "gid")
    private String gid;

    /**
     * 日期（date是MySQL保留字，显式指定字段名）
     */
    @TableField(value = "date")
    private Date date;

    /**
     * 访问量
     */
    @TableField(value = "cnt")
    private Integer cnt;

    /**
     * 访问网络（WiFi/4G/5G/蜂窝网络/未知网络等）
     */
    @TableField(value = "network")
    private String network;

    /**
     * 创建时间（插入时自动填充）
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 修改时间（插入/更新时自动填充）
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /**
     * 删除标识（0：未删除 1：已删除）
     * @TableLogic 实现逻辑删除，MyBatis-Plus自动处理
     */
    @TableField(value = "del_flag")
    @TableLogic
    private Boolean delFlag;
}