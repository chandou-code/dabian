package com.campus.errand.service;

import com.campus.errand.entity.Notification;
import java.util.List;

/**
 * 通知服务接口
 */
public interface NotificationService {
    /**
     * 获取用户通知列表
     * @param userId 用户ID
     * @param type 通知类型
     * @param page 页码
     * @param pageSize 每页数量
     * @return 通知列表
     */
    List<Notification> getUserNotifications(Long userId, String type, Integer page, Integer pageSize);
    
    /**
     * 获取用户未读通知数量
     * @param userId 用户ID
     * @return 未读通知数量
     */
    Integer getUnreadCount(Long userId);
    
    /**
     * 标记通知为已读
     * @param id 通知ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean markAsRead(Long id, Long userId);
    
    /**
     * 标记所有通知为已读
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean markAllAsRead(Long userId);
    
    /**
     * 清除所有通知
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean clearAll(Long userId);
    
    /**
     * 创建通知
     * @param notification 通知对象
     * @return 是否成功
     */
    boolean createNotification(Notification notification);
    
    /**
     * 批量创建通知
     * @param notifications 通知列表
     * @return 是否成功
     */
    boolean createNotifications(List<Notification> notifications);
}
