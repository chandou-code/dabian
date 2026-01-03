-- 常用密码的MD5值参考
USE campus_lost_found;

-- 以下是一些常用密码的MD5值（供测试使用）
-- 密码: admin -> MD5: 21232f297a57a5a743894a0e4a801fc3
-- 密码: 123456 -> MD5: e10adc3949ba59abbe56e057f20f883e  
-- 密码: admin123 -> MD5: 0192023a7bbd73250516f069df73b247
-- 密码: password -> MD5: 5f4dcc3b5aa765d61d8327deb882cf99
-- 密码: test123 -> MD5: c88f5e3af44b73789ed52293d9e3c0dd

-- 创建管理员账户（如果不存在）
INSERT IGNORE INTO users (id, username, password, real_name, email, phone, role, college, grade, status, created_at, updated_at) 
VALUES (1, 'admin', '21232f297a57a5a743894a0e4a801fc3', '管理员', 'admin@campus.edu', '13800000001', 'admin', '信息学院', '2020级', 1, NOW(), NOW());

-- 创建测试用户账户
INSERT IGNORE INTO users (id, username, password, real_name, email, phone, role, college, grade, status, created_at, updated_at) 
VALUES 
(2, 'testuser', 'e10adc3949ba59abbe56e057f20f883e', '测试用户', 'test@campus.edu', '13800000002', 'user', '计算机学院', '2021级', 1, NOW(), NOW());

-- 显示所有用户（不显示密码）
SELECT id, username, real_name, email, role, college, grade, status FROM users;

-- 提示信息
SELECT '登录测试账户：' as info;
SELECT '管理员: admin / admin' as account;
SELECT '用户: testuser / 123456' as account;