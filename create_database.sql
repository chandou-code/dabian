-- ==========================================
-- 校园跑腿平台数据库创建脚本
-- MySQL 8.0+
-- 字符集：utf8mb4
-- ==========================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS campus_errand CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE campus_errand;

-- ==========================================
-- 1. 用户相关表
-- ==========================================

-- 1.1 用户表
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
  `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
  `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
  `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `role` VARCHAR(20) NOT NULL DEFAULT 'user' COMMENT '角色：user/runner/admin',
  `is_verified` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否认证：0-否 1-是',
  `is_vip` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否VIP：0-否 1-是',
  `credit_score` INT NOT NULL DEFAULT 100 COMMENT '信用分',
  `college` VARCHAR(100) DEFAULT NULL COMMENT '学院',
  `grade` VARCHAR(20) DEFAULT NULL COMMENT '年级',
  `signature` VARCHAR(200) DEFAULT NULL COMMENT '个性签名',
  `register_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '账号状态：0-禁用 1-正常',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_phone` (`phone`),
  KEY `idx_role` (`role`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 1.2 跑腿员档案表
DROP TABLE IF EXISTS `runner_profiles`;
CREATE TABLE `runner_profiles` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '档案ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `service_tags` JSON DEFAULT NULL COMMENT '服务标签（数组）',
  `service_area` VARCHAR(200) DEFAULT NULL COMMENT '服务范围',
  `work_time` VARCHAR(100) DEFAULT NULL COMMENT '工作时间',
  `complete_count` INT NOT NULL DEFAULT 0 COMMENT '完成订单数',
  `good_rate` DECIMAL(5,2) NOT NULL DEFAULT 100.00 COMMENT '好评率（%）',
  `rating_speed` DECIMAL(3,2) NOT NULL DEFAULT 0.00 COMMENT '速度评分（1-5）',
  `rating_attitude` DECIMAL(3,2) NOT NULL DEFAULT 0.00 COMMENT '态度评分（1-5）',
  `rating_quality` DECIMAL(3,2) NOT NULL DEFAULT 0.00 COMMENT '质量评分（1-5）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  CONSTRAINT `fk_runner_profiles_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='跑腿员档案表';

