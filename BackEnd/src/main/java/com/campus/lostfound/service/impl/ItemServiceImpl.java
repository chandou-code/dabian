package com.campus.lostfound.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.lostfound.common.constants.StatusConstants;
import com.campus.lostfound.entity.Item;
import com.campus.lostfound.entity.Review;
import com.campus.lostfound.entity.User;
import com.campus.lostfound.mapper.ItemMapper;
import com.campus.lostfound.mapper.ReviewMapper;
import com.campus.lostfound.service.ItemService;
import com.campus.lostfound.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 物品服务实现类
 */
@Slf4j
@Service
public class ItemServiceImpl extends ServiceImpl<ItemMapper, Item> implements ItemService {
    
    private final UserService userService;
    private final ReviewMapper reviewMapper;
    
    public ItemServiceImpl(UserService userService, ReviewMapper reviewMapper) {
        this.userService = userService;
        this.reviewMapper = reviewMapper;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Item publishLostItem(Item item, Long submitterId) {
        if (item == null || submitterId == null) {
            return null;
        }
        
        // 设置发布信息
        item.setType(StatusConstants.ITEM_TYPE_LOST);
        item.setStatus(StatusConstants.ITEM_STATUS_PENDING);
        item.setCreatedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());
        
        // 设置发布者姓名
        User user = userService.getById(submitterId);
        if (user != null) {
            item.setSubmitterName(user.getRealName());
            item.setSubmitterId(submitterId);
        }
        

        
        boolean success = save(item);
        return success ? item : null;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Item publishFoundItem(Item item, Long submitterId) {
        if (item == null || submitterId == null) {
            return null;
        }
        
        // 设置发布信息
        item.setType(StatusConstants.ITEM_TYPE_FOUND);
        item.setStatus(StatusConstants.ITEM_STATUS_PENDING);
        item.setCreatedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());
        
        // 设置发布者姓名
        User user = userService.getById(submitterId);
        if (user != null) {
            item.setSubmitterName(user.getRealName());
            item.setSubmitterId(submitterId);
        }
        

        
        boolean success = save(item);
        return success ? item : null;
    }
    
    @Override
    public IPage<Item> getItemPage(int current, int size, String type, String category, 
                                  String status, String keyword, String location, Long submitterId) {
        Page<Item> page = new Page<>(current, size);
        return baseMapper.selectItemPage(page, type, category, status, keyword, location, submitterId);
    }
    
    @Override
    public Item getItemDetail(Long itemId) {
        if (itemId == null) {
            return null;
        }
        
        Item item = getById(itemId);
        if (item != null) {
            // 增加浏览次数
            baseMapper.updateViewCount(itemId);

        }
        
        return item;
    }
    
    @Override
    public IPage<Item> searchItems(int current, int size, String keyword, String type, 
                                   String category, String timeRange) {
        Page<Item> page = new Page<>(current, size);
        return baseMapper.searchItems(page, keyword, type, category, timeRange);
    }
    
    @Override
    public Map<String, Object> getUserStatistics(Long userId) {
        if (userId == null) {
            return new HashMap<>();
        }
        
        Map<String, Object> stats = new HashMap<>();
        
        // 统计用户发布的失物数量
        int totalLost = baseMapper.countItems(StatusConstants.ITEM_TYPE_LOST, null, userId);
        
        // 统计用户发布的招领数量
        int totalFound = baseMapper.countItems(StatusConstants.ITEM_TYPE_FOUND, null, userId);
        
        // 统计已找回物品数量
        int recovered = baseMapper.countItems(null, StatusConstants.ITEM_STATUS_RECOVERED, userId);
        
        // 统计待处理数量
        int pending = baseMapper.countItems(null, StatusConstants.ITEM_STATUS_PENDING, userId);
        
        stats.put("totalLost", totalLost);
        stats.put("totalFound", totalFound);
        stats.put("recovered", recovered);
        stats.put("pending", pending);
        
        // 获取最近活动
        List<Item> recentItems = baseMapper.selectRecentActivities(userId, 3);
        List<Map<String, Object>> recentActivities = new ArrayList<>();
        
        for (Item item : recentItems) {
            Map<String, Object> activity = new HashMap<>();
            activity.put("id", item.getId());
            activity.put("icon", item.getType().equals(StatusConstants.ITEM_TYPE_LOST) ? "📝" : "✅");
            activity.put("title", item.getType().equals(StatusConstants.ITEM_TYPE_LOST) ? "发布了失物信息" : "发布了招领信息");
            activity.put("description", item.getDescription());
            activity.put("time", formatTime(item.getCreatedAt()));
            activity.put("status", item.getStatus());
            recentActivities.add(activity);
        }
        stats.put("recentActivities", recentActivities);
        
        // 获取推荐匹配
        List<Item> recommendedItems = baseMapper.selectRecommendedMatches(userId);
        List<Map<String, Object>> recommendedMatches = new ArrayList<>();
        
        for (Item item : recommendedItems) {
            Map<String, Object> match = new HashMap<>();
            match.put("id", item.getId());
            match.put("itemName", item.getItemName());
            match.put("description", item.getDescription());
            match.put("image", getFirstImage(item.getImages()));
            match.put("location", item.getLocation());
            match.put("score", 85 + (int)(Math.random() * 15)); // 模拟匹配度
            recommendedMatches.add(match);
        }
        stats.put("recommendedMatches", recommendedMatches);
        
        return stats;
    }
    
