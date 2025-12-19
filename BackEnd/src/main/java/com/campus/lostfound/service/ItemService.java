package com.campus.lostfound.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.lostfound.entity.Item;

import java.util.List;
import java.util.Map;

/**
 * 物品服务接口
 */
public interface ItemService extends IService<Item> {
    
    /**
     * 发布失物信息
     */
    Item publishLostItem(Item item, Long submitterId);
    
    /**
     * 发布招领信息
     */
    Item publishFoundItem(Item item, Long submitterId);
    
    /**
     * 获取物品列表
     */
    IPage<Item> getItemPage(int current, int size, String type, String category, 
                           String status, String keyword, String location, Long submitterId);
    
    /**
     * 获取物品详情
     */
    Item getItemDetail(Long itemId);
    
    /**
     * 搜索物品
     */
    IPage<Item> searchItems(int current, int size, String keyword, String type, 
                           String category, String timeRange);
    
    /**
     * 获取用户统计数据
     */
    Map<String, Object> getUserStatistics(Long userId);
    
    /**
     * 获取管理员统计数据
     */
    Map<String, Object> getAdminStatistics(String startDate, String endDate, String timeRange);
    
    /**
     * 获取审核员统计数据
     */
    Map<String, Object> getReviewerStatistics(Long reviewerId);
    
    /**
     * 获取待审核物品列表
     */
    IPage<Item> getPendingItems(int current, int size, String type, String status, String keyword);
    
    /**
     * 审核通过
     */
    boolean approveItem(Long itemId, Long reviewerId, String reviewerName, String comment);
    
    /**
     * 审核驳回
     */
    boolean rejectItem(Long itemId, Long reviewerId, String reviewerName, String reason);
    
    /**
     * 批量审核
     */
    Map<String, Object> batchReview(Long[] itemIds, Long reviewerId, String reviewerName, 
                                   String action, String reason);
    
    /**
     * 标记物品已找回
     */
    boolean markItemFound(Long itemId, Long matchedItemId);
    
    /**
     * 标记物品已领回
     */
    boolean markItemRecovered(Long itemId);
    
    /**
     * 获取用户最近活动
     */
    List<Item> getUserRecentActivities(Long userId, Integer limit);
    
    /**
     * 获取推荐匹配
     */
    List<Item> getRecommendedMatches(Long userId);
    
    /**
     * 更新浏览次数
     */
    void incrementViewCount(Long itemId);
    
    /**
     * 获取物品统计
     */
    int getItemCount(String type, String status, Long submitterId);
    
    /**
     * 获取失物数量
     */
    int getLostItemCount(Long userId);
    
    /**
     * 获取招领数量
     */
    int getFoundItemCount(Long userId);
    
    /**
     * 获取已完成认领数量
     */
    int getCompletedClaimsCount(Long userId);
    
    /**
     * 获取总发布数量
     */
    int getTotalPostedCount(Long userId);
    
    /**
     * 获取总找到数量
     */
    int getTotalFoundCount(Long userId);
    
    /**
     * 获取物品总数
     */
    int getTotalItemCount();
    
    /**
     * 获取已找回物品数
     */
    int getRecoveredItemCount();
    
    /**
     * 获取类别统计
     */
    List<Map<String, Object>> getCategoryStatistics();
}