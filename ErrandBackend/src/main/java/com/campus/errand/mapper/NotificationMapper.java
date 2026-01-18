package com.campus.errand.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.errand.entity.Notification;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 通知Mapper接口
 */
public interface NotificationMapper extends BaseMapper<Notification> {
    /**
     * 获取用户通知列表
     * @param userId 用户ID
     * @param type 通知类型
     * @param limit 分页大小
     * @param offset 偏移量
     * @return 通知列表
     */
    List<Notification> getUserNotifications(@Param("userId") Long userId, @Param("type") String type, @Param("limit") Integer limit, @Param("offset") Integer offset);
    
    /**
     * 获取用户未读通知数量
     * @param userId 用户ID
     * @return 未读通知数量
     */
    Integer getUnreadCount(@Param("userId") Long userId);
    
    /**
     * 标记通知为已读
     * @param id 通知ID
     * @param userId 用户ID
     * @return 影响行数
     */
    Integer markAsRead(@Param("id") Long id, @Param("userId") Long userId);
    
    /**
     * 标记所有通知为已读
     * @param userId 用户ID
     * @return 影响行数
     */
    Integer markAllAsRead(@Param("userId") Long userId);
    
    /**
     * 清除所有通知
     * @param userId 用户ID
     * @return 影响行数
     */
    Integer clearAll(@Param("userId") Long userId);
}