    @Override
    public Map<String, Object> getAdminStatistics(String startDate, String endDate, String timeRange) {
        Map<String, Object> stats = new HashMap<>();
        
        // 核心指标
        Map<String, Object> coreMetrics = new HashMap<>();
        coreMetrics.put("totalUsers", userService.getUserCount(null, StatusConstants.USER_ENABLED));
        coreMetrics.put("userGrowth", 12.5);
        coreMetrics.put("totalItems", baseMapper.countItems(null, null, null));
        coreMetrics.put("itemGrowth", 8.3);
        coreMetrics.put("recoveryRate", 71.5);
        coreMetrics.put("recoveryTrend", 2.1);
        coreMetrics.put("activeUsers", 234);
        coreMetrics.put("activityGrowth", 15.2);
        stats.put("coreMetrics", coreMetrics);
        
        // 分类统计
        List<Map<String, Object>> categoryStats = new ArrayList<>();
        String[] categories = {"证件类", "电子设备", "生活用品", "学习用品", "服装配饰", "其他"};
        for (String category : categories) {
            Map<String, Object> catStat = new HashMap<>();
            catStat.put("category", category);
            catStat.put("total", (int)(Math.random() * 50) + 10);
            catStat.put("recovered", (int)(Math.random() * 30) + 5);
            catStat.put("recoveryRate", 60 + (int)(Math.random() * 30));
            catStat.put("avgRecoveryTime", 1 + (int)(Math.random() * 7));
            categoryStats.add(catStat);
        }
        stats.put("categoryStats", categoryStats);
        
        // 趋势数据
        Map<String, Object> trends = new HashMap<>();
        List<Map<String, Object>> dailyData = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            Map<String, Object> dayData = new HashMap<>();
            LocalDateTime date = LocalDateTime.now().minusDays(i);
            dayData.put("date", date.toLocalDate().toString());
            dayData.put("lostCount", (int)(Math.random() * 10) + 2);
            dayData.put("foundCount", (int)(Math.random() * 8) + 1);
            dayData.put("recoveredCount", (int)(Math.random() * 5));
            dailyData.add(dayData);
        }
        trends.put("dailyData", dailyData);
        stats.put("trends", trends);
        
