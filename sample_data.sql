-- 样例数据 - 可以后续手动添加或修改
USE campus_lost_found;

-- 基础用户数据（如果还没有用户）
INSERT IGNORE INTO users (id, username, password, real_name, email, phone, role, college, grade) VALUES
(1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', '管理员', 'admin@campus.edu', '13800000001', 'admin', '信息学院', '2020级'),
(2, 'user1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', '张三', 'zhangsan@campus.edu', '13800000002', 'user', '计算机学院', '2021级'),
(3, 'user2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', '李四', 'lisi@campus.edu', '13800000003', 'user', '电子学院', '2020级');

-- 样例失物招领数据模板（可以复制修改）
INSERT INTO items (item_name, category, description, location, event_time, type, status, submitter_id, created_at) VALUES
-- 失物模板
('黑色钱包', '证件类', '黑色皮质钱包，内有校园卡和银行卡', '图书馆二楼', '2024-12-20', 'lost', 'approved', 2, NOW()),
('白色耳机', '电子产品', 'AirPods Pro白色降噪耳机', '食堂一楼', '2024-12-19', 'lost', 'pending', 3, NOW()),
('蓝色笔记本', '学习用品', '16寸华为MateBook笔记本电脑', '教室A301', '2024-12-18', 'lost', 'approved', 2, NOW()),

-- 招领模板  
('学生卡', '证件类', '捡到一张学生卡，姓名：王五', '操场跑道', '2024-12-17', 'found', 'approved', 1, NOW()),
('雨伞', '生活用品', '黑色折叠伞，看起来很新', '教学楼B栋门口', '2024-12-16', 'found', 'pending', 3, NOW()),
('水杯', '生活用品', '蓝色保温杯，带杯套', '宿舍楼下', '2024-12-15', 'found', 'approved', 2, NOW());

-- 查看当前数据
SELECT '当前items表数据：' as info;
SELECT 
    id,
    item_name,
    category,
    type,
    status,
    location,
    created_at
FROM items 
ORDER BY created_at DESC;