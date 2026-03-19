package com.nageoffer.shortlink.project.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 短链访问统计表实体类
 * 对应数据表：t_link_access_stats
 */
@Data
@TableName(value = "t_link_access_stats")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LinkOsStatsDO {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID) // 主键自增，和数据表中id字段的NOT NULL对应
    private Integer id;

    /**
     * 完整短链接
     */
    @TableField(value = "full_short_url") // 字段名和实体属性驼峰对应时可省略value
    private String fullShortUrl;

    /**
     * 分组标识
     */
    @TableField(value = "gid")
    private String gid;

    /**
     * 访问量
     */
    @TableField(value = "cnt")
    private Integer cnt;

    /**
     * 操作系统
     */
    @TableField(value = "os")
    private String os;

    /**
     * 日期（注意：date是MySQL保留字，这里字段映射正常）
     */
    @TableField(value = "date")
    private Date date;

    /**
     * 创建日期
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT) // 插入时自动填充
    private Date createTime;

    /**
     * 更新日期
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE) // 插入/更新时自动填充
    private Date updateTime;

    /**
     * 删除标识（1=删除，0=未删除）
     */
    @TableField(value = "del_flag")
    @TableLogic // 逻辑删除注解，MyBatis-Plus自动处理删除/查询逻辑
    private Boolean delFlag;
}