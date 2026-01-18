-- 校园跑腿服务系统数据库
-- 创建数据库
CREATE DATABASE IF NOT EXISTS campus_errand CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE campus_errand;

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
    real_name VARCHAR(50) COMMENT '真实姓名',
    nickname VARCHAR(50) COMMENT '昵称',
    avatar VARCHAR(500) COMMENT '头像URL',
    phone VARCHAR(20) UNIQUE COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    role VARCHAR(20) NOT NULL DEFAULT 'user' COMMENT '角色：user/runner/admin',
    is_verified TINYINT NOT NULL DEFAULT 0 COMMENT '是否认证：0-否 1-是',
    is_vip TINYINT NOT NULL DEFAULT 0 COMMENT '是否VIP：0-否 1-是',
    credit_score INT NOT NULL DEFAULT 100 COMMENT '信用分',
    college VARCHAR(100) COMMENT '学院',
    grade VARCHAR(20) COMMENT '年级',
    major VARCHAR(100) COMMENT '专业',
    gender VARCHAR(10) COMMENT '性别',
    signature VARCHAR(200) COMMENT '个性签名',
    register_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    last_login_time DATETIME COMMENT '最后登录时间',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '账号状态：0-禁用 1-正常',
    
    INDEX idx_role (role),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 跑腿员档案表
CREATE TABLE IF NOT EXISTS runner_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE COMMENT '用户ID',
    service_tags JSON COMMENT '服务标签（数组）',
    service_area VARCHAR(200) COMMENT '服务范围',
    work_time VARCHAR(100) COMMENT '工作时间（如：8:00-22:00）',
    complete_count INT NOT NULL DEFAULT 0 COMMENT '完成订单数',
    good_rate DECIMAL(5,2) NOT NULL DEFAULT 100.00 COMMENT '好评率（%）',
    rating_speed DECIMAL(3,2) NOT NULL DEFAULT 0.00 COMMENT '速度评分（1-5）',
    rating_attitude DECIMAL(3,2) NOT NULL DEFAULT 0.00 COMMENT '态度评分（1-5）',
    rating_quality DECIMAL(3,2) NOT NULL DEFAULT 0.00 COMMENT '质量评分（1-5）',
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_complete_count (complete_count)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='跑腿员档案表';

