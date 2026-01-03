package com.campus.lostfound.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.lostfound.entity.Review;
import com.campus.lostfound.enums.ReviewAction;

import java.util.Map;

/**
 * 审核服务接口
 */
public interface IReviewService extends IService<Review> {
    
    /**
     * 审核物品
     */
    Map<String, Object> reviewItem(Long itemId, String status, String reason, ReviewAction action, Long reviewerId);
    
    /**
     * 批量审核
     */
    Map<String, Object> batchReview(Long[] itemIds, String status, String reason, ReviewAction action, Long reviewerId);
    
    /**
     * 获取待审核数量
     */
    Long getPendingCount();
    
    /**
     * 获取审核历史
     */
    IPage<Map<String, Object>> getReviewHistory(int current, int pageSize, String type, String status, Long reviewerId);
    
    /**
     * 获取审核统计
     */
    Map<String, Object> getReviewStats(Long reviewerId);
}