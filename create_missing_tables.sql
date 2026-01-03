-- 创建缺失的表结构

-- 评论表
CREATE TABLE IF NOT EXISTS item_comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    item_id BIGINT NOT NULL COMMENT '物品ID',
    user_id BIGINT NOT NULL COMMENT '用户ID', 
    content TEXT COMMENT '评论内容',
    like_count INT DEFAULT 0 COMMENT '点赞数',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (item_id) REFERENCES items(id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_item_id (item_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物品评论表';

-- 评论点赞表
CREATE TABLE IF NOT EXISTS comment_likes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    comment_id BIGINT NOT NULL COMMENT '评论ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (comment_id) REFERENCES item_comments(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id),
    UNIQUE KEY uk_comment_user (comment_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论点赞表';

-- 线索表
CREATE TABLE IF NOT EXISTS item_clues (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    item_id BIGINT NOT NULL COMMENT '物品ID',
    contact_name VARCHAR(50) COMMENT '联系人姓名',
    contact_phone VARCHAR(20) COMMENT '联系电话',
    contact_email VARCHAR(100) COMMENT '联系邮箱', 
    content TEXT COMMENT '线索内容',
    images JSON COMMENT '线索图片',
    status VARCHAR(20) DEFAULT 'pending' COMMENT '状态：pending,reviewed,useful,invalid',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (item_id) REFERENCES items(id),
    INDEX idx_item_id (item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物品线索表';

-- 添加ID为45的测试数据
INSERT IGNORE INTO items (
    id, user_id, item_name, category, description, location, event_time, 
    contact_phone, contact_email, type, status, created_at, updated_at
) VALUES (
    45, 1, '蓝色水杯', '电子产品', '在图书馆二楼自习室发现的蓝色保温杯，内有少量温水', 
    '图书馆二楼', '2024-12-20 14:30:00', '138****5678', 'zhang***@example.com', 
    'found', 'approved', NOW(), NOW()
);

-- 为物品45添加一些评论
INSERT IGNORE INTO item_comments (item_id, user_id, content, like_count) VALUES
(45, 2, '这个水杯我在昨天也看到了，好像是有人遗忘的', 3),
(45, 3, '失主应该很着急，建议发到失物招领群里', 5),
(45, 4, '可以交给前台工作人员', 2);

-- 为物品45添加一些线索
INSERT IGNORE INTO item_clues (item_id, contact_name, contact_phone, content, status) VALUES
(45, '李明', '13912345678', '我昨天下午3点左右在自习室看到有人在使用这个水杯', 'pending'),
(45, '王芳', '13787654321', '这个人好像是计算机学院的学生', 'pending');