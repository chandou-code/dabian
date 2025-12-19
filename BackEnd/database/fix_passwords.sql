-- 修复用户密码：从BCrypt改为MD5
USE campus_lost_found;

-- 更新admin用户密码 (Admin123456)
UPDATE users 
SET password = 'be05977add575832dc52655d4ad5c42e', updated_at = NOW()
WHERE username = 'admin';

-- 更新testuser用户密码 (Test123456)
UPDATE users 
SET password = '126cfbcd4d16ae6d25c9bfcae76d8ee4', updated_at = NOW()
WHERE username = 'testuser';

-- 更新reviewer用户密码 (Review123456)
UPDATE users 
SET password = 'bfc3f69a6ed14690a64276905d66c617', updated_at = NOW()
WHERE username = 'reviewer';

-- 显示更新后的用户信息
SELECT id, username, real_name, email, role, status, LEFT(password, 20) as password_prefix 
FROM users 
WHERE role IN ('admin', 'reviewer') OR username IN ('admin', 'testuser', 'reviewer');