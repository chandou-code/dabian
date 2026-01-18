-- 创建数据库
CREATE DATABASE IF NOT EXISTS campus_errand CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE campus_errand;

-- 创建用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    real_name VARCHAR(50) NULL,
    nickname VARCHAR(50) NULL,
    avatar VARCHAR(500) NULL,
    phone VARCHAR(20) NULL,
    email VARCHAR(100) NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'user',
    is_verified TINYINT(1) NOT NULL DEFAULT 0,
    is_vip TINYINT(1) NOT NULL DEFAULT 0,
    credit_score INT NOT NULL DEFAULT 100,
    college VARCHAR(100) NULL,
    grade VARCHAR(20) NULL,
    signature VARCHAR(200) NULL,
    register_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login_time DATETIME NULL,
    status TINYINT(1) NOT NULL DEFAULT 1,
    UNIQUE KEY uk_username (username),
    UNIQUE KEY uk_phone (phone),
    KEY idx_role (role),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建跑腿员档案表
CREATE TABLE IF NOT EXISTS runner_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    service_tags JSON NULL,
    service_area VARCHAR(200) NULL,
    work_time VARCHAR(100) NULL,
    complete_count INT NOT NULL DEFAULT 0,
    good_rate DECIMAL(5,2) NOT NULL DEFAULT 100.00,
    rating_speed DECIMAL(3,2) NOT NULL DEFAULT 0.00,
    rating_attitude DECIMAL(3,2) NOT NULL DEFAULT 0.00,
    rating_quality DECIMAL(3,2) NOT NULL DEFAULT 0.00,
    UNIQUE KEY uk_user_id (user_id),
    KEY idx_complete_count (complete_count),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建用户评价表