-- 用户评价表
CREATE TABLE IF NOT EXISTS user_reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reviewer_id BIGINT NOT NULL COMMENT '评价人ID',
    reviewee_id BIGINT NOT NULL COMMENT '被评价人ID',
    task_id BIGINT COMMENT '关联任务ID',
    rating DECIMAL(3,2) NOT NULL COMMENT '评分（1-5）',
    content TEXT COMMENT '评价内容',
    tags JSON COMMENT '标签（如：["速度快","态度好"]）',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评价时间',
    
    FOREIGN KEY (reviewer_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (reviewee_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_reviewer_id (reviewer_id),
    INDEX idx_reviewee_id (reviewee_id),
    INDEX idx_task_id (task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户评价表';

-- 跑腿任务表
CREATE TABLE IF NOT EXISTS errand_tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_no VARCHAR(50) NOT NULL UNIQUE COMMENT '任务编号',
    type VARCHAR(20) NOT NULL COMMENT '任务类型：delivery/food/shopping/queue/document/other',
    title VARCHAR(100) NOT NULL COMMENT '任务标题',
    description TEXT COMMENT '任务描述',
    publisher_id BIGINT NOT NULL COMMENT '发布人ID',
    runner_id BIGINT COMMENT '跑腿员ID',
    pickup_address VARCHAR(500) NOT NULL COMMENT '取货地址',
    pickup_detail VARCHAR(500) COMMENT '取货详细地址',
    pickup_lat DECIMAL(10,6) COMMENT '取货纬度',
    pickup_lng DECIMAL(10,6) COMMENT '取货经度',
    delivery_address VARCHAR(500) NOT NULL COMMENT '送达地址',
    delivery_detail VARCHAR(500) COMMENT '送达详细地址',
    delivery_lat DECIMAL(10,6) COMMENT '送达纬度',
    delivery_lng DECIMAL(10,6) COMMENT '送达经度',
    expected_time DATETIME NOT NULL COMMENT '期望送达时间',
    price DECIMAL(10,2) NOT NULL COMMENT '跑腿费用',
    contact_phone VARCHAR(20) NOT NULL COMMENT '联系电话',
    remark VARCHAR(500) COMMENT '备注信息',
    images JSON COMMENT '任务图片URL数组',
    status VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态：pending/accepted/delivering/completed/cancelled',
    publish_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    accept_time DATETIME COMMENT '接单时间',
    start_time DATETIME COMMENT '开始配送时间',
    complete_time DATETIME COMMENT '完成时间',
    cancel_time DATETIME COMMENT '取消时间',
    cancel_reason VARCHAR(500) COMMENT '取消原因',
    
    FOREIGN KEY (publisher_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (runner_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_publisher_id (publisher_id),
    INDEX idx_runner_id (runner_id),
    INDEX idx_status (status),
    INDEX idx_publish_time (publish_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='跑腿任务表';

-- 任务进度表
CREATE TABLE IF NOT EXISTS task_timelines (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT NOT NULL COMMENT '任务ID',
    step TINYINT NOT NULL COMMENT '步骤：1-已发布 2-已接单 3-配送中 4-已完成',
    title VARCHAR(50) NOT NULL COMMENT '步骤标题',
    description VARCHAR(500) COMMENT '步骤描述',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '时间',
    
    FOREIGN KEY (task_id) REFERENCES errand_tasks(id) ON DELETE CASCADE,
    INDEX idx_task_id (task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务进度表';

-- 失物招领物品表
CREATE TABLE IF NOT EXISTS items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    item_no VARCHAR(50) NOT NULL UNIQUE COMMENT '物品编号',
    type VARCHAR(20) NOT NULL COMMENT '类型：lost（失物）/found（招领）',
    item_name VARCHAR(100) NOT NULL COMMENT '物品名称',
    category VARCHAR(50) NOT NULL COMMENT '物品类别',
    description TEXT COMMENT '物品描述',
    lost_time DATETIME COMMENT '丢失/发现时间',
    location VARCHAR(500) NOT NULL COMMENT '丢失/发现地点',
    contact VARCHAR(100) NOT NULL COMMENT '联系方式（手机或邮箱）',
    publisher_id BIGINT NOT NULL COMMENT '发布人ID',
    images JSON COMMENT '物品图片URL数组',
    ai_description TEXT COMMENT 'AI生成的描述',
    status VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态：pending/approved/rejected/recovered',
    reviewer_id BIGINT COMMENT '审核人ID',
    review_time DATETIME COMMENT '审核时间',
    review_comment VARCHAR(500) COMMENT '审核意见',
    publish_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    
    FOREIGN KEY (publisher_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (reviewer_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_type (type),
    INDEX idx_publisher_id (publisher_id),
    INDEX idx_status (status),
    INDEX idx_category (category),
    INDEX idx_publish_time (publish_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='失物招领物品表';

-- 物品线索表
CREATE TABLE IF NOT EXISTS item_clues (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    item_id BIGINT NOT NULL COMMENT '物品ID',
    provider_id BIGINT NOT NULL COMMENT '线索提供人ID',
    content TEXT NOT NULL COMMENT '线索内容',
    contact VARCHAR(100) COMMENT '联系方式',
    status VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态：pending/useful/invalid',
    reviewer_id BIGINT COMMENT '审核人ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提供时间',
    
    FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE,
    FOREIGN KEY (provider_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (reviewer_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_item_id (item_id),
    INDEX idx_provider_id (provider_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物品线索表';

-- 通知表
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '接收人ID',
    type VARCHAR(20) NOT NULL COMMENT '类型：system/task/item/message',
    title VARCHAR(200) NOT NULL COMMENT '通知标题',
    content TEXT COMMENT '通知内容',
    related_id BIGINT COMMENT '关联对象ID',
    is_read TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读：0-否 1-是',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_is_read (is_read),
    INDEX idx_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知表';

-- 系统公告表
CREATE TABLE IF NOT EXISTS notices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL COMMENT '公告标题',
    content TEXT NOT NULL COMMENT '公告内容',
    priority TINYINT NOT NULL DEFAULT 0 COMMENT '优先级：0-普通 1-重要 2-紧急',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-下架 1-上架',
    publisher_id BIGINT NOT NULL COMMENT '发布人ID',
    view_count INT NOT NULL DEFAULT 0 COMMENT '浏览次数',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    FOREIGN KEY (publisher_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_status (status),
    INDEX idx_priority (priority),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统公告表';

-- 用户统计表
CREATE TABLE IF NOT EXISTS user_statistics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE COMMENT '用户ID',
    total_lost INT NOT NULL DEFAULT 0 COMMENT '发布失物总数',
    total_found INT NOT NULL DEFAULT 0 COMMENT '发布招领总数',
    recovered INT NOT NULL DEFAULT 0 COMMENT '已找回数量',
    publish_tasks INT NOT NULL DEFAULT 0 COMMENT '发布任务数',
    accept_tasks INT NOT NULL DEFAULT 0 COMMENT '接单任务数',
    complete_tasks INT NOT NULL DEFAULT 0 COMMENT '完成任务数',
    unread_notifications INT NOT NULL DEFAULT 0 COMMENT '未读通知数',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户统计表';

-- 聊天会话表
CREATE TABLE IF NOT EXISTS chats (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id1 BIGINT NOT NULL COMMENT '用户1 ID',
    user_id2 BIGINT NOT NULL COMMENT '用户2 ID',
    last_message VARCHAR(500) COMMENT '最后一条消息',
    last_message_time DATETIME COMMENT '最后消息时间',
    unread_count1 INT NOT NULL DEFAULT 0 COMMENT '用户1未读数',
    unread_count2 INT NOT NULL DEFAULT 0 COMMENT '用户2未读数',
    
    FOREIGN KEY (user_id1) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id2) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_users (user_id1, user_id2),
    INDEX idx_last_message_time (last_message_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天会话表';

-- 聊天消息表
CREATE TABLE IF NOT EXISTS chat_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    chat_id BIGINT NOT NULL COMMENT '会话ID',
    sender_id BIGINT NOT NULL COMMENT '发送人ID',
    content TEXT NOT NULL COMMENT '消息内容',
    message_type VARCHAR(20) NOT NULL DEFAULT 'text' COMMENT '类型：text/image/location',
    is_read TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    
    FOREIGN KEY (chat_id) REFERENCES chats(id) ON DELETE CASCADE,
    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_chat_id (chat_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天消息表';

-- 跑腿员申请表
CREATE TABLE IF NOT EXISTS runner_applications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE COMMENT '用户ID',
    service_area VARCHAR(200) COMMENT '服务范围',
    work_time VARCHAR(100) COMMENT '工作时间',
    service_tags JSON COMMENT '服务标签（数组）',
    introduction TEXT COMMENT '个人简介',
    student_id_photo VARCHAR(500) COMMENT '学生证照片URL',
    status VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态：pending/approved/rejected',
    reviewer_id BIGINT COMMENT '审核人ID',
    review_comment TEXT COMMENT '审核意见',
    apply_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    review_time DATETIME COMMENT '审核时间',
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (reviewer_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_status (status),
    INDEX idx_apply_time (apply_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='跑腿员申请表';

-- 插入初始数据

-- 1. 管理员账号
INSERT INTO users (username, password, real_name, nickname, role, status)
VALUES ('admin', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', '系统管理员', '管理员', 'admin', 1)
ON DUPLICATE KEY UPDATE username = username;

-- 2. 测试用户账号
INSERT INTO users (username, password, real_name, nickname, phone, role, status)
VALUES ('testuser', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', '测试用户', '用户1', '13800138001', 'user', 1)
ON DUPLICATE KEY UPDATE username = username;

-- 3. 测试跑腿员账号
INSERT INTO users (username, password, real_name, nickname, phone, role, is_verified, status)
VALUES ('runner', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', '测试跑腿员', '跑腿侠', '13800138002', 'runner', 1, 1)
ON DUPLICATE KEY UPDATE username = username;

-- 4. 系统公告
INSERT INTO notices (title, content, priority, status, publisher_id)
VALUES ('系统上线公告', '校园跑腿服务系统正式上线，欢迎使用！', 1, 1, 1)
ON DUPLICATE KEY UPDATE title = title;

-- 5. 为测试用户创建统计记录
INSERT INTO user_statistics (user_id)
VALUES (2), (3)
ON DUPLICATE KEY UPDATE user_id = user_id;

-- 6. 为跑腿员创建档案
INSERT INTO runner_profiles (user_id, service_tags, service_area, work_time)
VALUES (3, '["快递代取", "外卖代送", "物品购买"]', '校园内及周边2公里', '8:00-22:00')
ON DUPLICATE KEY UPDATE user_id = user_id;

-- 7. 插入测试任务
INSERT INTO errand_tasks (task_no, type, title, description, publisher_id, pickup_address, delivery_address, expected_time, price, contact_phone, status)
VALUES ('TASK20240101001', 'food', '外卖代取', '帮我取一份外卖，送到宿舍楼下', 2, '校门口外卖柜', '学生宿舍A栋楼下', NOW() + INTERVAL 30 MINUTE, 5.00, '13800138001', 'pending'),
       ('TASK20240101002', 'delivery', '快递代取', '帮我取一个快递，送到教学楼', 2, '快递驿站', '第一教学楼3楼', NOW() + INTERVAL 60 MINUTE, 3.00, '13800138001', 'pending')
ON DUPLICATE KEY UPDATE task_no = task_no;

-- 8. 插入任务进度
INSERT INTO task_timelines (task_id, step, title, description)
VALUES (1, 1, '任务已发布', '用户发布了外卖代取任务'),
       (2, 1, '任务已发布', '用户发布了快递代取任务')
ON DUPLICATE KEY UPDATE task_id = task_id;

-- 9. 插入测试通知
INSERT INTO notifications (user_id, type, title, content)
VALUES (2, 'system', '欢迎使用', '欢迎使用校园跑腿服务系统！'),
       (3, 'system', '欢迎加入', '欢迎成为校园跑腿员！')
ON DUPLICATE KEY UPDATE user_id = user_id;