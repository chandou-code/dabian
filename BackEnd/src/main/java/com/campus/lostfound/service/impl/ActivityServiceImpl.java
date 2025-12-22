package com.campus.lostfound.service.impl;

import com.campus.lostfound.dto.DashboardDTO;
import com.campus.lostfound.entity.Item;
import com.campus.lostfound.mapper.ItemMapper;
import com.campus.lostfound.service.ActivityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 活动服务实现类
 */
@Slf4j
@Service
public class ActivityServiceImpl implements ActivityService {
    
    @Autowired
    private ItemMapper itemMapper;
    
    @Override
    public List<DashboardDTO.ActivityDTO> getUserRecentActivities(Long userId, Integer limit) {
        return getUserRecentActivities(userId, 1, limit);
    }
    
    @Override
    public List<DashboardDTO.ActivityDTO> getUserRecentActivities(Long userId, Integer page, Integer size) {
        List<DashboardDTO.ActivityDTO> activities = new ArrayList<>();
        
        try {
            // 获取用户最近的物品记录
            int offset = (page - 1) * size;
            List<Item> recentItems = itemMapper.selectUserRecentActivities(userId, size);
            
            for (Item item : recentItems) {
                DashboardDTO.ActivityDTO activity = convertItemToActivity(item);
                activities.add(activity);
            }
            
            log.info("获取用户{}最近活动成功，共{}条记录", userId, activities.size());
            
        } catch (Exception e) {
            log.error("获取用户最近活动失败", e);
        }
        
        return activities;
    }
    
    @Override
    public DashboardDTO.ActivityDTO createActivity(DashboardDTO.ActivityDTO activity) {
        // 这里可以将活动记录保存到数据库（如果需要的话）
        // 目前主要是生成活动记录用于展示
        return activity;
    }
    
    @Override
    public DashboardDTO.ActivityDTO recordUserActivity(Long userId, String type, String title, 
                                                      String description, Long relatedItemId) {
        DashboardDTO.ActivityDTO activity = new DashboardDTO.ActivityDTO();
        activity.setId(System.currentTimeMillis()); // 临时ID
        activity.setType(type);
        activity.setTitle(title);
        activity.setDescription(description);
        activity.setRelatedItemId(relatedItemId);
        activity.setTime(LocalDateTime.now());
        activity.setIcon(getActivityIcon(type));
        activity.setStatus(getActivityStatus(type));
        
        return createActivity(activity);
    }
    
    /**
     * 将物品记录转换为活动记录
     */
    private DashboardDTO.ActivityDTO convertItemToActivity(Item item) {
        DashboardDTO.ActivityDTO activity = new DashboardDTO.ActivityDTO();
        activity.setId(item.getId());
        activity.setRelatedItemId(item.getId());
        activity.setTime(item.getCreatedAt());
        
        // 根据物品类型和状态设置活动信息
        if ("lost".equals(item.getType())) {
            activity.setType("publish");
            activity.setTitle("发布了失物信息");
            activity.setIcon("📝");
        } else if ("found".equals(item.getType())) {
            activity.setType("publish");
            activity.setTitle("发布了招领信息");
            activity.setIcon("✅");
        }
        
        activity.setDescription(item.getItemName() + " - " + item.getLocation());
        
        // 设置状态
        switch (item.getStatus()) {
            case "pending":
                activity.setStatus("pending");
                break;
            case "approved":
                activity.setStatus("approved");
                break;
            case "rejected":
                activity.setStatus("rejected");
                break;
            case "claimed":
                activity.setStatus("recovered");
                activity.setIcon("🎉");
                activity.setTitle("物品已找回");
                break;
            default:
                activity.setStatus("pending");
        }
        
        return activity;
    }
    
    /**
     * 根据活动类型获取图标
     */
    private String getActivityIcon(String type) {
        switch (type) {
            case "publish":
                return "📝";
            case "review":
                return "💬";
            case "claim":
                return "🎉";
            case "match":
                return "🎯";
            default:
                return "📋";
        }
    }
    
    /**
     * 根据活动类型获取状态
     */
    private String getActivityStatus(String type) {
        switch (type) {
            case "publish":
                return "approved";
            case "review":
                return "pending";
            case "claim":
                return "recovered";
            case "match":
                return "approved";
            default:
                return "pending";
        }
    }
}