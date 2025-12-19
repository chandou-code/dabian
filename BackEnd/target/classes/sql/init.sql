-- 创建数据库
CREATE DATABASE IF NOT EXISTS campus_lost_found DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE campus_lost_found;

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    real_name VARCHAR(50) COMMENT '真实姓名',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    gender TINYINT DEFAULT 0 COMMENT '性别：0-未知，1-男，2-女',
    avatar VARCHAR(255) COMMENT '头像URL',
    college VARCHAR(100) COMMENT '学院',
    grade VARCHAR(20) COMMENT '年级',
    major VARCHAR(100) COMMENT '专业',
    role VARCHAR(20) DEFAULT 'user' COMMENT '用户角色：user-普通用户，reviewer-审核员，admin-管理员',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    last_login_time DATETIME COMMENT '最后登录时间',
    last_login_ip VARCHAR(50) COMMENT '最后登录IP',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    INDEX idx_username (username),
    INDEX idx_role (role),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 失物招领物品表
CREATE TABLE IF NOT EXISTS lost_found_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '物品ID',
    title VARCHAR(100) NOT NULL COMMENT '标题',
    description TEXT COMMENT '描述',
    item_name VARCHAR(100) NOT NULL COMMENT '物品名称',
    category VARCHAR(50) NOT NULL COMMENT '分类',
    type VARCHAR(10) NOT NULL COMMENT '物品类型：lost-失物，found-招领',
    item_time DATETIME NOT NULL COMMENT '丢失/发现时间',
    location VARCHAR(200) NOT NULL COMMENT '丢失/发现地点',
    location_detail VARCHAR(255) COMMENT '详细地点描述',
    images TEXT COMMENT '图片URL列表，JSON格式',
    contact VARCHAR(100) NOT NULL COMMENT '联系方式',
    pickup_location VARCHAR(200) COMMENT '领取地点（招领物品专用）',
    status VARCHAR(20) DEFAULT 'pending' COMMENT '状态：pending-待审核，approved-已通过，rejected-已驳回，found-已找到，recovered-已领回',
    submitter_id BIGINT NOT NULL COMMENT '发布者ID',
    submitter_name VARCHAR(50) COMMENT '发布者姓名',
    reviewer_id BIGINT COMMENT '审核者ID',
    review_time DATETIME COMMENT '审核时间',
    review_comment TEXT COMMENT '审核意见',
    view_count INT DEFAULT 0 COMMENT '浏览次数',
    matched_item_id BIGINT COMMENT '匹配到的物品ID',
    matched_time DATETIME COMMENT '匹配时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    INDEX idx_type (type),
    INDEX idx_status (status),
    INDEX idx_category (category),
    INDEX idx_submitter_id (submitter_id),
    INDEX idx_create_time (create_time),
    INDEX idx_item_time (item_time),
    FOREIGN KEY (submitter_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    FOREIGN KEY (reviewer_id) REFERENCES sys_user(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='失物招领物品表';

-- 审核记录表
CREATE TABLE IF NOT EXISTS review_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
    item_id BIGINT NOT NULL COMMENT '物品ID',
    reviewer_id BIGINT NOT NULL COMMENT '审核者ID',
    reviewer_name VARCHAR(50) COMMENT '审核者姓名',
    action VARCHAR(20) NOT NULL COMMENT '审核动作：approve-通过，reject-驳回',
    comment TEXT COMMENT '审核意见',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted TINYINT DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    INDEX idx_item_id (item_id),
    INDEX idx_reviewer_id (reviewer_id),
    INDEX idx_create_time (create_time),
    FOREIGN KEY (item_id) REFERENCES lost_found_item(id) ON DELETE CASCADE,
    FOREIGN KEY (reviewer_id) REFERENCES sys_user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审核记录表';

-- 通知表
CREATE TABLE IF NOT EXISTS notification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '通知ID',
    user_id BIGINT NOT NULL COMMENT '接收者ID',
    title VARCHAR(100) NOT NULL COMMENT '标题',
    content TEXT NOT NULL COMMENT '内容',
    type VARCHAR(20) DEFAULT 'system' COMMENT '通知类型：system-系统通知，review-审核通知，found-找到通知',
    is_read TINYINT DEFAULT 0 COMMENT '是否已读：0-未读，1-已读',
    related_item_id BIGINT COMMENT '关联的物品ID（可选）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    read_time DATETIME COMMENT '阅读时间',
    deleted TINYINT DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    INDEX idx_user_id (user_id),
    INDEX idx_type (type),
    INDEX idx_is_read (is_read),
    INDEX idx_create_time (create_time),
    FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    FOREIGN KEY (related_item_id) REFERENCES lost_found_item(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知表';

-- 系统配置表
CREATE TABLE IF NOT EXISTS system_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '配置ID',
    config_key VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
    config_value TEXT COMMENT '配置值',
    description VARCHAR(255) COMMENT '配置描述',
    type VARCHAR(20) DEFAULT 'string' COMMENT '配置类型：string-字符串，number-数字，boolean-布尔值，json-JSON对象',
    is_system TINYINT DEFAULT 0 COMMENT '是否系统配置：0-否，1-是',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    INDEX idx_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- 插入初始数据

-- 插入默认管理员用户
INSERT INTO sys_user (username, password, real_name, email, role, status) 
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', '系统管理员', 'admin@campus.edu', 'admin', 1)
ON DUPLICATE KEY UPDATE id = id;

-- 插入默认审核员用户
INSERT INTO sys_user (username, password, real_name, email, role, status) 
VALUES ('reviewer', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', '审核员', 'reviewer@campus.edu', 'reviewer', 1)
ON DUPLICATE KEY UPDATE id = id;

-- 插入测试用户
INSERT INTO sys_user (username, password, real_name, email, phone, gender, college, grade, major, role, status) 
VALUES ('testuser', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', '张三', 'zhangsan@student.edu', '13800138001', 1, '计算机学院', '2021级', '软件工程', 'user', 1)
ON DUPLICATE KEY UPDATE id = id;

-- 插入系统配置
INSERT INTO system_config (config_key, config_value, description, type, is_system) VALUES
('categories', '["证件类","电子设备","生活用品","学习用品","服装配饰","其他"]', '物品分类配置', 'json', 1),
('locations', '["教学楼","图书馆","食堂","宿舍","体育馆","实验室","校园道路","其他"]', '地点分类配置', 'json', 1),
('site_name', '校园失物招领系统', '网站名称', 'string', 1),
('max_image_count', '6', '最大图片数量', 'number', 1),
('upload_image_size', '10485760', '上传图片大小限制（字节）', 'number', 1)
ON DUPLICATE KEY UPDATE id = id;

-- 插入示例失物招领数据
INSERT INTO lost_found_item (title, item_name, category, type, item_time, location, location_detail, description, contact, status, submitter_id, submitter_name) VALUES
('丢失黑色钱包', '黑色钱包', '证件类', 'lost', '2024-01-15 14:30:00', '图书馆二楼', '阅览区座位上', '黑色皮质钱包，内有身份证、银行卡和少量现金', '13800138001', 'approved', 3, '张三'),
('发现蓝色水杯', '蓝色保温杯', '生活用品', 'found', '2024-01-16 09:15:00', '教学楼三楼', '卫生间洗手台', '蓝色保温杯，品牌不详，较新', '13800138001', 'approved', 3, '张三'),
('丢失AirPods Pro', 'AirPods Pro', '电子设备', 'lost', '2024-01-17 16:45:00', '食堂', '一楼大厅', '白色降噪耳机，充电盒上有轻微划痕', '13800138002', 'pending', 3, '张三')
ON DUPLICATE KEY UPDATE id = id;