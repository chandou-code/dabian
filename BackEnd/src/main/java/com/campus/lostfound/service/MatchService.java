package com.campus.lostfound.service;

import com.campus.lostfound.dto.DashboardDTO;
import java.util.List;

/**
 * 匹配服务接口
 */
public interface MatchService {
    
    /**
     * 获取用户推荐匹配
     * @param userId 用户ID
     * @return 推荐匹配列表
     */
    List<DashboardDTO.MatchItemDTO> getRecommendedMatches(Long userId);
    
    /**
     * 获取用户推荐匹配（分页）
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 分页推荐匹配列表
     */
    List<DashboardDTO.MatchItemDTO> getRecommendedMatches(Long userId, Integer page, Integer size);
    
    /**
     * 计算两个物品的匹配分数
     * @param lostItem 失物
     * @param foundItem 招领
     * @return 匹配分数（0-100）
     */
    int calculateMatchScore(Object lostItem, Object foundItem);
    
    /**
     * 基于AI的智能匹配
     * @param userId 用户ID
     * @return 智能匹配结果
     */
    List<DashboardDTO.MatchItemDTO> getAiRecommendedMatches(Long userId);
}