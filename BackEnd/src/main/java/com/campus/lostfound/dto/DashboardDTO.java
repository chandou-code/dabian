package com.campus.lostfound.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 仪表板数据传输对象
 */
@Data
public class DashboardDTO {
    
    /**
     * 用户统计信息
     */
    private UserStatsDTO userStats;
    
    /**
     * 最近活动列表
     */
    private List<ActivityDTO> recentActivities;
    
    /**
     * 推荐匹配列表
     */
    private List<MatchItemDTO> recommendedMatches;
    
    /**
     * 用户统计信息DTO
     */
    @Data
    public static class UserStatsDTO {
        /**
         * 发布的失物数量
         */
        private int totalLost;
        
        /**
         * 发布的招领数量
         */
        private int totalFound;
        
        /**
         * 已找回物品数量
         */
        private int recovered;
        
        /**
         * 待处理信息数量
         */
        private int pending;
        
        /**
         * 待审核数量
         */
        private int pendingReview;
        
        /**
         * 未读通知数量
         */
        private int unreadNotifications;
    }
    
    /**
     * 活动记录DTO
     */
    @Data
    public static class ActivityDTO {
        /**
         * 活动ID
         */
        private Long id;
        
        /**
         * 活动图标
         */
        private String icon;
        
        /**
         * 活动标题
         */
        private String title;
        
        /**
         * 活动描述
         */
        private String description;
        
        /**
         * 活动时间
         */
        private LocalDateTime time;
        
        /**
         * 活动类型：publish, review, claim, match
         */
        private String type;
        
        /**
         * 活动状态：pending, approved, rejected, recovered
         */
        private String status;
        
        /**
         * 相关物品ID
         */
        private Long relatedItemId;
    }
    
    /**
     * 推荐匹配物品DTO
     */
    @Data
    public static class MatchItemDTO {
        /**
         * 物品ID
         */
        private Long id;
        
        /**
         * 物品名称
         */
        private String title;
        
        /**
         * 物品描述
         */
        private String description;
        
        /**
         * 物品图片URL
         */
        private String image;
        
        /**
         * 物品地点
         */
        private String location;
        
        /**
         * 匹配分数
         */
        private int score;
        
        /**
         * 物品类型：lost/found
         */
        private String type;
        
        /**
         * 相关物品ID（用于跳转详情）
         */
        private Long relatedItemId;
    }
}