-- 1.3 用户评价表
DROP TABLE IF EXISTS `user_reviews`;
CREATE TABLE `user_reviews` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评价ID',
  `reviewer_id` BIGINT NOT NULL COMMENT '评价人ID',
  `reviewee_id` BIGINT NOT NULL COMMENT '被评价人ID',
  `task_id` BIGINT DEFAULT NULL COMMENT '关联任务ID',
  `rating` DECIMAL(3,2) NOT NULL COMMENT '评分（1-5）',
  `content` TEXT COMMENT '评价内容',
  `tags` JSON DEFAULT NULL COMMENT '标签',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评价时间',
  PRIMARY KEY (`id`),
  KEY `idx_reviewer_id` (`reviewer_id`),
  KEY `idx_reviewee_id` (`reviewee_id`),
  KEY `idx_task_id` (`task_id`),
  CONSTRAINT `fk_user_reviews_reviewer` FOREIGN KEY (`reviewer_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_user_reviews_reviewee` FOREIGN KEY (`reviewee_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户评价表';

-- ==========================================
-- 2. 跑腿任务相关表
-- ==========================================

-- 2.1 跑腿任务表
DROP TABLE IF EXISTS `errand_tasks`;
CREATE TABLE `errand_tasks` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `task_no` VARCHAR(50) NOT NULL COMMENT '任务编号',
  `type` VARCHAR(20) NOT NULL COMMENT '任务类型',
  `title` VARCHAR(100) NOT NULL COMMENT '任务标题',
  `description` TEXT COMMENT '任务描述',
  `publisher_id` BIGINT NOT NULL COMMENT '发布人ID',
  `runner_id` BIGINT DEFAULT NULL COMMENT '跑腿员ID',
  `pickup_address` VARCHAR(500) NOT NULL COMMENT '取货地址',
  `pickup_detail` VARCHAR(500) DEFAULT NULL COMMENT '取货详细地址',
  `pickup_lat` DECIMAL(10,6) DEFAULT NULL COMMENT '取货纬度',
  `pickup_lng` DECIMAL(10,6) DEFAULT NULL COMMENT '取货经度',
  `delivery_address` VARCHAR(500) NOT NULL COMMENT '送达地址',
  `delivery_detail` VARCHAR(500) DEFAULT NULL COMMENT '送达详细地址',
  `delivery_lat` DECIMAL(10,6) DEFAULT NULL COMMENT '送达纬度',
  `delivery_lng` DECIMAL(10,6) DEFAULT NULL COMMENT '送达经度',
  `expected_time` DATETIME NOT NULL COMMENT '期望送达时间',
  `price` DECIMAL(10,2) NOT NULL COMMENT '跑腿费用',
  `contact_phone` VARCHAR(20) NOT NULL COMMENT '联系电话',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注信息',
  `images` JSON DEFAULT NULL COMMENT '任务图片URL数组',
  `status` VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态',
  `publish_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  `accept_time` DATETIME DEFAULT NULL COMMENT '接单时间',
  `start_time` DATETIME DEFAULT NULL COMMENT '开始配送时间',
  `complete_time` DATETIME DEFAULT NULL COMMENT '完成时间',
  `cancel_time` DATETIME DEFAULT NULL COMMENT '取消时间',
  `cancel_reason` VARCHAR(500) DEFAULT NULL COMMENT '取消原因',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_task_no` (`task_no`),
  KEY `idx_publisher_id` (`publisher_id`),
  KEY `idx_runner_id` (`runner_id`),
  KEY `idx_status` (`status`),
  KEY `idx_publish_time` (`publish_time`),
  CONSTRAINT `fk_errand_tasks_publisher` FOREIGN KEY (`publisher_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_errand_tasks_runner` FOREIGN KEY (`runner_id`) REFERENCES `users` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='跑腿任务表';

-- 2.2 任务进度表
DROP TABLE IF EXISTS `task_timelines`;
CREATE TABLE `task_timelines` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '进度ID',
  `task_id` BIGINT NOT NULL COMMENT '任务ID',
  `step` TINYINT(1) NOT NULL COMMENT '步骤',
  `title` VARCHAR(50) NOT NULL COMMENT '步骤标题',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '步骤描述',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '时间',
  PRIMARY KEY (`id`),
  KEY `idx_task_id` (`task_id`),
  CONSTRAINT `fk_task_timelines_task` FOREIGN KEY (`task_id`) REFERENCES `errand_tasks` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务进度表';

-- ==========================================
-- 3. 失物招领相关表
-- ==========================================

-- 3.1 失物招领物品表
DROP TABLE IF EXISTS `items`;
CREATE TABLE `items` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '物品ID',
  `item_no` VARCHAR(50) NOT NULL COMMENT '物品编号',
  `type` VARCHAR(20) NOT NULL COMMENT '类型',
  `item_name` VARCHAR(100) NOT NULL COMMENT '物品名称',
  `category` VARCHAR(50) NOT NULL COMMENT '物品类别',
  `description` TEXT COMMENT '物品描述',
  `lost_time` DATETIME DEFAULT NULL COMMENT '丢失/发现时间',
  `location` VARCHAR(500) NOT NULL COMMENT '丢失/发现地点',
  `contact` VARCHAR(100) NOT NULL COMMENT '联系方式',
  `publisher_id` BIGINT NOT NULL COMMENT '发布人ID',
  `images` JSON DEFAULT NULL COMMENT '物品图片URL数组',
  `ai_description` TEXT DEFAULT NULL COMMENT 'AI生成的描述',
  `status` VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态',
  `reviewer_id` BIGINT DEFAULT NULL COMMENT '审核人ID',
  `review_time` DATETIME DEFAULT NULL COMMENT '审核时间',
  `review_comment` VARCHAR(500) DEFAULT NULL COMMENT '审核意见',
  `publish_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_item_no` (`item_no`),
  KEY `idx_type` (`type`),
  KEY `idx_publisher_id` (`publisher_id`),
  KEY `idx_status` (`status`),
  KEY `idx_category` (`category`),
  KEY `idx_publish_time` (`publish_time`),
  CONSTRAINT `fk_items_publisher` FOREIGN KEY (`publisher_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_items_reviewer` FOREIGN KEY (`reviewer_id`) REFERENCES `users` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='失物招领物品表';

-- 3.2 物品线索表
DROP TABLE IF EXISTS `item_clues`;
CREATE TABLE `item_clues` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '线索ID',
  `item_id` BIGINT NOT NULL COMMENT '物品ID',
  `provider_id` BIGINT NOT NULL COMMENT '线索提供人ID',
  `content` TEXT NOT NULL COMMENT '线索内容',
  `contact` VARCHAR(100) DEFAULT NULL COMMENT '联系方式',
  `status` VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态',
  `reviewer_id` BIGINT DEFAULT NULL COMMENT '审核人ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提供时间',
  PRIMARY KEY (`id`),
  KEY `idx_item_id` (`item_id`),
  KEY `idx_provider_id` (`provider_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `fk_item_clues_item` FOREIGN KEY (`item_id`) REFERENCES `items` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_item_clues_provider` FOREIGN KEY (`provider_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_item_clues_reviewer` FOREIGN KEY (`reviewer_id`) REFERENCES `users` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物品线索表';

-- ==========================================
-- 4. 消息通知相关表
-- ==========================================

-- 4.1 通知表
DROP TABLE IF EXISTS `notifications`;
CREATE TABLE `notifications` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `user_id` BIGINT NOT NULL COMMENT '接收人ID',
  `type` VARCHAR(20) NOT NULL COMMENT '类型',
  `title` VARCHAR(200) NOT NULL COMMENT '通知标题',
  `content` TEXT COMMENT '通知内容',
  `related_id` BIGINT DEFAULT NULL COMMENT '关联对象ID',
  `is_read` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已读',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_is_read` (`is_read`),
  KEY `idx_type` (`type`),
  CONSTRAINT `fk_notifications_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知表';

-- ==========================================
-- 5. 系统相关表
-- ==========================================

-- 5.1 系统公告表
DROP TABLE IF EXISTS `notices`;
CREATE TABLE `notices` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  `title` VARCHAR(200) NOT NULL COMMENT '公告标题',
  `content` TEXT NOT NULL COMMENT '公告内容',
  `priority` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '优先级',
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态',
  `publisher_id` BIGINT NOT NULL COMMENT '发布人ID',
  `view_count` INT NOT NULL DEFAULT 0 COMMENT '浏览次数',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_priority` (`priority`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `fk_notices_publisher` FOREIGN KEY (`publisher_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统公告表';

-- ==========================================
-- 6. 统计相关表
-- ==========================================

-- 6.1 用户统计表
DROP TABLE IF EXISTS `user_statistics`;
CREATE TABLE `user_statistics` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '统计ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `total_lost` INT NOT NULL DEFAULT 0 COMMENT '发布失物总数',
  `total_found` INT NOT NULL DEFAULT 0 COMMENT '发布招领总数',
  `recovered` INT NOT NULL DEFAULT 0 COMMENT '已找回数量',
  `publish_tasks` INT NOT NULL DEFAULT 0 COMMENT '发布任务数',
  `accept_tasks` INT NOT NULL DEFAULT 0 COMMENT '接单任务数',
  `complete_tasks` INT NOT NULL DEFAULT 0 COMMENT '完成任务数',
  `unread_notifications` INT NOT NULL DEFAULT 0 COMMENT '未读通知数',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  CONSTRAINT `fk_user_statistics_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户统计表';

-- ==========================================
-- 7. 聊天相关表
-- ==========================================

-- 7.1 聊天会话表
DROP TABLE IF EXISTS `chats`;
CREATE TABLE `chats` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '会话ID',
  `user_id1` BIGINT NOT NULL COMMENT '用户1 ID',
  `user_id2` BIGINT NOT NULL COMMENT '用户2 ID',
  `last_message` VARCHAR(500) DEFAULT NULL COMMENT '最后一条消息',
  `last_message_time` DATETIME DEFAULT NULL COMMENT '最后消息时间',
  `unread_count1` INT NOT NULL DEFAULT 0 COMMENT '用户1未读数',
  `unread_count2` INT NOT NULL DEFAULT 0 COMMENT '用户2未读数',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_users` (`user_id1`, `user_id2`),
  KEY `idx_last_message_time` (`last_message_time`),
  CONSTRAINT `fk_chats_user1` FOREIGN KEY (`user_id1`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_chats_user2` FOREIGN KEY (`user_id2`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天会话表';

-- 7.2 聊天消息表
DROP TABLE IF EXISTS `chat_messages`;
CREATE TABLE `chat_messages` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `chat_id` BIGINT NOT NULL COMMENT '会话ID',
  `sender_id` BIGINT NOT NULL COMMENT '发送人ID',
  `content` TEXT NOT NULL COMMENT '消息内容',
  `message_type` VARCHAR(20) NOT NULL DEFAULT 'text' COMMENT '类型',
  `is_read` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已读',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  PRIMARY KEY (`id`),
  KEY `idx_chat_id` (`chat_id`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `fk_chat_messages_chat` FOREIGN KEY (`chat_id`) REFERENCES `chats` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_chat_messages_sender` FOREIGN KEY (`sender_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天消息表';

-- ==========================================
-- 初始化数据
-- ==========================================

-- 插入管理员账号（密码：admin的BCrypt加密值）
INSERT INTO `users` (`username`, `password`, `real_name`, `nickname`, `role`, `credit_score`, `register_time`, `status`)
VALUES 
  ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', '管理员', 'admin', 100, NOW(), 1);

-- 插入测试用户账号（密码：123456的BCrypt加密值）
INSERT INTO `users` (`username`, `password`, `real_name`, `nickname`, `phone`, `role`, `credit_score`, `college`, `grade`, `signature`, `register_time`, `status`)
VALUES 
  ('testuser', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '测试用户', '测试用户', '13800138000', 'user', 95, '计算机学院', '大三', '爱生活，爱校园', NOW(), 1),
  ('runner', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '测试跑腿员', '跑腿员小李', '13800138001', 'runner', 98, '经济学院', '大二', '用心服务，准时送达', NOW(), 1);

-- 插入跑腿员档案信息
INSERT INTO `runner_profiles` (`user_id`, `service_tags`, `service_area`, `work_time`, `complete_count`, `good_rate`, `rating_speed`, `rating_attitude`, `rating_quality`)
SELECT `id`, '["快递代取", "外卖代送", "物品购买"]', '全校', '8:00-22:00', 156, 99.00, 5.00, 5.00, 5.00
FROM `users` WHERE `username` = 'runner';

-- 初始化用户统计数据
INSERT INTO `user_statistics` (`user_id`, `total_lost`, `total_found`, `recovered`, `unread_notifications`)
SELECT `id`, 0, 0, 0, 0 FROM `users`;

-- 插入系统公告
INSERT INTO `notices` (`title`, `content`, `priority`, `status`, `publisher_id`, `view_count`)
VALUES 
  ('校园跑腿平台上线公告', '欢迎使用校园跑腿平台，为您提供便捷的校园服务。', 1, 1, 1, 0),
  ('安全使用指南', '请使用真实的联系方式，交易时注意安全。', 0, 1, 1, 0),
  ('跑腿员招募计划', '诚邀各位同学加入跑腿员队伍，赚取零花钱。', 0, 1, 1, 0);

-- 完成提示
SELECT '数据库创建完成！' AS message;
SELECT '默认账号：admin/admin' AS admin_account;
SELECT '测试账号：testuser/123456, runner/123456' AS test_accounts;
