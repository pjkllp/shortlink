package com.nageoffer.shortlink.project.dao.entity; // 替换为你的实际包名

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.units.qual.A;

import java.util.Date;

/**
 * 短链接浏览器访问统计表实体类
 * 对应数据表：t_link_browser_stats
 */
@Data
@TableName(value = "t_link_browser_stats")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LinkBrowserStatsDO {

    /**
     * 主键ID（自增）
     * 关键：IdType.AUTO 匹配数据库AUTO_INCREMENT，解决id为空问题
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 完整短链接
     * 对应数据表字段：full_short_url
     */
    @TableField(value = "full_short_url")
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
     * 日期（date是MySQL保留字，通过value指定字段名）
     */
    @TableField(value = "date")
    private Date date;

    /**
     * 浏览器类型（如Chrome 122、微信浏览器 8.0）
     */
    @TableField(value = "browser")
    private String browser;

    /**
     * 创建时间（插入时自动填充）
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 更新时间（插入/更新时自动填充）
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /**
     * 删除标识（1=删除，0=未删除）
     * @TableLogic 实现逻辑删除，MyBatis-Plus自动处理
     */
    @TableField(value = "del_flag")
    @TableLogic
    private Boolean delFlag;
}