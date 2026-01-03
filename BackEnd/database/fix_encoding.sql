-- 检查和修复字符编码问题
USE campus_lost_found;

-- 检查数据库字符集
SHOW CREATE DATABASE campus_lost_found;

-- 检查表字符集
SHOW CREATE TABLE items;

-- 检查表中数据的字符集
SELECT 
    id,
    item_name,
    description,
    category,
    location,
    HEX(item_name) as hex_name,
    HEX(description) as hex_description
FROM items 
WHERE id IN (5, 6, 7, 8, 11)
ORDER BY created_at DESC;

-- 如果需要，可以修复字符编码（谨慎使用）
/*
ALTER DATABASE campus_lost_found CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
ALTER TABLE items CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE items MODIFY item_name VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL;
ALTER TABLE items MODIFY description TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE items MODIFY category VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL;
ALTER TABLE items MODIFY location VARCHAR(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
*/

-- 查看最新插入的测试数据（按创建时间降序）
SELECT 
    id,
    item_name as title,
    description,
    type,
    status,
    category,
    location,
    created_at as createTime,
    event_time as eventTime
FROM items 
WHERE status IN ('approved', 'claimed') AND deleted = 0
ORDER BY created_at DESC 
LIMIT 10;