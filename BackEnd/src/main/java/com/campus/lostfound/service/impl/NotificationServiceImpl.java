package com.campus.lostfound.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.lostfound.common.constants.StatusConstants;
import com.campus.lostfound.entity.Notification;
import com.campus.lostfound.mapper.NotificationMapper;
import com.campus.lostfound.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 通知服务实现类
 */
@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements NotificationService {
    
    @Override
    public IPage<Notification> getNotificationPage(Long userId, int current, int size, String type, Integer isRead) {
        if (userId == null) {
            return new Page<>();
        }
        
        Page<Notification> page = new Page<>(current, size);
        return baseMapper.selectNotificationPage(page, userId, type, isRead);
    }
    
    @Override
    public int getUnreadCount(Long userId) {
        if (userId == null) {
            return 0;
        }
        return baseMapper.getUnreadCount(userId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createNotification(Long userId, String title, String content, String type, Long relatedItemId) {
        if (userId == null || title == null || content == null) {
            return false;
        }
        
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type != null ? type : StatusConstants.NOTIFICATION_TYPE_SYSTEM);
        notification.setIsRead(StatusConstants.NOTIFICATION_UNREAD);
        notification.setRelatedItemId(relatedItemId);
        
        return save(notification);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchCreateNotification(List<Long> userIds, String title, String content, String type, Long relatedItemId) {
        if (userIds == null || userIds.isEmpty() || title == null || content == null) {
            return false;
        }
        
        for (Long userId : userIds) {
            createNotification(userId, title, content, type, relatedItemId);
        }
        
        return true;
    }
    
    @Override
    public boolean markAsRead(Long notificationId) {
        if (notificationId == null) {
            return false;
        }
        
        return baseMapper.markAsRead(notificationId) > 0;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchMarkAsRead(Long userId, List<Long> notificationIds) {
        if (userId == null || notificationIds == null || notificationIds.isEmpty()) {
            return false;
        }
        
        return baseMapper.batchMarkAsRead(userId, notificationIds) > 0;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean markAllAsRead(Long userId) {
        if (userId == null) {
            return false;
        }
        
        return lambdaUpdate()
            .eq(Notification::getUserId, userId)
            .eq(Notification::getIsRead, StatusConstants.NOTIFICATION_UNREAD)
            .set(Notification::getIsRead, StatusConstants.NOTIFICATION_READ)
            .set(Notification::getUpdatedAt, LocalDateTime.now())
            .update();
    }
    
    @Override
    public boolean deleteNotification(Long notificationId) {
        if (notificationId == null) {
            return false;
        }
        
        return removeById(notificationId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDeleteNotifications(Long userId, List<Long> notificationIds) {
        if (userId == null || notificationIds == null || notificationIds.isEmpty()) {
            return false;
        }
        
        return baseMapper.batchDeleteNotifications(userId, notificationIds) > 0;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean clearAllNotifications(Long userId) {
        if (userId == null) {
            return false;
        }
        
        return lambdaUpdate()
            .eq(Notification::getUserId, userId)
            .set(Notification::getDeleted, StatusConstants.DELETED)
            .update();
    }
}