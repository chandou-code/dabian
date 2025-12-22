package com.campus.lostfound.service;

import com.campus.lostfound.dto.DashboardDTO;
import com.campus.lostfound.mapper.ItemMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 统计服务实现类
 */
@Slf4j
@Service
public class StatisticsService {

    @Autowired
    private ItemService itemService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ReviewService reviewService;
    
    @Autowired
    private ActivityService activityService;
    
    @Autowired
    private MatchService matchService;
    
    @Autowired
    private ItemMapper itemMapper;
    
    // 提供给Controller调用的公共方法
    public ItemMapper getItemMapper() {
        return itemMapper;
    }
    
    /**
     * 获取统计数据
     */
    public Map<String, Object> getStatistics(String period, String startDate, String endDate, Long userId) {
        Map<String, Object> statistics = new HashMap<>();
        
        // 获取总数统计
        statistics.put("totalLostItems", itemService.getLostItemCount(userId));
        statistics.put("totalFoundItems", itemService.getFoundItemCount(userId));
        statistics.put("pendingReview", reviewService.getPendingCount());
        statistics.put("completedClaims", itemService.getCompletedClaimsCount(userId));
        
        // 获取趋势数据
        List<Map<String, Object>> trendData = getTrendData(period, startDate, endDate, userId);
        statistics.put("trendData", trendData);
        
        // 获取类别分布
        List<Map<String, Object>> categoryDistribution = getCategoryDistribution(userId);
        statistics.put("categoryDistribution", categoryDistribution);
        
        return statistics;
    }
    
    /**
     * 获取用户仪表盘统计
     */
    public DashboardDTO getUserDashboard(Long userId) {
        DashboardDTO dashboard = new DashboardDTO();
        
        try {
            // 1. 获取用户统计信息
            DashboardDTO.UserStatsDTO userStats = getUserStatistics(userId);
            dashboard.setUserStats(userStats);
            
            // 2. 获取最近活动
            List<DashboardDTO.ActivityDTO> recentActivities = activityService.getUserRecentActivities(userId, 10);
            dashboard.setRecentActivities(recentActivities);
            
            // 3. 获取推荐匹配
            List<DashboardDTO.MatchItemDTO> recommendedMatches = matchService.getRecommendedMatches(userId);
            dashboard.setRecommendedMatches(recommendedMatches);
            
            log.info("获取用户{}仪表板数据成功", userId);
            
        } catch (Exception e) {
            log.error("获取用户仪表板数据失败", e);
            // 返回空数据而不是null，避免前端报错
            dashboard.setUserStats(new DashboardDTO.UserStatsDTO());
            dashboard.setRecentActivities(new ArrayList<>());
            dashboard.setRecommendedMatches(new ArrayList<>());
        }
        
        return dashboard;
    }
    
    /**
     * 获取用户统计信息
     */
    private DashboardDTO.UserStatsDTO getUserStatistics(Long userId) {
        DashboardDTO.UserStatsDTO userStats = new DashboardDTO.UserStatsDTO();
        
        try {
            // 从数据库获取真实统计数据
            Map<String, Object> statsMap = itemMapper.getUserItemStats(userId);
            
            log.info("用户{}的原始统计数据: {}", userId, statsMap);
            
            userStats.setTotalLost(statsMap.get("totalLost") != null ? 
                Integer.parseInt(statsMap.get("totalLost").toString()) : 0);
            userStats.setTotalFound(statsMap.get("totalFound") != null ? 
                Integer.parseInt(statsMap.get("totalFound").toString()) : 0);
            userStats.setRecovered(statsMap.get("recovered") != null ? 
                Integer.parseInt(statsMap.get("recovered").toString()) : 0);
            userStats.setPending(statsMap.get("pending") != null ? 
                Integer.parseInt(statsMap.get("pending").toString()) : 0);
            
            // 获取额外的统计信息
            int pendingReviewCount = itemMapper.getUserPendingReviewCount(userId);
            int unreadNotificationCount = itemMapper.getUserUnreadNotificationCount(userId);
            
            userStats.setPendingReview(pendingReviewCount);
            userStats.setUnreadNotifications(unreadNotificationCount);
            
            log.info("用户{}处理后的统计数据: totalLost={}, totalFound={}, recovered={}, pending={}, pendingReview={}, unreadNotifications={}", 
                userId, userStats.getTotalLost(), userStats.getTotalFound(), 
                userStats.getRecovered(), userStats.getPending(), 
                userStats.getPendingReview(), userStats.getUnreadNotifications());
            
        } catch (Exception e) {
            log.error("获取用户{}统计信息失败", userId, e);
            // 设置默认值
            userStats.setTotalLost(0);
            userStats.setTotalFound(0);
            userStats.setRecovered(0);
            userStats.setPending(0);
            userStats.setPendingReview(0);
            userStats.setUnreadNotifications(0);
        }
        
        return userStats;
    }
    