CREATE TABLE IF NOT EXISTS user_reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reviewer_id BIGINT NOT NULL,
    reviewee_id BIGINT NOT NULL,
    task_id BIGINT NULL,
    rating DECIMAL(3,2) NOT NULL,
    content TEXT NULL,
    tags JSON NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_reviewer_id (reviewer_id),
    KEY idx_reviewee_id (reviewee_id),
    KEY idx_task_id (task_id),
    FOREIGN KEY (reviewer_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (reviewee_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建跑腿任务表
CREATE TABLE IF NOT EXISTS errand_tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_no VARCHAR(50) NOT NULL,
    type VARCHAR(20) NOT NULL,
    title VARCHAR(100) NOT NULL,
    description TEXT NULL,
    publisher_id BIGINT NOT NULL,
    runner_id BIGINT NULL,
    pickup_address VARCHAR(500) NOT NULL,
    pickup_detail VARCHAR(500) NULL,
    pickup_lat DECIMAL(10,6) NULL,
    pickup_lng DECIMAL(10,6) NULL,
    delivery_address VARCHAR(500) NOT NULL,
    delivery_detail VARCHAR(500) NULL,
    delivery_lat DECIMAL(10,6) NULL,
    delivery_lng DECIMAL(10,6) NULL,
    expected_time DATETIME NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    contact_phone VARCHAR(20) NOT NULL,
    remark VARCHAR(500) NULL,
    images JSON NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'pending',
    publish_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    accept_time DATETIME NULL,
    start_time DATETIME NULL,
    complete_time DATETIME NULL,
    cancel_time DATETIME NULL,
    cancel_reason VARCHAR(500) NULL,
    UNIQUE KEY uk_task_no (task_no),
    KEY idx_publisher_id (publisher_id),
    KEY idx_runner_id (runner_id),
    KEY idx_status (status),
    KEY idx_publish_time (publish_time),
    FOREIGN KEY (publisher_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (runner_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建任务进度表
CREATE TABLE IF NOT EXISTS task_timelines (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT NOT NULL,
    step TINYINT(1) NOT NULL,
    title VARCHAR(50) NOT NULL,
    description VARCHAR(500) NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_task_id (task_id),
    FOREIGN KEY (task_id) REFERENCES errand_tasks(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建失物招领物品表
CREATE TABLE IF NOT EXISTS items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    item_no VARCHAR(50) NOT NULL,
    type VARCHAR(20) NOT NULL,
    item_name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    description TEXT NULL,
    lost_time DATETIME NULL,
    location VARCHAR(500) NOT NULL,
    contact VARCHAR(100) NOT NULL,
    publisher_id BIGINT NOT NULL,
    images JSON NULL,
    ai_description TEXT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'pending',
    reviewer_id BIGINT NULL,
    review_time DATETIME NULL,
    review_comment VARCHAR(500) NULL,
    publish_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_item_no (item_no),
    KEY idx_type (type),
    KEY idx_publisher_id (publisher_id),
    KEY idx_status (status),
    KEY idx_category (category),
    KEY idx_publish_time (publish_time),
    FOREIGN KEY (publisher_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (reviewer_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建物品线索表
CREATE TABLE IF NOT EXISTS item_clues (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    item_id BIGINT NOT NULL,
    provider_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    contact VARCHAR(100) NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'pending',
    reviewer_id BIGINT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_item_id (item_id),
    KEY idx_provider_id (provider_id),
    KEY idx_status (status),
    FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE,
    FOREIGN KEY (provider_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (reviewer_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建通知表
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT NULL,
    related_id BIGINT NULL,
    is_read TINYINT(1) NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_user_id (user_id),
    KEY idx_is_read (is_read),
    KEY idx_type (type),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建系统公告表
CREATE TABLE IF NOT EXISTS notices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    priority TINYINT(1) NOT NULL DEFAULT 0,
    status TINYINT(1) NOT NULL DEFAULT 1,
    publisher_id BIGINT NOT NULL,
    view_count INT NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_status (status),
    KEY idx_priority (priority),
    KEY idx_create_time (create_time),
    FOREIGN KEY (publisher_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建用户统计表
CREATE TABLE IF NOT EXISTS user_statistics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    total_lost INT NOT NULL DEFAULT 0,
    total_found INT NOT NULL DEFAULT 0,
    recovered INT NOT NULL DEFAULT 0,
    publish_tasks INT NOT NULL DEFAULT 0,
    accept_tasks INT NOT NULL DEFAULT 0,
    complete_tasks INT NOT NULL DEFAULT 0,
    unread_notifications INT NOT NULL DEFAULT 0,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_id (user_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建聊天会话表
CREATE TABLE IF NOT EXISTS chats (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id1 BIGINT NOT NULL,
    user_id2 BIGINT NOT NULL,
    last_message VARCHAR(500) NULL,
    last_message_time DATETIME NULL,
    unread_count1 INT NOT NULL DEFAULT 0,
    unread_count2 INT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_users (user_id1, user_id2),
    KEY idx_last_message_time (last_message_time),
    FOREIGN KEY (user_id1) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id2) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建聊天消息表
CREATE TABLE IF NOT EXISTS chat_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    chat_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    message_type VARCHAR(20) NOT NULL DEFAULT 'text',
    is_read TINYINT(1) NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_chat_id (chat_id),
    KEY idx_create_time (create_time),
    FOREIGN KEY (chat_id) REFERENCES chats(id) ON DELETE CASCADE,
    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 插入初始数据
-- 管理员账号
INSERT INTO users (username, password, real_name, nickname, role, status) 
VALUES ('admin', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', '管理员', '管理员', 'admin', 1)
ON DUPLICATE KEY UPDATE username = username;

-- 测试用户账号
INSERT INTO users (username, password, real_name, nickname, role, status) 
VALUES ('testuser', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', '测试用户', '测试用户', 'user', 1)
ON DUPLICATE KEY UPDATE username = username;

-- 测试跑腿员账号
INSERT INTO users (username, password, real_name, nickname, role, status) 
VALUES ('runner', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', '测试跑腿员', '测试跑腿员', 'runner', 1)
ON DUPLICATE KEY UPDATE username = username;