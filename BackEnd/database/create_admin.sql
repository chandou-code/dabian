-- 创建管理员用户
USE campus_lost_found;

-- 检查admin用户是否存在，如果不存在则创建
INSERT IGNORE INTO users (username, password, real_name, email, phone, role, status, created_at, updated_at)
VALUES (
    'admin', 
    '$2a$10$v0xEy1Hm5s1QbZjX4BzgF9EpPl4t8kMhMaE/NKq2U1Zf', -- Admin123456 (BCrypt加密)
    '系统管理员', 
    'admin@campus.edu.cn', 
    '13800138000', 
    'admin', 
    1, 
    NOW(), 
    NOW()
);

-- 创建一些测试用户
INSERT IGNORE INTO users (username, password, real_name, email, phone, role, status, created_at, updated_at)
VALUES 
(
    'testuser', 
    '$2a$10$v0xEy1Hm5s1QbZjX4BzgF9EpPl4t8kMhMaE/NKq2U1Zf', -- Test123456 (BCrypt加密)
    '测试用户', 
    'test@campus.edu.cn', 
    '13800138001', 
    'user', 
    1, 
    NOW(), 
    NOW()
);

-- 创建审核员用户
INSERT IGNORE INTO users (username, password, real_name, email, phone, role, status, created_at, updated_at)
VALUES 
(
    'reviewer', 
    '$2a$10$v0xEy1Hm5s1QbZjX4BzgF9EpPl4t8kMhMaE/NKq2U1Zf', -- Review123456 (BCrypt加密)
    '审核员', 
    'reviewer@campus.edu.cn', 
    '13800138002', 
    'reviewer', 
    1, 
    NOW(), 
    NOW()
);

-- 显示创建的用户
SELECT id, username, real_name, email, role, status FROM users WHERE role IN ('admin', 'reviewer') OR username IN ('admin', 'testuser', 'reviewer');