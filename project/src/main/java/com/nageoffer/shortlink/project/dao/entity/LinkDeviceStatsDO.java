package com.nageoffer.shortlink.project.dao.entity; // 替换为你的实际包名

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 短链接设备访问统计表实体类
 * 对应数据表：t_link_device_stats
 */
@Data
@TableName(value = "t_link_device_stats")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LinkDeviceStatsDO {

    /**
     * 主键ID（自增）
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
     * 访问设备（手机/电脑/平板/小程序等）
     */
    @TableField(value = "device")
    private String device;

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