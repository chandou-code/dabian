package com.campus.lostfound.controller;

import com.campus.lostfound.common.Result;
import com.campus.lostfound.enums.ReviewAction;
import com.campus.lostfound.service.ItemService;
import com.campus.lostfound.service.IReviewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 审核控制器
 */
@Slf4j
@RestController
@RequestMapping("/review")
@CrossOrigin
public class ReviewController {
    
    @Autowired
    private ItemService itemService;
    
    @Autowired
    private IReviewService reviewService;
    
    @GetMapping("/pending")
    public Result<Map<String, Object>> getPendingItems(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestHeader("Authorization") String token) {
        
        try {
            Long reviewerId = 2L;
            String reviewerName = "审核员";
            
            var page = itemService.getPendingItems(current, pageSize, type, status, keyword);
            
            Map<String, Object> result = Map.of(
                "items", page.getRecords(),
                "pagination", Map.of(
                    "current", page.getCurrent(),
                    "pageSize", page.getSize(),
                    "total", page.getTotal(),
                    "pages", page.getPages()
                )
            );
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取待审核列表失败", e);
            return Result.error("获取待审核列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 审核物品信息
     */
    @PutMapping("/{id}")
    public Result<Map<String, Object>> reviewItem(
            @PathVariable Long id,
            @RequestBody Map<String, Object> reviewData,
            @RequestHeader("Authorization") String token) {
        
        try {
            Long reviewerId = 2L; // TODO: 从JWT解析审核员ID
            String status = (String) reviewData.get("status");
            String reason = (String) reviewData.get("reason");
            String type = (String) reviewData.get("type");
            
            if (!"approved".equals(status) && !"rejected".equals(status)) {
                return Result.error("审核状态只能是approved或rejected");
            }
            
            // 将status转换为ReviewAction枚举
            ReviewAction action = ReviewAction.valueOf(status);
            
            Map<String, Object> result = reviewService.reviewItem(
                id, status, reason, action, reviewerId
            );
            
            return Result.success("审核成功", result);
        } catch (Exception e) {
            log.error("审核物品失败", e);
            return Result.error("审核失败：" + e.getMessage());
        }
    }
    
    /**
     * 批量审核
     */
    @PutMapping("/batch")
    public Result<Map<String, Object>> batchReview(
            @RequestBody Map<String, Object> batchData,
            @RequestHeader("Authorization") String token) {
        
        try {
            Long reviewerId = 2L; // TODO: 从JWT解析审核员ID
            @SuppressWarnings("unchecked")
            List<Long> itemIdsList = (List<Long>) batchData.get("itemIds");
            String status = (String) batchData.get("status");
            String reason = (String) batchData.get("reason");
            String type = (String) batchData.get("type");
            
            // 将List<Long>转换为Long[]
            Long[] itemIds = itemIdsList.toArray(new Long[0]);
            
            // 将status转换为ReviewAction枚举
            ReviewAction action = ReviewAction.valueOf(status);
            
            Map<String, Object> result = reviewService.batchReview(
                itemIds, status, reason, action, reviewerId
            );
            
            return Result.success("批量审核成功", result);
        } catch (Exception e) {
            log.error("批量审核失败", e);
            return Result.error("批量审核失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取审核历史
     */
    @GetMapping("/history")
    public Result<Map<String, Object>> getReviewHistory(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestHeader("Authorization") String token) {
        
        try {
            Long reviewerId = 2L; // TODO: 从JWT解析审核员ID
            
            var page = reviewService.getReviewHistory(current, pageSize, type, status, reviewerId);
            
            Map<String, Object> result = Map.of(
                "items", page.getRecords(),
                "pagination", Map.of(
                    "current", page.getCurrent(),
                    "pageSize", page.getSize(),
                    "total", page.getTotal(),
                    "pages", page.getPages()
                )
            );
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取审核历史失败", e);
            return Result.error("获取审核历史失败：" + e.getMessage());
        }
    }
    
    @GetMapping("/test")
    public Result<Map<String, String>> test() {
        Map<String, String> data = Map.of("message", "ReviewController正常工作", "status", "ok");
        return Result.success("测试成功", data);
    }
    
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getReviewerDashboard(@RequestHeader("Authorization") String token) {
        try {
            // TODO: 从JWT解析审核员ID
            Long reviewerId = 2L;
            
            // 获取审核员统计数据
            Map<String, Object> stats = itemService.getReviewerStatistics(reviewerId);
            
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取审核员仪表板数据失败", e);
            return Result.error("获取仪表板数据失败：" + e.getMessage());
        }
    }
}