    /**
     * 获取用户仪表板统计（兼容旧接口）
     */
    public Map<String, Object> getUserDashboardMap(Long userId) {
        DashboardDTO dashboard = getUserDashboard(userId);
        
        // 转换为Map格式以保持向后兼容
        Map<String, Object> resultMap = new HashMap<>();
        
        // 用户统计信息
        if (dashboard.getUserStats() != null) {
            DashboardDTO.UserStatsDTO stats = dashboard.getUserStats();
            resultMap.put("totalLost", stats.getTotalLost());
            resultMap.put("totalFound", stats.getTotalFound());
            resultMap.put("recovered", stats.getRecovered());
            resultMap.put("pending", stats.getPending());
            resultMap.put("pendingReview", stats.getPendingReview());
            resultMap.put("unreadNotifications", stats.getUnreadNotifications());
        }
        
        // 最近活动
        if (dashboard.getRecentActivities() != null) {
            resultMap.put("recentActivities", convertActivitiesToMap(dashboard.getRecentActivities()));
        }
        
        // 推荐匹配
        if (dashboard.getRecommendedMatches() != null) {
            resultMap.put("recommendedMatches", convertMatchesToMap(dashboard.getRecommendedMatches()));
        }
        
        return resultMap;
    }
    
    /**
     * 将ActivityDTO列表转换为Map列表
     */
    private List<Map<String, Object>> convertActivitiesToMap(List<DashboardDTO.ActivityDTO> activities) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (DashboardDTO.ActivityDTO activity : activities) {
            Map<String, Object> activityMap = new HashMap<>();
            activityMap.put("id", activity.getId());
            activityMap.put("icon", activity.getIcon());
            activityMap.put("title", activity.getTitle());
            activityMap.put("description", activity.getDescription());
            activityMap.put("time", activity.getTime());
            activityMap.put("type", activity.getType());
            activityMap.put("status", activity.getStatus());
            activityMap.put("relatedItemId", activity.getRelatedItemId());
            result.add(activityMap);
        }
        
