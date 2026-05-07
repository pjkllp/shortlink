package com.nageoffer.shortlink.project.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 死信消息归档实体
 */
@Data
@TableName("t_dlq_message")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DlqMessageDO {

    /**
     * 自增主键
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 业务唯一ID（消息幂等ID）
     */
    private String eventId;

    /**
     * 死信队列主题
     */
    private String dlqTopic;

    /**
     * 死信原始消息体（JSON字符串）
     */
    private String messageStr;

    /**
     * 消费组
     */
    private String consumerGroup;

    /**
     * 异常类型
     */
    private String errorType;

    /**
     * 异常信息
     */
    private String errorMessage;

    /**
     * 处理状态：0-待处理 1-已重放 2-已忽略
     */
    private Integer status;

    /**
     * 重放次数
     */
    private Integer retryCount;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT) // 插入时自动填
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE) // 插入/更新时自动填充
    private Date updateTime;
}
