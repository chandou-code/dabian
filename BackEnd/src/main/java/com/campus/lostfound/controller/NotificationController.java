package com.campus.lostfound.controller;

import com.campus.lostfound.common.Result;
import com.campus.lostfound.common.constants.StatusConstants;
import com.campus.lostfound.entity.Notification;
import com.campus.lostfound.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 通知控制器
 */
@Slf4j
@RestController
@RequestMapping("/notifications")
@CrossOrigin
public class NotificationController {
    
    @Autowired
    private NotificationService notificationService;
    
    @GetMapping
    public Result<Map<String, Object>> getNotifications(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer isRead,
            @RequestHeader("Authorization") String token) {
        
        try {
            // TODO: 从token中获取用户ID
            Long userId = 1L; // 临时硬编码，实际应该从JWT中解析
            
            var page = notificationService.getNotificationPage(userId, current, pageSize, type, isRead);
            
            Map<String, Object> result = Map.of(
                "notifications", page.getRecords(),
                "pagination", Map.of(
                    "current", page.getCurrent(),
                    "pageSize", page.getSize(),
                    "total", page.getTotal(),
                    "pages", page.getPages()
                )
            );
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取通知列表失败", e);
            return Result.error("获取通知列表失败：" + e.getMessage());
        }
    }
    
    @GetMapping("/unread-count")
    public Result<Map<String, Integer>> getUnreadCount(@RequestHeader("Authorization") String token) {
        try {
            // TODO: 从token中获取用户ID
            Long userId = 1L; // 临时硬编码，实际应该从JWT中解析
            
            int count = notificationService.getUnreadCount(userId);
            return Result.success(Map.of("count", count));
        } catch (Exception e) {
            log.error("获取未读通知数量失败", e);
            return Result.error("获取未读数量失败：" + e.getMessage());
        }
    }
    
    @PutMapping("/{id}/read")
    public Result markAsRead(@PathVariable Long id,
                              @RequestHeader("Authorization") String token) {
        try {
            boolean success = notificationService.markAsRead(id);
            if (success) {
                return Result.success("标记成功");
            } else {
                return Result.error("标记失败");
            }
        } catch (Exception e) {
            log.error("标记通知为已读失败", e);
            return Result.error("标记失败：" + e.getMessage());
        }
    }
    
    @PutMapping("/batch-read")
    public Result batchMarkAsRead(@RequestBody Map<String, Object> request,
                                   @RequestHeader("Authorization") String token) {
        try {
            // TODO: 从token中获取用户ID
            Long userId = 1L; // 临时硬编码，实际应该从JWT中解析
            
            @SuppressWarnings("unchecked")
            List<Long> notificationIds = ((List<Integer>) request.get("notificationIds"))
                .stream()
                .map(Long::valueOf)
                .toList();
            
            boolean success = notificationService.batchMarkAsRead(userId, notificationIds);
            if (success) {
                return Result.success("批量标记成功");
            } else {
                return Result.error("批量标记失败");
            }
        } catch (Exception e) {
            log.error("批量标记通知为已读失败", e);
            return Result.error("批量标记失败：" + e.getMessage());
        }
    }
    
    @PutMapping("/all-read")
    public Result markAllAsRead(@RequestHeader("Authorization") String token) {
        try {
            // TODO: 从token中获取用户ID
            Long userId = 1L; // 临时硬编码，实际应该从JWT中解析
            
            boolean success = notificationService.markAllAsRead(userId);
            if (success) {
                return Result.success("标记成功");
            } else {
                return Result.error("标记失败");
            }
        } catch (Exception e) {
            log.error("标记所有通知为已读失败", e);
            return Result.error("标记失败：" + e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public Result deleteNotification(@PathVariable Long id,
                                    @RequestHeader("Authorization") String token) {
        try {
            boolean success = notificationService.deleteNotification(id);
            if (success) {
                return Result.success("删除成功");
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            log.error("删除通知失败", e);
            return Result.error("删除失败：" + e.getMessage());
        }
    }
    
    @DeleteMapping("/batch")
    public Result batchDeleteNotifications(@RequestBody Map<String, Object> request,
                                          @RequestHeader("Authorization") String token) {
        try {
            // TODO: 从token中获取用户ID
            Long userId = 1L; // 临时硬编码，实际应该从JWT中解析
            
            @SuppressWarnings("unchecked")
            List<Long> notificationIds = ((List<Integer>) request.get("notificationIds"))
                .stream()
                .map(Long::valueOf)
                .toList();
            
            boolean success = notificationService.batchDeleteNotifications(userId, notificationIds);
            if (success) {
                return Result.success("批量删除成功");
            } else {
                return Result.error("批量删除失败");
            }
        } catch (Exception e) {
            log.error("批量删除通知失败", e);
            return Result.error("批量删除失败：" + e.getMessage());
        }
    }
    
    @DeleteMapping("/all")
    public Result clearAllNotifications(@RequestHeader("Authorization") String token) {
        try {
            // TODO: 从token中获取用户ID
            Long userId = 1L; // 临时硬编码，实际应该从JWT中解析
            
            boolean success = notificationService.clearAllNotifications(userId);
            if (success) {
                return Result.success("清空成功");
            } else {
                return Result.error("清空失败");
            }
        } catch (Exception e) {
            log.error("清空所有通知失败", e);
            return Result.error("清空失败：" + e.getMessage());
        }
    }
    
    @PostMapping
    public Result createNotification(@RequestBody Map<String, Object> request,
                                    @RequestHeader("Authorization") String token) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            String title = (String) request.get("title");
            String content = (String) request.get("content");
            String type = (String) request.getOrDefault("type", StatusConstants.NOTIFICATION_TYPE_SYSTEM);
            Long relatedItemId = null;
            if (request.containsKey("relatedItemId")) {
                relatedItemId = Long.valueOf(request.get("relatedItemId").toString());
            }
            
            boolean success = notificationService.createNotification(userId, title, content, type, relatedItemId);
            if (success) {
                return Result.success("通知创建成功");
            } else {
                return Result.error("通知创建失败");
            }
        } catch (Exception e) {
            log.error("创建通知失败", e);
            return Result.error("创建失败：" + e.getMessage());
        }
    }
}