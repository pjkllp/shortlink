-- 用户表增加管理员标识：1=管理员，0=普通用户（注册默认 0）
ALTER TABLE `t_user_0`
    ADD COLUMN `is_admin` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否管理员 1=是 0=否' AFTER `del_flag`;

ALTER TABLE `t_user_1`
    ADD COLUMN `is_admin` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否管理员 1=是 0=否' AFTER `del_flag`;

ALTER TABLE `t_user_2`
    ADD COLUMN `is_admin` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否管理员 1=是 0=否' AFTER `del_flag`;

ALTER TABLE `t_user_3`
    ADD COLUMN `is_admin` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否管理员 1=是 0=否' AFTER `del_flag`;

ALTER TABLE `t_user_4`
    ADD COLUMN `is_admin` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否管理员 1=是 0=否' AFTER `del_flag`;