        return stats;
    }
    
    @Override
    public Map<String, Object> getReviewerStatistics(Long reviewerId) {
        Map<String, Object> stats = new HashMap<>();
        
        // 待审核数量
        int pending = baseMapper.countItems(null, StatusConstants.ITEM_STATUS_PENDING, null);
        
        // 今日已审核数量
        Integer todayReviewed = reviewMapper.getTodayReviewCount(reviewerId);
        if (todayReviewed == null) todayReviewed = 0;
        
        // 审核统计
        Map<String, Object> reviewStats = reviewMapper.getReviewStats(reviewerId);
        int approved = ((Number) reviewStats.getOrDefault("approved", 0)).intValue();
        int rejected = ((Number) reviewStats.getOrDefault("rejected", 0)).intValue();
        
        stats.put("pending", pending);
        stats.put("approved", approved);
        stats.put("rejected", rejected);
        stats.put("efficiency", 85.5);
        stats.put("todayReviewed", todayReviewed);
        stats.put("avgTime", 1.2);
        stats.put("weeklyReviewed", 45);
        stats.put("accuracy", 92.3);
        
        return stats;
    }
    
    @Override
    public IPage<Item> getPendingItems(int current, int size, String type, String status, String keyword) {
        Page<Item> page = new Page<>(current, size);
        return baseMapper.selectPendingItems(page, type, status, keyword);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean approveItem(Long itemId, Long reviewerId, String reviewerName, String comment) {
        if (itemId == null || reviewerId == null) {
            return false;
        }
        
        // 更新物品状态
        boolean success = lambdaUpdate()
            .eq(Item::getId, itemId)
            .set(Item::getStatus, StatusConstants.ITEM_STATUS_APPROVED)
            .set(Item::getReviewerId, reviewerId)
            .set(Item::getUpdatedAt, LocalDateTime.now())
            .update();
        
        if (success) {
            // 记录审核记录
            Review record = new Review();
            record.setItemId(itemId);
            record.setReviewerId(reviewerId);
            record.setAction(StatusConstants.REVIEW_ACTION_APPROVED);
            record.setReason(comment);
            record.setReviewTime(LocalDateTime.now());
            reviewMapper.insert(record);
        }
        
        return success;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean rejectItem(Long itemId, Long reviewerId, String reviewerName, String reason) {
        if (itemId == null || reviewerId == null) {
            return false;
        }
        
        // 更新物品状态
        boolean success = lambdaUpdate()
            .eq(Item::getId, itemId)
            .set(Item::getStatus, StatusConstants.ITEM_STATUS_REJECTED)
            .set(Item::getReviewerId, reviewerId)
            .set(Item::getUpdatedAt, LocalDateTime.now())
            .update();
        
        if (success) {
            // 记录审核记录
            Review record = new Review();
            record.setItemId(itemId);
            record.setReviewerId(reviewerId);
            record.setAction(StatusConstants.REVIEW_ACTION_REJECTED);
            record.setReason(reason);
            record.setReviewTime(LocalDateTime.now());
            reviewMapper.insert(record);
        }
        
        return success;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> batchReview(Long[] itemIds, Long reviewerId, String reviewerName, 
                                          String action, String reason) {
        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        int failCount = 0;
        
        if (itemIds == null || itemIds.length == 0) {
            result.put("successCount", 0);
            result.put("failCount", 0);
            return result;
        }
        
        for (Long itemId : itemIds) {
            boolean success = false;
            if (StatusConstants.REVIEW_ACTION_APPROVE.equals(action)) {
                success = approveItem(itemId, reviewerId, reviewerName, reason);
            } else if (StatusConstants.REVIEW_ACTION_REJECT.equals(action)) {
                success = rejectItem(itemId, reviewerId, reviewerName, reason);
            }
            
            if (success) {
                successCount++;
            } else {
                failCount++;
            }
        }
        
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        return result;
    }
    
    @Override
    public boolean markItemFound(Long itemId, Long matchedItemId) {
        return lambdaUpdate()
            .eq(Item::getId, itemId)
            .set(Item::getStatus, StatusConstants.ITEM_STATUS_FOUND)
            .set(Item::getMatchedItemId, matchedItemId)
            .set(Item::getMatchedTime, LocalDateTime.now())
            .update();
    }
    
    @Override
    public boolean markItemRecovered(Long itemId) {
        return lambdaUpdate()
            .eq(Item::getId, itemId)
            .set(Item::getStatus, StatusConstants.ITEM_STATUS_RECOVERED)
            .update();
    }
    
    @Override
    public List<Item> getUserRecentActivities(Long userId, Integer limit) {
        if (userId == null) {
            return new ArrayList<>();
        }
        return baseMapper.selectRecentActivities(userId, limit);
    }
    
    @Override
    public List<Item> getRecommendedMatches(Long userId) {
        if (userId == null) {
            return new ArrayList<>();
        }
        return baseMapper.selectRecommendedMatches(userId);
    }
    
    @Override
    public void incrementViewCount(Long itemId) {
        if (itemId != null) {
            baseMapper.updateViewCount(itemId);
        }
    }
    
    @Override
    public int getItemCount(String type, String status, Long submitterId) {
        return baseMapper.countItems(type, status, submitterId);
    }
    
    @Override
    public int getLostItemCount(Long userId) {
        return baseMapper.countItems("lost", null, userId);
    }
    
    @Override
    public int getFoundItemCount(Long userId) {
        return baseMapper.countItems("found", null, userId);
    }
    
    @Override
    public int getCompletedClaimsCount(Long userId) {
        return baseMapper.countItems(null, "recovered", userId);
    }
    
    @Override
    public int getTotalPostedCount(Long userId) {
        return baseMapper.countItems(null, null, userId);
    }
    
    @Override
    public int getTotalFoundCount(Long userId) {
        return baseMapper.countItems(null, "found", userId) + baseMapper.countItems(null, "recovered", userId);
    }
    
    @Override
    public int getTotalItemCount() {
        return baseMapper.countItems(null, null, null);
    }
    
    @Override
    public int getRecoveredItemCount() {
        return baseMapper.countItems(null, "recovered", null);
    }
    
    @Override
    public List<Map<String, Object>> getCategoryStatistics() {
        return baseMapper.getCategoryStatistics();
    }
    
    /**
     * 格式化时间
     */
    private String formatTime(LocalDateTime time) {
        if (time == null) {
            return "";
        }
        
        LocalDateTime now = LocalDateTime.now();
        long days = java.time.Duration.between(time, now).toDays();
        
        if (days == 0) {
            long hours = java.time.Duration.between(time, now).toHours();
            if (hours == 0) {
                long minutes = java.time.Duration.between(time, now).toMinutes();
                return minutes + "分钟前";
            }
            return hours + "小时前";
        } else if (days == 1) {
            return "昨天";
        } else if (days < 7) {
            return days + "天前";
        } else {
            return time.toLocalDate().toString();
        }
    }
    
    /**
     * 获取第一张图片
     */
    private String getFirstImage(String images) {
        if (StrUtil.isBlank(images)) {
            return "";
        }
        
        try {
            List<String> imageList = JSONUtil.toList(images, String.class);
            return imageList.isEmpty() ? "" : imageList.get(0);
        } catch (Exception e) {
            return "";
        }
    }
}