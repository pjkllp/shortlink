CREATE TABLE IF NOT EXISTS `t_access_log` (
    `id`          BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `event_id`    VARCHAR(64)  NOT NULL COMMENT '业务事件ID',
    `ip`          VARCHAR(64)  NOT NULL COMMENT '访问IP',
    `path`        VARCHAR(512) NOT NULL COMMENT '请求路径',
    `method`      VARCHAR(10)  NOT NULL COMMENT '请求方法',
    `browser`     VARCHAR(64)  DEFAULT NULL COMMENT '浏览器',
    `device`      VARCHAR(32)  DEFAULT NULL COMMENT '设备类型',
    `network`     VARCHAR(32)  DEFAULT NULL COMMENT '网络类型',
    `os`          VARCHAR(32)  DEFAULT NULL COMMENT '操作系统',
    `province`    VARCHAR(64)  DEFAULT NULL COMMENT '省份',
    `city`        VARCHAR(64)  DEFAULT NULL COMMENT '城市',
    `access_time` DATETIME     NOT NULL COMMENT '访问时间',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_access_time_ip_province_city` (`access_time`, `ip`, `province`, `city`),
    KEY `idx_event_id` (`event_id`),
    KEY `idx_ip` (`ip`),
    KEY `idx_path` (`path`),
    KEY `idx_access_time` (`access_time`),
    KEY `idx_province` (`province`),
    KEY `idx_city` (`city`)
) ENGINE=InnoDB 
  DEFAULT CHARSET=utf8mb4 
  COLLATE=utf8mb4_unicode_ci 
  COMMENT='访问日志表';

-- 若表已存在且仍为旧唯一键 uk_access_time_ip，可手工执行：
# ALTER TABLE `t_access_log` DROP INDEX `uk_access_time_ip`;
# ALTER TABLE `t_access_log` ADD UNIQUE KEY `uk_access_time_ip_province_city` (`access_time`, `ip`, `province`, `city`);