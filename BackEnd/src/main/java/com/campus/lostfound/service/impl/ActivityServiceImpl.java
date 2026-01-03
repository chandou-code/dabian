package com.campus.lostfound.service.impl;

import com.campus.lostfound.dto.DashboardDTO;
import com.campus.lostfound.entity.Clue;
import com.campus.lostfound.entity.Item;
import com.campus.lostfound.mapper.ClueMapper;
import com.campus.lostfound.mapper.ItemMapper;
import com.campus.lostfound.service.ActivityService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 活动服务实现类
 */
@Slf4j
@Service
public class ActivityServiceImpl implements ActivityService {
    
    @Autowired
    private ItemMapper itemMapper;
    
    @Autowired
    private ClueMapper clueMapper;
    
    @Override
    public List<DashboardDTO.ActivityDTO> getUserRecentActivities(Long userId, Integer limit) {
        return getUserRecentActivities(userId, 1, limit);
    }
    
    @Override
    public List<DashboardDTO.ActivityDTO> getUserRecentActivities(Long userId, Integer page, Integer size) {
        List<DashboardDTO.ActivityDTO> activities = new ArrayList<>();
        
        try {
            // 1. 获取用户最近的物品记录（状态变更）
            List<Item> recentItems = itemMapper.selectUserRecentActivities(userId, size);
            
            for (Item item : recentItems) {
                DashboardDTO.ActivityDTO activity = convertItemToActivity(item);
                activities.add(activity);
            }
            
            // 2. 获取用户收到的线索
            List<Clue> receivedClues = getReceivedCluesByUserId(userId, size);
            
            for (Clue clue : receivedClues) {
                DashboardDTO.ActivityDTO activity = convertClueToActivity(clue);
                activities.add(activity);
            }
            
            // 3. 合并并按时间倒序排序
            activities = activities.stream()
                .sorted(Comparator.comparing(DashboardDTO.ActivityDTO::getTime).reversed())
                .collect(Collectors.toList());
            
            // 4. 分页处理
            int offset = (page - 1) * size;
            int end = Math.min(offset + size, activities.size());
            if (offset < activities.size()) {
                activities = activities.subList(offset, end);
            } else {
                activities = new ArrayList<>();
            }
            
            log.info("获取用户{}最近活动成功，共{}条记录", userId, activities.size());
            
        } catch (Exception e) {
            log.error("获取用户最近活动失败", e);
        }
        
        return activities;
    }
    
    /**
     * 获取用户收到的线索
     */
    private List<Clue> getReceivedCluesByUserId(Long userId, Integer size) {
        // 首先获取用户发布的所有物品ID
        List<Item> userItems = itemMapper.selectUserItems(userId);
        List<Long> itemIds = userItems.stream()
            .map(Item::getId)
            .collect(Collectors.toList());
        
        if (itemIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 根据物品ID查询相关线索
        QueryWrapper<Clue> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("item_id", itemIds)
            .orderByDesc("created_at")
            .last("LIMIT " + size);
        
        return clueMapper.selectList(queryWrapper);
    }
    
    /**
     * 将线索转换为活动记录
     */
    private DashboardDTO.ActivityDTO convertClueToActivity(Clue clue) {
        DashboardDTO.ActivityDTO activity = new DashboardDTO.ActivityDTO();
        activity.setId(clue.getId());
        activity.setRelatedItemId(clue.getItemId());
        activity.setTime(clue.getCreatedAt());
        
        // 设置活动信息
        activity.setType("clue");
        activity.setTitle("收到新线索");
        activity.setIcon("💬");
        activity.setDescription("有人为您的物品提供了线索：" + clue.getContent());
        activity.setStatus(clue.getStatus());
        
        return activity;
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
            activity.setIcon("📝");
        } else if ("found".equals(item.getType())) {
            activity.setType("publish");
            activity.setIcon("✅");
        }
        
        activity.setDescription(item.getItemName() + " - " + item.getLocation());
        
        // 设置状态和标题
        switch (item.getStatus()) {
            case "pending":
                activity.setStatus("pending");
                activity.setTitle("发布了失物信息");
                break;
            case "approved":
                activity.setStatus("approved");
                activity.setTitle("发布了失物信息");
                break;
            case "rejected":
                activity.setStatus("rejected");
                activity.setTitle("物品信息已被拒绝");
                activity.setIcon("❌");
                break;
            case "claimed":
                activity.setStatus("recovered");
                activity.setTitle("物品已找回");
                activity.setIcon("🎉");
                break;
            default:
                activity.setStatus("pending");
                activity.setTitle("发布了失物信息");
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