        return result;
    }
    
    /**
     * 将MatchItemDTO列表转换为Map列表
     */
    private List<Map<String, Object>> convertMatchesToMap(List<DashboardDTO.MatchItemDTO> matches) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (DashboardDTO.MatchItemDTO match : matches) {
            Map<String, Object> matchMap = new HashMap<>();
            matchMap.put("id", match.getId());
            matchMap.put("title", match.getTitle());
            matchMap.put("description", match.getDescription());
            matchMap.put("image", match.getImage());
            matchMap.put("location", match.getLocation());
            matchMap.put("score", match.getScore());
            matchMap.put("type", match.getType());
            matchMap.put("relatedItemId", match.getRelatedItemId());
            result.add(matchMap);
        }
        
        return result;
    }
    
    /**
     * 获取管理员核心指标
     */
    public Map<String, Object> getAdminCoreStatistics() {
        Map<String, Object> coreStats = new HashMap<>();
        
        // 用户统计
        int totalUsers = userService.getTotalUserCount();
        double userGrowth = getUserGrowthRate();
        
        coreStats.put("totalUsers", totalUsers);
        coreStats.put("userGrowth", Math.round(userGrowth * 10.0) / 10.0);
        
        // 物品统计
        int totalItems = itemService.getTotalItemCount();
        double itemGrowth = getItemGrowthRate();
        
        coreStats.put("totalItems", totalItems);
        coreStats.put("itemGrowth", Math.round(itemGrowth * 10.0) / 10.0);
        
        // 回收率
        double recoveryRate = getOverallRecoveryRate();
        double recoveryTrend = getRecoveryTrend();
        
        coreStats.put("recoveryRate", Math.round(recoveryRate * 10.0) / 10.0);
        coreStats.put("recoveryTrend", Math.round(recoveryTrend * 10.0) / 10.0);
        
        // 活跃用户
        int activeUsers = getActiveUserCount();
        coreStats.put("activeUsers", activeUsers);
        
        return coreStats;
    }
    
    /**
     * 获取物品类别统计
     */
    public Map<String, Object> getCategoryStatistics() {
        Map<String, Object> categoryStats = new HashMap<>();
        
        List<Map<String, Object>> categories = itemService.getCategoryStatistics();
        categoryStats.put("categories", categories);
        
        return categoryStats;
    }
    
    /**
     * 获取趋势统计
     */
    public Map<String, Object> getTrendStatistics(String period, String startDate, String endDate) {
        Map<String, Object> trends = new HashMap<>();
        
        // 根据时间周期获取趋势数据
        List<Map<String, Object>> trendData = getTrendData(period, startDate, endDate, null);
        trends.put("trendData", trendData);
        
        return trends;
    }
    
    /**
     * 获取趋势数据
     */
    private List<Map<String, Object>> getTrendData(String period, String startDate, String endDate, Long userId) {
        List<Map<String, Object>> trendData = new ArrayList<>();
        
        // 这里简化处理，实际应该根据时间范围查询数据库
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        // 生成最近7天的数据作为示例
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            Map<String, Object> data = new HashMap<>();
            data.put("date", date.format(formatter));
            data.put("lostCount", (int)(Math.random() * 10) + 1);
            data.put("foundCount", (int)(Math.random() * 8) + 1);
            trendData.add(data);
        }
        
        return trendData;
    }
    
    /**
     * 获取类别分布
     */
    private List<Map<String, Object>> getCategoryDistribution(Long userId) {
        List<Map<String, Object>> distribution = new ArrayList<>();
        
        String[] categories = {"电子产品", "生活用品", "学习用品", "证件类", "其他"};
        for (String category : categories) {
            Map<String, Object> data = new HashMap<>();
            data.put("category", category);
            data.put("count", (int)(Math.random() * 20) + 5);
            distribution.add(data);
        }
        
        return distribution;
    }
    
    /**
     * 获取最近活动
     */
    private List<Map<String, Object>> getRecentActivities(Long userId) {
        List<Map<String, Object>> activities = new ArrayList<>();
        
        // 这里简化处理，实际应该查询用户的活动记录
        Map<String, Object> activity1 = new HashMap<>();
        activity1.put("id", 1L);
        activity1.put("title", "您的书包失物招领信息已发布");
        activity1.put("time", "2024-01-01 14:30:00");
        activity1.put("type", "publish");
        activities.add(activity1);
        
        Map<String, Object> activity2 = new HashMap<>();
        activity2.put("id", 2L);
        activity2.put("title", "您的手机招领信息已被认领");
        activity2.put("time", "2024-01-02 09:15:00");
        activity2.put("type", "claim");
        activities.add(activity2);
        
        return activities;
    }
    
    /**
     * 获取推荐匹配
     */
    private List<Map<String, Object>> getRecommendedMatches(Long userId) {
        List<Map<String, Object>> matches = new ArrayList<>();
        
        // 这里简化处理，实际应该根据算法计算匹配
        Map<String, Object> match = new HashMap<>();
        match.put("id", 1L);
        match.put("itemName", "黑色华为手机");
        match.put("type", "found");
        match.put("matchScore", 85);
        match.put("distance", "500m");
        matches.add(match);
        
        return matches;
    }
    
    /**
     * 获取用户增长率
     */
    private double getUserGrowthRate() {
        // 这里简化处理，实际应该对比不同时期的用户数量
        return 12.5;
    }
    
    /**
     * 获取物品增长率
     */
    private double getItemGrowthRate() {
        // 这里简化处理，实际应该对比不同时期的物品数量
        return 8.3;
    }
    
    /**
     * 获取总体回收率
     */
    private double getOverallRecoveryRate() {
        int totalItems = itemService.getTotalItemCount();
        int recoveredItems = itemService.getRecoveredItemCount();
        
        return totalItems > 0 ? (double) recoveredItems / totalItems * 100 : 0;
    }
    
    /**
     * 获取回收趋势
     */
    private double getRecoveryTrend() {
        // 这里简化处理，实际应该计算回收率的变化趋势
        return 2.1;
    }
    
    /**
     * 获取活跃用户数
     */
    private int getActiveUserCount() {
        // 这里简化处理，实际应该统计最近活跃的用户数量
        return 320;
    }
}