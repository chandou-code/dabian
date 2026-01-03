-- 修复管理员密码 - 使用MD5加密
USE campus_lost_found;

-- 计算admin密码的MD5值（假设密码是admin123）
-- admin123的MD5是：0192023a7bbd73250516f069df73b247
-- 如果密码是admin，MD5是：21232f297a57a5a743894a0e4a801fc3

-- 更新管理员密码为admin123（MD5）
UPDATE users 
SET password = '0192023a7bbd73250516f069df73b247' 
WHERE username = 'admin';

-- 检查更新结果
SELECT id, username, password, role, status FROM users WHERE username = 'admin';

-- 如果需要其他测试用户，也可以添加
-- user1的密码设为user123（MD5：0192023a7bbd73250516f069df73b247，与admin相同）
UPDATE users 
SET password = '0192023a7bbd73250516f069df73b247' 
WHERE username IN ('user1', 'user2');