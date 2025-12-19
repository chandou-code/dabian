-- 校园失物招领系统测试数据
USE campus_lost_found;

-- 插入系统配置
INSERT INTO system_configs (config_key, config_value, description, config_type, is_public) VALUES
('site_name', '校园失物招领系统', '网站名称', 'string', 1),
('site_description', '让失物回家，让爱心传递', '网站描述', 'string', 1),
('site_version', '1.0.0', '系统版本', 'string', 1),
('categories', '["证件类","电子设备","生活用品","学习用品","服装配饰","其他"]', '物品类别', 'json', 1),
('locations', '["教学楼","图书馆","食堂","宿舍","体育馆","实验室","校园道路","其他"]', '地点类型', 'json', 1),
('maintenance_mode', 'false', '维护模式', 'boolean', 0),
('upload_max_size', '10485760', '文件上传最大大小（字节）', 'number', 0),
('email_approval_template', '您的失物招领信息已审核通过，感谢您的支持！', '审核通过邮件模板', 'string', 0),
('email_rejection_template', '您的失物招领信息未通过审核，原因：%reason%', '审核驳回邮件模板', 'string', 0),
('email_recovery_template', '您发布的失物招领有新的反馈，请及时查看！', '找回通知邮件模板', 'string', 0);

-- 插入用户数据
INSERT INTO users (username, password, real_name, email, phone, role, avatar, college, grade, status) VALUES
-- 管理员
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKVjzieMwkOmANgNOgKQNNBDvAGK', '系统管理员', 'admin@campus.edu', '13800000001', 'admin', '/static/avatar/admin.png', '信息工程学院', '2022级', 1),

-- 审核员
('reviewer1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKVjzieMwkOmANgNOgKQNNBDvAGK', '张审核', 'zhang@campus.edu', '13800000002', 'reviewer', '/static/avatar/reviewer1.png', '信息工程学院', '2021级', 1),
('reviewer2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKVjzieMwkOmANgNOgKQNNBDvAGK', '李审核', 'li@campus.edu', '13800000003', 'reviewer', '/static/avatar/reviewer2.png', '计算机学院', '2020级', 1),

