package com.nageoffer.shortlink.project.dao.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 访问日志监控实体
 */
@Data
@TableName("t_link_access_logs")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinkAccessLogsDO  {

    /**
     * id
     */
    private Long id;

    /**
     * 完整短链
     */
    private String fullShortUrl;

    /**
     * 用户信息
     */
    private String user;

    /**
     * 浏览
     */
    private String browser;

    /**
     * 操作系统
     */
    private String os;

    /**
     * ip
     */
    private String ip;

    /**
     * 访问网络
     */
    private String network;

    /**
     * 访问设备
     */
    private String device;

    /**
     * 地区
     */
    private String locale;

    /**
     * 删除标识
     */
    private Integer delFlag;

    /**
     * 创建日期
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT) // 插入时自动填
    @JsonFormat(pattern = "YYYY-MM-dd HH:mm:ss")
    private Date createTime;

}
