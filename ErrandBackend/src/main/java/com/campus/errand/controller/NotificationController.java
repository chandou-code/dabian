package com.campus.errand.controller;

import com.campus.errand.entity.Notification;
import com.campus.errand.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通知控制器
 */
@Slf4j
@RestController
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    /**
     * 获取通知列表
     */
    @GetMapping
    public Map<String, Object> getNotifications(
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestAttribute(value = "currentUserId", required = false) Long userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (userId == null) {
                response.put("code", 401);
                response.put("msg", "用户未认证");
                response.put("data", null);
                return response;
            }

            List<Notification> notifications = notificationService.getUserNotifications(userId, type, page, pageSize);
            if (notifications == null) {
                notifications = new ArrayList<>();
            }

            // 转换时间格式，便于前端显示
            List<Map<String, Object>> formattedNotifications = new ArrayList<>();
            for (Notification notification : notifications) {
                Map<String, Object> formatted = new HashMap<>();
                formatted.put("id", notification.getId());
                formatted.put("type", notification.getType());
                formatted.put("title", notification.getTitle());
                formatted.put("content", notification.getContent());
                formatted.put("tagName", notification.getTagName());
                formatted.put("taskId", notification.getTaskId());
                formatted.put("noticeId", notification.getNoticeId());
                formatted.put("read", notification.getRead());
                formatted.put("time", formatTime(notification.getCreatedAt()));
                formattedNotifications.add(formatted);
            }

            response.put("code", 200);
            response.put("msg", "获取成功");
            response.put("data", formattedNotifications);
            response.put("hasMore", formattedNotifications.size() >= pageSize);
        } catch (Exception e) {
            log.error("获取通知列表异常", e);
            response.put("code", 500);
            response.put("msg", "获取失败：" + e.getMessage());
            response.put("data", null);
        }
        return response;
    }

    /**
     * 获取未读通知数量
     */
    @GetMapping("/unread-count")
    public Map<String, Object> getUnreadCount(
            @RequestAttribute(value = "currentUserId", required = false) Long userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (userId == null) {
                response.put("code", 401);
                response.put("msg", "用户未认证");
                response.put("data", null);
                return response;
            }

            Integer count = notificationService.getUnreadCount(userId);
            response.put("code", 200);
            response.put("msg", "获取成功");
            response.put("data", count);
        } catch (Exception e) {
            log.error("获取未读通知数量异常", e);
            response.put("code", 500);
            response.put("msg", "获取失败：" + e.getMessage());
            response.put("data", null);
        }
        return response;
    }

    /**
     * 标记通知为已读
     */
    @PutMapping("/{id}/read")
    public Map<String, Object> markAsRead(
            @PathVariable Long id,
            @RequestAttribute(value = "currentUserId", required = false) Long userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (userId == null) {
                response.put("code", 401);
                response.put("msg", "用户未认证");
                response.put("data", null);
                return response;
            }

            boolean success = notificationService.markAsRead(id, userId);
            if (success) {
                response.put("code", 200);
                response.put("msg", "标记成功");
            } else {
                response.put("code", 400);
                response.put("msg", "标记失败");
            }
            response.put("data", null);
        } catch (Exception e) {
            log.error("标记通知为已读异常", e);
            response.put("code", 500);
            response.put("msg", "操作失败：" + e.getMessage());
            response.put("data", null);
        }
        return response;
    }

    /**
     * 标记所有通知为已读
     */
    @PutMapping("/read-all")
    public Map<String, Object> markAllAsRead(
            @RequestAttribute(value = "currentUserId", required = false) Long userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (userId == null) {
                response.put("code", 401);
                response.put("msg", "用户未认证");
                response.put("data", null);
                return response;
            }

            boolean success = notificationService.markAllAsRead(userId);
            if (success) {
                response.put("code", 200);
                response.put("msg", "标记成功");
            } else {
                response.put("code", 400);
                response.put("msg", "标记失败");
            }
            response.put("data", null);
        } catch (Exception e) {
            log.error("标记所有通知为已读异常", e);
            response.put("code", 500);
            response.put("msg", "操作失败：" + e.getMessage());
            response.put("data", null);
        }
        return response;
    }

    /**
     * 清除所有通知
     */
    @DeleteMapping("/clear-all")
    public Map<String, Object> clearAll(
            @RequestAttribute(value = "currentUserId", required = false) Long userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (userId == null) {
                response.put("code", 401);
                response.put("msg", "用户未认证");
                response.put("data", null);
                return response;
            }

            boolean success = notificationService.clearAll(userId);
            if (success) {
                response.put("code", 200);
                response.put("msg", "清除成功");
            } else {
                response.put("code", 400);
                response.put("msg", "清除失败");
            }
            response.put("data", null);
        } catch (Exception e) {
            log.error("清除所有通知异常", e);
            response.put("code", 500);
            response.put("msg", "操作失败：" + e.getMessage());
            response.put("data", null);
        }
        return response;
    }

    /**
     * 格式化时间，便于前端显示
     */
    private String formatTime(java.util.Date date) {
        if (date == null) {
            return "刚刚";
        }

        long now = System.currentTimeMillis();
        long diff = now - date.getTime();

        long minutes = diff / (60 * 1000);
        long hours = diff / (60 * 60 * 1000);
        long days = diff / (24 * 60 * 60 * 1000);

        if (days > 0) {
            return days + "天前";
        } else if (hours > 0) {
            return hours + "小时前";
        } else if (minutes > 0) {
            return minutes + "分钟前";
        } else {
            return "刚刚";
        }
    }
}
