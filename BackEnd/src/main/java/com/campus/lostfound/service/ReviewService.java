package com.campus.lostfound.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.lostfound.entity.Item;
import com.campus.lostfound.entity.Review;
import com.campus.lostfound.mapper.ItemMapper;
import com.campus.lostfound.mapper.ReviewMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 审核服务实现类
 */
@Slf4j
@Service
public class ReviewService extends ServiceImpl<ReviewMapper, Review> {

    @Autowired
    private ReviewMapper reviewMapper;
    
    @Autowired
    private ItemMapper itemMapper;
    
    /**
     * 审核物品
     */
    public Map<String, Object> reviewItem(Long itemId, String status, String reason, String action, Long reviewerId) {
        // 查询物品信息
        Item item = itemMapper.selectById(itemId);
        if (item == null) {
            throw new RuntimeException("物品不存在");
        }
        
        // 检查物品状态
        if (!"pending".equals(item.getStatus())) {
            throw new RuntimeException("该物品已审核过了");
        }
        
        // 更新物品状态
        item.setStatus(status);
        item.setUpdatedAt(LocalDateTime.now());
        itemMapper.updateById(item);
        
        // 创建审核记录
        Review review = new Review();
        review.setItemId(itemId);
        review.setReviewerId(reviewerId);
        review.setAction(action);
        review.setReason(reason);
        review.setReviewTime(LocalDateTime.now());
        reviewMapper.insert(review);
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", itemId);
        result.put("status", status);
        result.put("reviewTime", review.getReviewTime());
        
        return result;
    }
    
    /**
     * 批量审核
     */
    public Map<String, Object> batchReview(List<Long> itemIds, String status, String reason, String type, Long reviewerId) {
        int successCount = 0;
        int failCount = 0;
        
        for (Long itemId : itemIds) {
            try {
                reviewItem(itemId, status, reason, type, reviewerId);
                successCount++;
            } catch (Exception e) {
                log.error("批量审核失败，物品ID: {}", itemId, e);
                failCount++;
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("totalCount", itemIds.size());
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        
        return result;
    }
    
    /**
     * 获取待审核数量
     */
    public Long getPendingCount() {
        Integer count = reviewMapper.getPendingCount();
        return count != null ? count.longValue() : 0L;
    }
    
    /**
     * 获取审核历史
     */
    public IPage<Map<String, Object>> getReviewHistory(int current, int pageSize, String type, String status, Long reviewerId) {
        Page<Review> page = new Page<>(current, pageSize);
        return reviewMapper.getReviewHistory(page, type, status, reviewerId);
    }
    
    /**
     * 获取审核统计
     */
    public Map<String, Object> getReviewStats(Long reviewerId) {
        Map<String, Object> stats = reviewMapper.getReviewStats(reviewerId);
        if (stats == null) {
            stats = new HashMap<>();
            stats.put("totalReviewed", 0);
            stats.put("approved", 0);
            stats.put("rejected", 0);
            stats.put("pending", 0);
        }
        
        // 计算通过率
        Integer totalReviewed = (Integer) stats.get("totalReviewed");
        Integer approved = (Integer) stats.get("approved");
        if (totalReviewed != null && totalReviewed > 0 && approved != null) {
            double approvalRate = (double) approved / totalReviewed * 100;
            stats.put("approvalRate", Math.round(approvalRate * 10.0) / 10.0);
        } else {
            stats.put("approvalRate", 0.0);
        }
        
        return stats;
    }
}