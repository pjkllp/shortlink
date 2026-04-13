package com.nageoffer.shortlink.project.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 网关访问日志实体类
 * 对应 AccessLogGatewayFilter 中采集的字段
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_access_log")
public class AccessLogDO {

    /**
     * 请求ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 客户端IP
     */
    private String ip;

    /**
     * 请求路径
     */
    private String path;

    /**
     * 请求方法（GET/POST等）
     */
    private String method;

    /**
     * 浏览器类型
     */
    private String browser;

    /**
     * 设备类型
     */
    private String device;

    /**
     * 网络类型（WiFi/4G/5G等）
     */
    private String network;

    /**
     * 操作系统
     */
    private String os;

    /**
     * 访问时间（入库为时刻；接口 JSON 固定东八区字符串，与 MyBatis 映射无关）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date accessTime;

    /** 
     * 省份
    */
    private String province;

    /** 
     * 城市
     */
    private String city;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT) // 插入时自动填充
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 业务id，防止幂等
     */
    private String eventId;
}
