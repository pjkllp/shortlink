CREATE TABLE IF NOT EXISTS `t_dlq_message` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `event_id` varchar(64) NOT NULL COMMENT '业务唯一ID（消息幂等ID）',
  `dlq_topic` varchar(128) NOT NULL COMMENT '死信队列主题，如 %DLQ%short-link-stats-consumer-group',
  `message_str` longtext NOT NULL COMMENT '死信原始消息体（JSON字符串）',
  `consumer_group` varchar(128) DEFAULT NULL COMMENT '消费组',
  `error_type` varchar(128) DEFAULT NULL COMMENT '异常类型',
  `error_message` varchar(1024) DEFAULT NULL COMMENT '异常信息',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '处理状态：0-待处理 1-已重放 2-已忽略',
  `retry_count` int NOT NULL DEFAULT 0 COMMENT '重放次数',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_event_id` (`event_id`),
  KEY `idx_dlq_topic` (`dlq_topic`),
  KEY `idx_status_create_time` (`status`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='死信消息归档表';
