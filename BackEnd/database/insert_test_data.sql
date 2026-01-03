-- 插入正确的测试数据（确保字符编码正确）
USE campus_lost_found;

-- 清理可能的错误数据
DELETE FROM items WHERE id >= 100;

-- 插入新的测试数据（按创建时间递减，确保最新数据在前面）
INSERT INTO items (id, item_name, category, description, location, event_time, type, status, submitter_id, created_at) VALUES
(101, '黑色钱包', '证件类', '在图书馆二楼丢失，内有身份证和银行卡', '图书馆二楼', '2024-12-20', 'lost', 'approved', 1, '2024-12-20 14:30:00'),
(102, '蓝色水杯', '生活用品', '在教学楼三楼卫生间发现的蓝色保温杯', '教学楼三楼卫生间', '2024-12-19', 'found', 'approved', 2, '2024-12-19 10:20:00'),
(103, 'AirPods Pro', '电子产品', '白色降噪耳机，在食堂丢失，充电盒也在', '食堂一楼', '2024-12-18', 'lost', 'claimed', 1, '2024-12-18 12:15:00'),
(104, '学生卡', '证件类', '捡到一张学生卡，姓名李四，学号20201234', '操场', '2024-12-17', 'found', 'approved', 3, '2024-12-17 16:45:00'),
(105, '笔记本电脑', '电子产品', '华为MateBook 14，银色，在自习室忘记带走', '图书馆三楼自习室', '2024-12-16', 'lost', 'pending', 2, '2024-12-16 20:10:00'),
(106, '雨伞', '生活用品', '蓝色折叠伞，在教学楼门口捡到', '教学楼A栋门口', '2024-12-15', 'found', 'approved', 1, '2024-12-15 08:30:00'),
(107, '钥匙串', '其他', '一把汽车钥匙和两把家门钥匙，挂在红色钥匙扣上', '食堂二楼', '2024-12-14', 'lost', 'approved', 3, '2024-12-14 13:25:00'),
(108, '眼镜', '其他', '黑框眼镜，在篮球场捡到', '篮球场', '2024-12-13', 'found', 'pending', 2, '2024-12-13 17:50:00'),
(109, '教科书', '学习用品', '《高等数学》第七版，有笔记', '教室B102', '2024-12-12', 'lost', 'claimed', 1, '2024-12-12 15:40:00'),
(110, '充电宝', '电子产品', '小米充电宝，10000mAh，白色', '宿舍楼下', '2024-12-11', 'found', 'approved', 3, '2024-12-11 09:15:00');

-- 验证数据插入和排序
SELECT 
    id,
    item_name as title,
    description,
    type,
    status,
    created_at as createTime
FROM items 
WHERE status IN ('approved', 'claimed') AND deleted = 0 AND id >= 100
ORDER BY created_at DESC 
LIMIT 10;

-- 检查字符编码
SELECT 
    id,
    item_name,
    description,
    HEX(item_name) as hex_name
FROM items 
WHERE id >= 100
ORDER BY id DESC;