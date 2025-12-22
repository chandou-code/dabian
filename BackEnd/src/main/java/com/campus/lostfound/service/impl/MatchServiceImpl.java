package com.campus.lostfound.service.impl;

import com.campus.lostfound.dto.DashboardDTO;
import com.campus.lostfound.entity.Item;
import com.campus.lostfound.mapper.ItemMapper;
import com.campus.lostfound.service.MatchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 匹配服务实现类
 */
@Slf4j
@Service
public class MatchServiceImpl implements MatchService {
    
    @Autowired
    private ItemMapper itemMapper;
    
    @Override
    public List<DashboardDTO.MatchItemDTO> getRecommendedMatches(Long userId) {
        return getRecommendedMatches(userId, 1, 10);
    }
    
    @Override
    public List<DashboardDTO.MatchItemDTO> getRecommendedMatches(Long userId, Integer page, Integer size) {
        List<DashboardDTO.MatchItemDTO> matches = new ArrayList<>();
        
        try {
            // 获取用户发布的所有物品（用于匹配）
            List<Item> userItems = itemMapper.selectUserRecentActivities(userId, 100);
            
            // 获取其他用户发布的已审核物品
            List<Item> candidateItems = itemMapper.selectRecommendedMatches(userId);
            
            // 计算匹配分数
            for (Item candidate : candidateItems) {
                for (Item userItem : userItems) {
                    // 只对相反类型的物品进行匹配（失物匹配招领）
                    if (!candidate.getType().equals(userItem.getType())) {
                        int score = calculateMatchScore(userItem, candidate);
                        
                        if (score >= 60) { // 匹配分数阈值
                            DashboardDTO.MatchItemDTO match = convertToMatchItemDTO(candidate, score, userItem.getId());
                            matches.add(match);
                        }
                    }
                }
            }
            
            // 按匹配分数排序并分页
            matches.sort((a, b) -> Integer.compare(b.getScore(), a.getScore()));
            
            int startIndex = (page - 1) * size;
            int endIndex = Math.min(startIndex + size, matches.size());
            
            if (startIndex < matches.size()) {
                matches = matches.subList(startIndex, endIndex);
            } else {
                matches = new ArrayList<>();
            }
            
            log.info("为用户{}获取推荐匹配成功，共{}条记录", userId, matches.size());
            
        } catch (Exception e) {
            log.error("获取推荐匹配失败", e);
        }
        
        return matches;
    }
    
    @Override
    public int calculateMatchScore(Object lostItemObj, Object foundItemObj) {
        if (!(lostItemObj instanceof Item) || !(foundItemObj instanceof Item)) {
            return 0;
        }
        
        Item lostItem = (Item) lostItemObj;
        Item foundItem = (Item) foundItemObj;
        
        int score = 0;
        
        // 1. 类别匹配（最高权重40分）
        if (lostItem.getCategory() != null && lostItem.getCategory().equals(foundItem.getCategory())) {
            score += 40;
        }
        
        // 2. 位置匹配（高权重30分）
        if (lostItem.getLocation() != null && foundItem.getLocation() != null) {
            String lostLocation = lostItem.getLocation().toLowerCase();
            String foundLocation = foundItem.getLocation().toLowerCase();
            
            if (lostLocation.equals(foundLocation)) {
                score += 30;
            } else if (lostLocation.contains(foundLocation) || foundLocation.contains(lostLocation)) {
                score += 20;
            } else if (hasLocationKeywordMatch(lostLocation, foundLocation)) {
                score += 10;
            }
        }
        
        // 3. 时间匹配（中权重15分）
        if (lostItem.getEventTime() != null && foundItem.getEventTime() != null) {
            long timeDiff = Math.abs(java.sql.Date.valueOf(lostItem.getEventTime()).getTime() - 
                                  java.sql.Date.valueOf(foundItem.getEventTime()).getTime());
            long daysDiff = timeDiff / (1000 * 60 * 60 * 24);
            
            if (daysDiff <= 1) {
                score += 15;
            } else if (daysDiff <= 3) {
                score += 10;
            } else if (daysDiff <= 7) {
                score += 5;
            }
        }
        
        // 4. 描述匹配（中权重10分）
        if (lostItem.getDescription() != null && foundItem.getDescription() != null) {
            String lostDesc = lostItem.getDescription().toLowerCase();
            String foundDesc = foundItem.getDescription().toLowerCase();
            
            if (hasKeywordMatch(lostDesc, foundDesc)) {
                score += 10;
            }
        }
        
        // 5. 物品名称匹配（低权重5分）
        if (lostItem.getItemName() != null && foundItem.getItemName() != null) {
            String lostName = lostItem.getItemName().toLowerCase();
            String foundName = foundItem.getItemName().toLowerCase();
            
            if (hasKeywordMatch(lostName, foundName)) {
                score += 5;
            }
        }
        
        return Math.min(score, 100);
    }
    
    @Override
    public List<DashboardDTO.MatchItemDTO> getAiRecommendedMatches(Long userId) {
        // 这里可以集成AI服务进行更智能的匹配
        // 目前返回基础匹配结果
        return getRecommendedMatches(userId);
    }
    
    /**
     * 将Item转换为MatchItemDTO
     */
    private DashboardDTO.MatchItemDTO convertToMatchItemDTO(Item item, int score, Long relatedItemId) {
        DashboardDTO.MatchItemDTO match = new DashboardDTO.MatchItemDTO();
        match.setId(item.getId());
        match.setTitle(item.getItemName());
        match.setDescription(item.getDescription());
        match.setLocation(item.getLocation());
        match.setScore(score);
        match.setType(item.getType());
        match.setRelatedItemId(relatedItemId);
        
        // 处理图片
        if (item.getImages() != null && !item.getImages().isEmpty()) {
            // 如果images是JSON数组，取第一张图片
            match.setImage(item.getImages().toString());
        } else {
            match.setImage("/static/logo.png"); // 默认图片
        }
        
        return match;
    }
    
    /**
     * 检查位置关键词匹配
     */
    private boolean hasLocationKeywordMatch(String location1, String location2) {
        String[] commonKeywords = {"图书馆", "食堂", "宿舍", "教学楼", "操场", "实验室", "教室"};
        
        for (String keyword : commonKeywords) {
            if (location1.contains(keyword) && location2.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 检查关键词匹配
     */
    private boolean hasKeywordMatch(String text1, String text2) {
        String[] words1 = text1.split("\\s+");
        String[] words2 = text2.split("\\s+");
        
        for (String word1 : words1) {
            for (String word2 : words2) {
                if (word1.length() > 2 && word2.length() > 2 && 
                    (word1.contains(word2) || word2.contains(word1))) {
                    return true;
                }
            }
        }
        return false;
    }
}