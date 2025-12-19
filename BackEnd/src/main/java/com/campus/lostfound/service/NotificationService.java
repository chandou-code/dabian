package com.campus.lostfound.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.lostfound.entity.Notification;

import java.util.List;

/**
 * 通知服务接口
 */
public interface NotificationService extends IService<Notification> {
    
    /**
     * 分页查询用户通知
     */
    IPage<Notification> getNotificationPage(Long userId, int current, int size, String type, Integer isRead);
    
    /**
     * 获取未读通知数量
     */
    int getUnreadCount(Long userId);
    
    /**
     * 创建通知
     */
    boolean createNotification(Long userId, String title, String content, String type, Long relatedItemId);
    
    /**
     * 批量创建通知
     */
    boolean batchCreateNotification(List<Long> userIds, String title, String content, String type, Long relatedItemId);
    
    /**
     * 标记通知为已读
     */
    boolean markAsRead(Long notificationId);
    
    /**
     * 批量标记为已读
     */
    boolean batchMarkAsRead(Long userId, List<Long> notificationIds);
    
    /**
     * 标记所有通知为已读
     */
    boolean markAllAsRead(Long userId);
    
    /**
     * 删除通知
     */
    boolean deleteNotification(Long notificationId);
    
    /**
     * 批量删除通知
     */
    boolean batchDeleteNotifications(Long userId, List<Long> notificationIds);
    
    /**
     * 清空所有通知
     */
    boolean clearAllNotifications(Long userId);
}