-- 普通用户
('user1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKVjzieMwkOmANgNOgKQNNBDvAGK', '张三', 'zhangsan@campus.edu', '13800001001', 'user', '/static/avatar/user1.png', '计算机学院', '2022级', 1),
('user2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKVjzieMwkOmANgNOgKQNNBDvAGK', '李四', 'lisi@campus.edu', '13800001002', 'user', '/static/avatar/user2.png', '信息工程学院', '2021级', 1),
('user3', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKVjzieMwkOmANgNOgKQNNBDvAGK', '王五', 'wangwu@campus.edu', '13800001003', 'user', '/static/avatar/user3.png', '电子工程学院', '2022级', 1),
('user4', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKVjzieMwkOmANgNOgKQNNBDvAGK', '赵六', 'zhaoliu@campus.edu', '13800001004', 'user', '/static/avatar/user4.png', '管理学院', '2020级', 1),
('user5', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKVjzieMwkOmANgNOgKQNNBDvAGK', '孙七', 'sunqi@campus.edu', '13800001005', 'user', '/static/avatar/user5.png', '外国语学院', '2021级', 1);

-- 插入失物招领物品数据
INSERT INTO items (item_name, category, description, images, contact, location, event_time, type, status, submitter_id, reviewer_id, review_time) VALUES
-- 已审核通过的失物
('黑色双肩书包', '生活用品', '黑色双肩书包，内有《数据结构》教材、笔记本和一支钢笔。书包有点旧，但有明显的黄色拉链。', 
'["/uploads/items/backpack1.jpg","/uploads/items/backpack2.jpg"]', 
'13800001001', '图书馆三楼自习区', '2024-12-10', 'lost', 'approved', 1, 2, '2024-12-11 09:30:00'),

('白色iPhone 13手机', '电子设备', '白色iPhone 13，128GB，有透明的手机壳，屏幕有一道小裂纹，在微信聊天界面有一张本人照片作为屏保。', 
'["/uploads/items/iphone13.jpg"]', 
'13800001002', '教学楼A座201教室', '2024-12-12', 'lost', 'approved', 2, 2, '2024-12-12 16:20:00'),

('学生证', '证件类', '蓝色封面的学生证，姓名：李四，学号：2021010234，计算机学院2021级。证件背面有校园卡功能。', 
'["/uploads/items/student_card.jpg"]', 
'13800001002', '食堂二楼', '2024-12-11', 'lost', 'approved', 2, 1, '2024-12-11 14:15:00'),

('黑色U盘', '电子设备', '金士顿64GB黑色U盘，挂在蓝色钥匙扣上，里面存有本学期课件资料。', 
'["/uploads/items/usb_flash.jpg"]', 
'13800001003', '实验室302室', '2024-12-13', 'lost', 'approved', 3, 2, '2024-12-13 11:45:00'),

-- 已审核通过的招领
('黑色钱包', '生活用品', '黑色皮革钱包，内有现金约200元和一张身份证（姓名：王五）。在校园主路上捡到的。', 
'["/uploads/items/wallet.jpg"]', 
'13800001004', '校园主路图书馆前', '2024-12-10', 'found', 'approved', 4, 1, '2024-12-10 15:30:00'),

('蓝色雨伞', '生活用品', '折叠式蓝色雨伞，自动开关，品牌为天堂伞。在教学楼门口捡到。', 
'["/uploads/items/umbrella.jpg"]', 
'13800001005', '教学楼B座门口', '2024-12-12', 'found', 'approved', 5, 2, '2024-12-12 17:10:00'),

('银色手表', '其他', '银色机械手表，表带为黑色皮质，表盘显示时间为3:15。品牌未知，看起来比较老旧。', 
'["/uploads/items/watch.jpg"]', 
'13800001001', '体育馆更衣室', '2024-12-11', 'found', 'approved', 1, 1, '2024-12-11 19:20:00'),

('数学教材', '学习用品', '《高等数学》下册，同济大学出版社，书内有笔记和练习题答案。封面有些磨损。', 
'["/uploads/items/math_book.jpg"]', 
'13800001003', '图书馆二楼阅览室', '2024-12-13', 'found', 'approved', 3, 2, '2024-12-13 10:25:00'),

-- 待审核的物品
('无线耳机', '电子设备', '白色无线蓝牙耳机，AirPods Pro款，充电盒电量50%左右。右耳耳机有轻微划痕。', 
'["/uploads/items/airpods.jpg"]', 
'13800001004', '学生活动中心', '2024-12-14', 'lost', 'pending', 4, NULL, NULL),

('红色围巾', '服装配饰', '红色羊毛围巾，长约1.8米，有几何图案。看起来很新，应该是不久前丢失的。', 
'["/uploads/items/scarf.jpg"]', 
'13800001005', '校园花园', '2024-12-14', 'found', 'pending', 5, NULL, NULL),

-- 已被认领的物品
('眼镜盒', '其他', '黑色眼镜盒，内有近视眼镜，度数约500度。镜框为黑色金属材质。', 
'["/uploads/items/glasses.jpg"]', 
'13800001002', '食堂三楼', '2024-12-08', 'found', 'claimed', 2, 1, '2024-12-08 12:30:00'),

('笔记本', '学习用品', 'Dell笔记本电脑，银色，15.6寸，贴有卡通贴纸。密码为：123456。', 
'["/uploads/items/laptop.jpg"]', 
'13800001001', '图书馆电子阅览区', '2024-12-09', 'lost', 'claimed', 1, 2, '2024-12-09 16:45:00');

-- 更新已认领物品的认领时间
UPDATE items SET recovered_at = '2024-12-10 10:30:00' WHERE id = 11;
UPDATE items SET recovered_at = '2024-12-10 14:20:00' WHERE id = 12;

-- 插入匹配记录
INSERT INTO matches (lost_item_id, found_item_id, match_score, similarity_details, status, created_by) VALUES
-- 高匹配度
(1, 5, 85.5, '{"category": 100, "location": 90, "description": 75, "time": 80}', 'confirmed', NULL),
(3, 6, 72.0, '{"category": 100, "location": 60, "description": 65, "time": 65}', 'pending', NULL),

-- 中等匹配度
(2, 7, 45.0, '{"category": 100, "location": 30, "description": 40, "time": 20}', 'pending', NULL),
(8, 1, 38.5, '{"category": 100, "location": 50, "description": 25, "time": 30}', 'rejected', 1);

-- 更新匹配物品的关联
UPDATE items SET matched_item_id = 5 WHERE id = 1;
UPDATE items SET matched_item_id = 6 WHERE id = 3;

-- 插入审核记录
INSERT INTO reviews (item_id, reviewer_id, action, reason, review_time) VALUES
(1, 2, 'approved', '物品描述清晰，信息完整，符合发布规范', '2024-12-11 09:30:00'),
(2, 2, 'approved', '物品信息详细，有清晰图片，同意发布', '2024-12-12 16:20:00'),
(3, 1, 'approved', '证件类物品，信息核实无误', '2024-12-11 14:15:00'),
(4, 2, 'approved', '电子设备，描述具体，同意发布', '2024-12-13 11:45:00'),
(5, 1, 'approved', '招领信息完整，符合规范', '2024-12-10 15:30:00'),
(6, 2, 'approved', '物品描述清楚，同意发布', '2024-12-12 17:10:00'),
(7, 1, 'approved', '招领物品信息真实有效', '2024-12-11 19:20:00'),
(8, 2, 'approved', '教材类物品，信息完整', '2024-12-13 10:25:00'),
(11, 1, 'approved', '证件类物品，同意发布', '2024-12-08 12:30:00'),
(12, 2, 'approved', '贵重物品，描述详细', '2024-12-09 16:45:00');

-- 插入通知数据
INSERT INTO notifications (user_id, title, content, type, related_item_id, is_read, created_at) VALUES
-- 用户1的通知
(1, '您的失物招领信息已审核通过', '您发布的"黑色双肩书包"失物信息已通过审核，现已公开显示。', 'approval', 1, 0, '2024-12-11 09:30:00'),
(1, '有新的招领信息与您匹配', '系统检测到"黑色钱包"可能与您的失物匹配，相似度85.5%。', 'match', 5, 1, '2024-12-11 10:00:00'),
(1, '恭喜！您的失物已被认领', '您发布的"黑色双肩书包"已被成功认领，请及时联系。', 'recovery', 1, 0, '2024-12-10 10:30:00'),

-- 用户2的通知
(2, '您的失物招领信息已审核通过', '您发布的"白色iPhone 13手机"失物信息已通过审核。', 'approval', 2, 1, '2024-12-12 16:20:00'),
(2, '您的失物招领信息已审核通过', '您发布的"学生证"失物信息已通过审核。', 'approval', 3, 1, '2024-12-11 14:15:00'),

-- 用户3的通知
(3, '您的失物招领信息已审核通过', '您发布的"黑色U盘"失物信息已通过审核。', 'approval', 4, 0, '2024-12-13 11:45:00'),
(3, '您的招领信息已审核通过', '您发布的"数学教材"招领信息已通过审核。', 'approval', 8, 1, '2024-12-13 10:25:00'),

-- 用户4的通知
(4, '您的招领信息已审核通过', '您发布的"黑色钱包"招领信息已通过审核。', 'approval', 5, 1, '2024-12-10 15:30:00'),

-- 用户5的通知
(5, '您的招领信息已审核通过', '您发布的"蓝色雨伞"招领信息已通过审核。', 'approval', 6, 1, '2024-12-12 17:10:00');

-- 插入公告数据
INSERT INTO announcements (title, content, status, created_by) VALUES
('系统升级公告', '为了提供更好的服务体验，系统将于本周六凌晨2:00-4:00进行升级维护，期间服务可能暂时中断，敬请谅解。', 'active', 1),
('失物招领小程序上线', '校园失物招领微信小程序正式上线啦！扫码即可快速发布失物信息，支持图片上传和智能匹配功能。', 'active', 1),
('感谢信发布', '本学期以来，在全校师生的共同努力下，失物招领平台共帮助找回失物285件，找回率达到78.5%，感谢大家的积极参与！', 'active', 1);

-- 插入文件上传记录
INSERT INTO file_uploads (original_name, file_name, file_path, file_size, file_type, mime_type, upload_user_id, related_item_id, created_at) VALUES
('backpack1.jpg', 'backpack1_20241210.jpg', '/uploads/items/backpack1.jpg', 1024576, 'image', 'image/jpeg', 1, 1, '2024-12-10 14:30:00'),
('backpack2.jpg', 'backpack2_20241210.jpg', '/uploads/items/backpack2.jpg', 987654, 'image', 'image/jpeg', 1, 1, '2024-12-10 14:31:00'),
('iphone13.jpg', 'iphone13_20241212.jpg', '/uploads/items/iphone13.jpg', 1524567, 'image', 'image/jpeg', 2, 2, '2024-12-12 15:45:00'),
('student_card.jpg', 'student_card_20241211.jpg', '/uploads/items/student_card.jpg', 523456, 'image', 'image/jpeg', 2, 3, '2024-12-11 13:20:00'),
('usb_flash.jpg', 'usb_flash_20241213.jpg', '/uploads/items/usb_flash.jpg', 256789, 'image', 'image/jpeg', 3, 4, '2024-12-13 11:15:00'),
('wallet.jpg', 'wallet_20241210.jpg', '/uploads/items/wallet.jpg', 892456, 'image', 'image/jpeg', 4, 5, '2024-12-10 15:20:00'),
('umbrella.jpg', 'umbrella_20241212.jpg', '/uploads/items/umbrella.jpg', 745632, 'image', 'image/jpeg', 5, 6, '2024-12-12 17:00:00'),
('watch.jpg', 'watch_20241211.jpg', '/uploads/items/watch.jpg', 623456, 'image', 'image/jpeg', 1, 7, '2024-12-11 19:15:00'),
('math_book.jpg', 'math_book_20241213.jpg', '/uploads/items/math_book.jpg', 1123456, 'image', 'image/jpeg', 3, 8, '2024-12-13 10:10:00'),
('airpods.jpg', 'airpods_20241214.jpg', '/uploads/items/airpods.jpg', 856432, 'image', 'image/jpeg', 4, 9, '2024-12-14 09:30:00'),
('scarf.jpg', 'scarf_20241214.jpg', '/uploads/items/scarf.jpg', 657891, 'image', 'image/jpeg', 5, 10, '2024-12-14 14:45:00'),
('glasses.jpg', 'glasses_20241208.jpg', '/uploads/items/glasses.jpg', 432156, 'image', 'image/jpeg', 2, 11, '2024-12-08 12:25:00'),
('laptop.jpg', 'laptop_20241209.jpg', '/uploads/items/laptop.jpg', 1824567, 'image', 'image/jpeg', 1, 12, '2024-12-09 16:40:00');