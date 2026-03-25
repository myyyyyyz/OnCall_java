-- 初始化用户数据
-- 密码均为 123456（实际生产环境请使用加密存储，如 BCrypt）

-- 管理员用户：熊大
INSERT INTO user (username, password, phone, user_type, create_time, update_time)
VALUES ('熊大', '123456', '13800138001', 'admin', NOW(), NOW());

-- 操作员用户：熊二
INSERT INTO user (username, password, phone, user_type, create_time, update_time)
VALUES ('熊二', '123456', '13800138002', 'operator', NOW(), NOW());

-- 普通用户：光头强
INSERT INTO user (username, password, phone, user_type, create_time, update_time)
VALUES ('光头强', '123456', '13800138003', 'user', NOW(), NOW());

CREATE TABLE `security_log` (
                                `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
                                `user_id` BIGINT DEFAULT NULL COMMENT '操作用户ID',
                                `username` VARCHAR(50) DEFAULT NULL COMMENT '操作用户名',
                                `action` VARCHAR(50) NOT NULL COMMENT '操作类型（LOGIN/UPLOAD/...）',
                                `resource` VARCHAR(255) DEFAULT NULL COMMENT '操作资源（URL/文件路径）',
                                `risk_level` ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') NOT NULL COMMENT '风险等级',
                                `result` VARCHAR(10) NOT NULL COMMENT '操作结果（SUCCESS/FAIL）',
                                `ip_address` VARCHAR(50) DEFAULT NULL COMMENT '请求IP',
                                `user_agent` VARCHAR(512) DEFAULT NULL COMMENT '客户端信息',
                                `request_params` TEXT DEFAULT NULL COMMENT '请求参数（JSON）',
                                `error_message` TEXT DEFAULT NULL COMMENT '错误信息（如有）',
                                `timestamp` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
                                PRIMARY KEY (`id`),
                                KEY `idx_user_id` (`user_id`),
                                KEY `idx_action` (`action`),
                                KEY `idx_risk_level` (`risk_level`),
                                KEY `idx_timestamp` (`timestamp`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='安全日志表';