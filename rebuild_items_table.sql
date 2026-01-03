-- 重建items表
USE campus_lost_found;

-- 创建items表
CREATE TABLE items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    item_name VARCHAR(100) NOT NULL COMMENT '物品名称',
    category VARCHAR(50) NOT NULL COMMENT '物品类别',
    description TEXT COMMENT '物品描述',
    images JSON COMMENT '物品图片URL数组',
    contact VARCHAR(500) COMMENT '联系方式',
    location VARCHAR(200) COMMENT '地点（丢失地点或发现地点）',
    event_time DATE COMMENT '事件时间（丢失时间或发现时间）',
    type ENUM('lost', 'found') NOT NULL COMMENT '类型：lost-失物，found-招领',
    status ENUM('pending', 'approved', 'rejected', 'claimed') DEFAULT 'pending' COMMENT '状态',
    submitter_id BIGINT NOT NULL COMMENT '发布者ID',
    reviewer_id BIGINT COMMENT '审核者ID',
    review_time TIMESTAMP NULL COMMENT '审核时间',
    review_reason TEXT COMMENT '审核原因',
    matched_item_id BIGINT COMMENT '匹配的物品ID',
    recovered_at TIMESTAMP NULL COMMENT '认领/找回时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    
    INDEX idx_type (type),
    INDEX idx_status (status),
    INDEX idx_category (category),
    INDEX idx_submitter_id (submitter_id),
    INDEX idx_created_at (created_at),
    INDEX idx_location (location)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物品表';

-- 验证表已创建
SELECT 'items表已重建并清空' as status;
SELECT COUNT(*) as current_count FROM items;