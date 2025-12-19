-- 添加缺失的字段到items表
USE campus_lost_found;

-- 为items表添加缺失的字段
ALTER TABLE items 
ADD COLUMN title VARCHAR(200) COMMENT '物品标题' AFTER item_name,
ADD COLUMN review_comment TEXT COMMENT '审核评论' AFTER review_reason,
ADD COLUMN view_count INT DEFAULT 0 COMMENT '浏览次数' AFTER review_comment,
ADD COLUMN create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间（别名）' AFTER created_at,
ADD COLUMN lost_time DATE COMMENT '丢失时间' AFTER event_time,
ADD COLUMN found_time DATE COMMENT '发现时间' AFTER lost_time;

-- 为system_configs表添加缺失的字段
ALTER TABLE system_configs 
ADD COLUMN type VARCHAR(50) COMMENT '配置类型（别名）' AFTER config_type,
ADD COLUMN is_system TINYINT DEFAULT 0 COMMENT '是否系统配置：0-否，1-是' AFTER is_public,
ADD COLUMN deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除' AFTER is_system;

-- 检查reviews表是否存在，如果不存在则创建
CREATE TABLE IF NOT EXISTS reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    item_id BIGINT NOT NULL COMMENT '物品ID',
    reviewer_id BIGINT NOT NULL COMMENT '审核者ID',
    action ENUM('approved', 'rejected') NOT NULL COMMENT '审核动作：approved-通过，rejected-驳回',
    reason TEXT COMMENT '审核原因',
    review_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '审核时间',
    
    FOREIGN KEY (item_id) REFERENCES items(id),
    FOREIGN KEY (reviewer_id) REFERENCES users(id),
    
    INDEX idx_item_id (item_id),
    INDEX idx_reviewer_id (reviewer_id),
    INDEX idx_review_time (review_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审核记录表';