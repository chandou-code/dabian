-- 清空失物数据的SQL脚本
-- 注意：此操作会永久删除所有失物数据，请谨慎执行

-- 使用校园失物招领数据库
USE campus_lost_found;

-- 1. 先删除与失物相关的匹配记录
DELETE FROM matches WHERE lost_item_id IN (SELECT id FROM items WHERE type = 'lost');

-- 2. 再删除与失物相关的通知
DELETE FROM notifications WHERE related_item_id IN (SELECT id FROM items WHERE type = 'lost');

-- 3. 然后删除与失物相关的审核记录
DELETE FROM reviews WHERE item_id IN (SELECT id FROM items WHERE type = 'lost');

-- 4. 接着删除与失物相关的文件上传记录
DELETE FROM file_uploads WHERE related_item_id IN (SELECT id FROM items WHERE type = 'lost');

-- 5. 最后删除失物数据本身
DELETE FROM items WHERE type = 'lost';

-- 6. 重置自增主键（可选，仅在需要重新开始ID计数时使用）
ALTER TABLE items AUTO_INCREMENT = 1;

SELECT '失物数据已成功清空' AS result;
