-- 失物招领详情页相关数据库设计

-- 评论表
CREATE TABLE IF NOT EXISTS item_comments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    item_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    content TEXT NOT NULL COMMENT '评论内容',
    like_count INT DEFAULT 0 COMMENT '点赞数',
    is_deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (item_id) REFERENCES items(id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_item_id (item_id),
    INDEX idx_created_at (created_at)
);

-- 点赞表
CREATE TABLE IF NOT EXISTS comment_likes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    comment_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (comment_id) REFERENCES item_comments(id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    UNIQUE KEY uk_comment_user (comment_id, user_id)
);

-- 线索表
CREATE TABLE IF NOT EXISTS item_clues (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    item_id BIGINT NOT NULL,
    contact_name VARCHAR(50) COMMENT '联系人姓名',
    contact_phone VARCHAR(20) COMMENT '联系电话',
    contact_email VARCHAR(100) COMMENT '联系邮箱',
    content TEXT NOT NULL COMMENT '线索内容',
    images JSON COMMENT '线索图片列表',
    status ENUM('pending', 'reviewed', 'useful', 'invalid') DEFAULT 'pending' COMMENT '线索状态',
    is_deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (item_id) REFERENCES items(id),
    INDEX idx_item_id (item_id),
    INDEX idx_status (status)
);

-- 插入一些测试数据
INSERT INTO item_comments (item_id, user_id, content, like_count) VALUES
(42, 26, '我在图书馆见过这个物品，可能还在那里', 5),
(42, 26, '联系电话是否可以多提供几个？', 2),
(43, 26, '这个看起来很贵重，希望失主早日找回', 8),
(38, 26, '我昨天在教学楼附近看到过类似的东西', 3);

INSERT INTO item_clues (item_id, contact_name, contact_phone, content, status) VALUES
(42, '张三', '13812345678', '昨天下午3点左右在图书馆三楼自习室看到，应该还在，可以联系管理员查看监控', 'pending'),
(43, '李四', '13987654321', '在食堂二楼拾获，已交给失物招领处', 'reviewed'),
(38, '王五', '13666666666', '这个钱包我在校园超市附近看到过', 'useful');