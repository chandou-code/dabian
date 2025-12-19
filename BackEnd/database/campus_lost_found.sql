-- 校园失物招领系统数据库
-- 创建数据库
CREATE DATABASE IF NOT EXISTS campus_lost_found CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE campus_lost_found;

-- 用户表
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码（加密）',
    real_name VARCHAR(50) COMMENT '真实姓名',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    role ENUM('user', 'admin', 'reviewer') DEFAULT 'user' COMMENT '角色',
    avatar VARCHAR(500) COMMENT '头像URL',
    college VARCHAR(100) COMMENT '学院',
    grade VARCHAR(50) COMMENT '年级',
    status TINYINT DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_role (role),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 物品表
CREATE TABLE items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    item_name VARCHAR(100) NOT NULL COMMENT '物品名称',
    category VARCHAR(50) NOT NULL COMMENT '物品类别',
    description TEXT COMMENT '物品描述',
    images JSON COMMENT '物品图片URL数组',
    contact VARCHAR(500) COMMENT '联系方式',
    location VARCHAR(200) COMMENT '地点（丢失地点或发现地点）',
    event_time DATE COMMENT '事件时间（丢失时间或发现时间）',
    type ENUM('lost', 'found') NOT NULL COMMENT '类型：lost-失物，found-招领',
    status ENUM('pending', 'approved', 'rejected', 'claimed') DEFAULT 'pending' COMMENT '状态',
    submitter_id BIGINT NOT NULL COMMENT '发布者ID',
    reviewer_id BIGINT COMMENT '审核者ID',
    review_time TIMESTAMP NULL COMMENT '审核时间',
    review_reason TEXT COMMENT '审核原因',
    matched_item_id BIGINT COMMENT '匹配的物品ID',
    recovered_at TIMESTAMP NULL COMMENT '认领/找回时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    
    FOREIGN KEY (submitter_id) REFERENCES users(id),
    FOREIGN KEY (reviewer_id) REFERENCES users(id),
    FOREIGN KEY (matched_item_id) REFERENCES items(id),
    
    INDEX idx_type (type),
    INDEX idx_status (status),
    INDEX idx_category (category),
    INDEX idx_submitter_id (submitter_id),
    INDEX idx_event_time (event_time),
    INDEX idx_created_at (created_at),
    INDEX idx_location (location),
    FULLTEXT INDEX idx_search (item_name, description, location)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='失物招领物品表';

-- 通知表
CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '接收者ID',
    title VARCHAR(200) NOT NULL COMMENT '通知标题',
    content TEXT COMMENT '通知内容',
    type ENUM('approval', 'rejection', 'match', 'recovery', 'system') DEFAULT 'system' COMMENT '通知类型',
    related_item_id BIGINT COMMENT '相关物品ID',
    is_read TINYINT DEFAULT 0 COMMENT '是否已读：0-未读，1-已读',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (related_item_id) REFERENCES items(id),
    
    INDEX idx_user_id (user_id),
    INDEX idx_is_read (is_read),
    INDEX idx_type (type),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知表';

-- 匹配记录表
CREATE TABLE matches (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    lost_item_id BIGINT NOT NULL COMMENT '失物ID',
    found_item_id BIGINT NOT NULL COMMENT '招领ID',
    match_score DECIMAL(5,2) COMMENT '匹配分数（0-100）',
    similarity_details JSON COMMENT '相似度详情',
    status ENUM('pending', 'confirmed', 'rejected') DEFAULT 'pending' COMMENT '匹配状态',
    created_by BIGINT COMMENT '创建者ID（系统自动或手动创建）',
    confirmed_at TIMESTAMP NULL COMMENT '确认时间',
    confirmed_by BIGINT COMMENT '确认者ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    FOREIGN KEY (lost_item_id) REFERENCES items(id),
    FOREIGN KEY (found_item_id) REFERENCES items(id),
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (confirmed_by) REFERENCES users(id),
    
    INDEX idx_lost_item_id (lost_item_id),
    INDEX idx_found_item_id (found_item_id),
    INDEX idx_status (status),
    INDEX idx_match_score (match_score),
    UNIQUE INDEX uk_match_pair (lost_item_id, found_item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='匹配记录表';

-- 审核记录表
CREATE TABLE reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    item_id BIGINT NOT NULL COMMENT '物品ID',
    reviewer_id BIGINT NOT NULL COMMENT '审核者ID',
    action ENUM('approved', 'rejected') NOT NULL COMMENT '审核动作',
    reason TEXT COMMENT '审核原因',
    review_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '审核时间',
    
    FOREIGN KEY (item_id) REFERENCES items(id),
    FOREIGN KEY (reviewer_id) REFERENCES users(id),
    
    INDEX idx_item_id (item_id),
    INDEX idx_reviewer_id (reviewer_id),
    INDEX idx_action (action),
    INDEX idx_review_time (review_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审核记录表';

-- 系统配置表
CREATE TABLE system_configs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_key VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
    config_value TEXT COMMENT '配置值',
    description VARCHAR(500) COMMENT '配置描述',
    config_type ENUM('string', 'number', 'boolean', 'json') DEFAULT 'string' COMMENT '配置类型',
    is_public TINYINT DEFAULT 0 COMMENT '是否公开：0-私有，1-公开',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_config_key (config_key),
    INDEX idx_is_public (is_public)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- 公告表
CREATE TABLE announcements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL COMMENT '公告标题',
    content TEXT NOT NULL COMMENT '公告内容',
    status ENUM('active', 'inactive') DEFAULT 'active' COMMENT '状态',
    created_by BIGINT COMMENT '创建者ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    
    FOREIGN KEY (created_by) REFERENCES users(id),
    
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    INDEX idx_created_by (created_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公告表';

-- 文件上传记录表
CREATE TABLE file_uploads (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    original_name VARCHAR(500) NOT NULL COMMENT '原始文件名',
    file_name VARCHAR(500) NOT NULL COMMENT '存储文件名',
    file_path VARCHAR(1000) NOT NULL COMMENT '文件路径',
    file_size BIGINT COMMENT '文件大小（字节）',
    file_type VARCHAR(50) COMMENT '文件类型',
    mime_type VARCHAR(100) COMMENT 'MIME类型',
    upload_user_id BIGINT COMMENT '上传者ID',
    related_item_id BIGINT COMMENT '相关物品ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    FOREIGN KEY (upload_user_id) REFERENCES users(id),
    FOREIGN KEY (related_item_id) REFERENCES items(id),
    
    INDEX idx_upload_user_id (upload_user_id),
    INDEX idx_related_item_id (related_item_id),
    INDEX idx_created_at (created_at),
    INDEX idx_file_type (file_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件上传记录表';