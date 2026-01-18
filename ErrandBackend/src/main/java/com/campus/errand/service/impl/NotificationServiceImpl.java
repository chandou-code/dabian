package com.campus.errand.service.impl;

import com.campus.errand.entity.Notification;
import com.campus.errand.mapper.NotificationMapper;
import com.campus.errand.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 通知服务实现类
 */
@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationMapper notificationMapper;

    @Override
    public List<Notification> getUserNotifications(Long userId, String type, Integer page, Integer pageSize) {
        try {
            int offset = (page - 1) * pageSize;
            return notificationMapper.getUserNotifications(userId, type, pageSize, offset);
        } catch (Exception e) {
            log.error("获取用户通知列表失败", e);
            return null;
        }
    }

    @Override
    public Integer getUnreadCount(Long userId) {
        try {
            return notificationMapper.getUnreadCount(userId);
        } catch (Exception e) {
            log.error("获取用户未读通知数量失败", e);
            return 0;
        }
    }

    @Override
    public boolean markAsRead(Long id, Long userId) {
        try {
            int result = notificationMapper.markAsRead(id, userId);
            return result > 0;
        } catch (Exception e) {
            log.error("标记通知为已读失败", e);
            return false;
        }
    }

    @Override
    public boolean markAllAsRead(Long userId) {
        try {
            int result = notificationMapper.markAllAsRead(userId);
            return result > 0;
        } catch (Exception e) {
            log.error("标记所有通知为已读失败", e);
            return false;
        }
    }

    @Override
    public boolean clearAll(Long userId) {
        try {
            int result = notificationMapper.clearAll(userId);
            return result > 0;
        } catch (Exception e) {
            log.error("清除所有通知失败", e);
            return false;
        }
    }

    @Override
    public boolean createNotification(Notification notification) {
        try {
            notification.setRead(false);
            notification.setCreatedAt(new Date());
            int result = notificationMapper.insert(notification);
            return result > 0;
        } catch (Exception e) {
            log.error("创建通知失败", e);
            return false;
        }
    }

    @Override
    public boolean createNotifications(List<Notification> notifications) {
        try {
            int result = 0;
            for (Notification notification : notifications) {
                notification.setRead(false);
                notification.setCreatedAt(new Date());
                result += notificationMapper.insert(notification);
            }
            return result > 0;
        } catch (Exception e) {
            log.error("批量创建通知失败", e);
            return false;
        }
    }
}
