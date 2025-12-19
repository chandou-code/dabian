package com.campus.lostfound.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.lostfound.entity.Item;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 物品Mapper接口
 */
@Mapper
public interface ItemMapper extends BaseMapper<Item> {

    /**
     * 分页查询物品
     */
    IPage<Item> selectItemPage(Page<Item> page, @Param("type") String type, 
                            @Param("category") String category, @Param("status") String status,
                            @Param("keyword") String keyword, @Param("location") String location,
                            @Param("submitterId") Long submitterId);

    /**
     * 分页查询待审核物品
     */
    IPage<Item> selectPendingItems(Page<Item> page, @Param("type") String type, 
                                 @Param("status") String status, @Param("keyword") String keyword);

    /**
     * 搜索物品
     */
    IPage<Item> searchItems(Page<Item> page, @Param("keyword") String keyword, @Param("type") String type,
                          @Param("category") String category, @Param("timeRange") String timeRange);

    /**
     * 获取物品总数
     */
    Long getItemCount(@Param("type") String type, @Param("status") String status, @Param("submitterId") Long submitterId);

    /**
     * 获取用户最近活动
     */
    List<Item> selectUserRecentActivities(@Param("userId") Long userId, @Param("limit") Integer limit);

    /**
     * 获取推荐匹配
     */
    List<Item> selectRecommendedMatches(@Param("userId") Long userId);

    /**
     * 获取管理员统计数据
     */
    Map<String, Object> selectAdminStatistics(@Param("startDate") String startDate, 
                                         @Param("endDate") String endDate, @Param("timeRange") String timeRange);

    /**
     * 获取用户统计数据
     */
    Map<String, Object> selectUserStatistics(@Param("userId") Long userId);

    /**
     * 获取待审核物品数量
     */
    Long getPendingReviewCount();

    /**
     * 标记物品已找到
     */
    int markItemFound(@Param("id") Long id, @Param("matchedItemId") Long matchedItemId);

    /**
     * 标记物品已领回
     */
    int markItemRecovered(@Param("id") Long id);
    
    /**
     * 统计物品数量
     */
    int countItems(@Param("type") String type, @Param("status") String status, @Param("submitterId") Long submitterId);
    
    /**
     * 获取最近活动
     */
    List<Item> selectRecentActivities(@Param("userId") Long userId, @Param("limit") Integer limit);
    
    /**
     * 更新浏览次数
     */
    int updateViewCount(@Param("id") Long id);
    
    /**
     * 获取类别统计
     */
    List<Map<String, Object>> getCategoryStatistics();
}