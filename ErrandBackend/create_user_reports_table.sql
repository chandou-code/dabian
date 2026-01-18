-- 创建用户举报表
CREATE TABLE IF NOT EXISTS `user_reports` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `reporter_id` bigint NOT NULL COMMENT '举报者ID',
  `reported_user_id` bigint NOT NULL COMMENT '被举报者ID',
  `type` varchar(20) NOT NULL COMMENT '举报类型：harassment, false_info, illegal_content, other',
  `content` text NOT NULL COMMENT '举报内容',
  `images` varchar(255) DEFAULT NULL COMMENT '举报截图（JSON数组）',
  `status` varchar(20) NOT NULL DEFAULT 'pending' COMMENT '举报状态：pending, processed, dismissed',
  `admin_remark` text COMMENT '管理员处理备注',
  `handled_at` datetime DEFAULT NULL COMMENT '处理时间',
  `created_at` datetime NOT NULL COMMENT '创建时间',
  `updated_at` datetime NOT NULL COMMENT '最后更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_reporter_id` (`reporter_id`),
  KEY `idx_reported_user_id` (`reported_user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户举报表';