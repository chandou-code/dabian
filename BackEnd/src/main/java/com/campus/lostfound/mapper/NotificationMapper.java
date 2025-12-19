package com.campus.lostfound.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.lostfound.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 通知Mapper接口
 */
@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {

    /**
     * 分页查询通知
     */
    IPage<Notification> selectNotificationPage(Page<Notification> page, @Param("userId") Long userId,
                                           @Param("type") String type, @Param("isRead") Integer isRead);

    /**
     * 获取未读通知数量
     */
    Integer getUnreadCount(@Param("userId") Long userId);

    /**
     * 批量标记已读
     */
    int batchMarkAsRead(@Param("userId") Long userId, @Param("notificationIds") List<Long> notificationIds);

    /**
     * 标记单个通知已读
     */
    int markAsRead(@Param("notificationId") Long notificationId);

    /**
     * 标记所有已读
     */
    int markAllAsRead(@Param("userId") Long userId);

    /**
     * 创建通知
     */
    int insertNotification(@Param("userId") Long userId, @Param("title") String title,
                        @Param("content") String content, @Param("type") String type,
                        @Param("relatedItemId") Long relatedItemId);

    /**
     * 批量删除通知
     */
    int batchDeleteNotifications(@Param("userId") Long userId, @Param("notificationIds") List<Long> notificationIds);

    /**
     * 清空所有通知
     */
    int clearAllNotifications(@Param("userId") Long userId);
}