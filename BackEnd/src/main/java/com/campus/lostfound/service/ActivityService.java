package com.campus.lostfound.service;

import com.campus.lostfound.dto.DashboardDTO;
import java.util.List;

/**
 * 活动服务接口
 */
public interface ActivityService {
    
    /**
     * 获取用户最近活动
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 活动列表
     */
    List<DashboardDTO.ActivityDTO> getUserRecentActivities(Long userId, Integer limit);
    
    /**
     * 获取用户最近活动（分页）
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 分页活动列表
     */
    List<DashboardDTO.ActivityDTO> getUserRecentActivities(Long userId, Integer page, Integer size);
    
    /**
     * 创建活动记录
     * @param activity 活动信息
     * @return 创建的活动记录
     */
    DashboardDTO.ActivityDTO createActivity(DashboardDTO.ActivityDTO activity);
    
    /**
     * 记录用户行为活动
     * @param userId 用户ID
     * @param type 活动类型
     * @param title 活动标题
     * @param description 活动描述
     * @param relatedItemId 相关物品ID
     * @return 活动记录
     */
    DashboardDTO.ActivityDTO recordUserActivity(Long userId, String type, String title, 
                                              String description, Long relatedItemId);
}