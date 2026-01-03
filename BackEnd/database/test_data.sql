-- 插入测试数据用于首页展示

USE campus_lost_found;

-- 插入测试用户（如果不存在）
INSERT IGNORE INTO users (id, username, password, real_name, email, phone, role, college, grade) VALUES
(1, 'testuser1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', '张三', 'zhangsan@example.com', '13800138001', 'user', '计算机学院', '2021级'),
(2, 'testuser2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', '李四', 'lisi@example.com', '13800138002', 'user', '电子学院', '2020级'),
(3, 'testuser3', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', '王五', 'wangwu@example.com', '13800138003', 'user', '机械学院', '2022级');

-- 插入测试失物数据
INSERT IGNORE INTO items (id, item_name, category, description, location, event_time, type, status, submitter_id, created_at) VALUES
(1, '黑色钱包', '证件类', '在图书馆二楼丢失，内有身份证和银行卡，身份证姓名张三', '图书馆二楼', '2024-01-15', 'lost', 'approved', 1, '2024-01-15 14:30:00'),
(2, '蓝色水杯', '生活用品', '在教学楼三楼卫生间发现的蓝色保温杯，看起来很新', '教学楼三楼卫生间', '2024-01-14', 'found', 'approved', 2, '2024-01-14 10:20:00'),
(3, 'AirPods Pro', '电子产品', '黑色降噪耳机，在食堂一楼吃饭时丢失，充电盒也在', '食堂一楼', '2024-01-13', 'lost', 'claimed', 1, '2024-01-13 12:15:00'),
(4, '学生卡', '证件类', '捡到一张学生卡，姓名李四，学号20201234', '操场', '2024-01-16', 'found', 'approved', 3, '2024-01-16 16:45:00'),
(5, '笔记本电脑', '电子产品', '华为MateBook 14，银色，在自习室忘记带走', '图书馆三楼自习室', '2024-01-12', 'lost', 'pending', 2, '2024-01-12 20:10:00'),
(6, '雨伞', '生活用品', '蓝色折叠伞，在教学楼门口捡到', '教学楼A栋门口', '2024-01-17', 'found', 'approved', 1, '2024-01-17 08:30:00'),
(7, '钥匙串', '其他', '一把汽车钥匙和两把家门钥匙，挂在红色钥匙扣上', '食堂二楼', '2024-01-11', 'lost', 'approved', 3, '2024-01-11 13:25:00'),
(8, '眼镜', '其他', '黑框眼镜，在篮球场捡到', '篮球场', '2024-01-18', 'found', 'pending', 2, '2024-01-18 17:50:00'),
(9, '教科书', '学习用品', '《高等数学》第七版，有笔记', '教室B102', '2024-01-10', 'lost', 'claimed', 1, '2024-01-10 15:40:00'),
(10, '充电宝', '电子产品', '小米充电宝，10000mAh，白色', '宿舍楼下', '2024-01-19', 'found', 'approved', 3, '2024-01-19 09:15:00');

-- 更新一些认领信息
UPDATE items SET reviewer_id = 1, review_time = '2024-01-16 09:00:00' WHERE id IN (1, 2, 4);
UPDATE items SET reviewer_id = 1, review_time = '2024-01-13 14:00:00', matched_item_id = 2, recovered_at = '2024-01-15 11:30:00' WHERE id = 3;
UPDATE items SET reviewer_id = 1, review_time = '2024-01-14 10:00:00', matched_item_id = 7, recovered_at = '2024-01-12 16:20:00' WHERE id = 9;

-- 查询测试数据统计
SELECT 
    '失物数量' as 类型,
    COUNT(*) as 数量
FROM items 
WHERE type = 'lost' AND deleted = 0
UNION ALL
SELECT 
    '招领数量' as 类型,
    COUNT(*) as 数量
FROM items 
WHERE type = 'found' AND deleted = 0
UNION ALL
SELECT 
    '已找回' as 类型,
    COUNT(*) as 数量
FROM items 
WHERE status = 'claimed' AND deleted = 0
UNION ALL
SELECT 
    '待审核' as 类型,
    COUNT(*) as 数量
FROM items 
WHERE status = 'pending' AND deleted = 0;