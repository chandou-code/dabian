package com.campus.lostfound.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.lostfound.entity.Review;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 审核记录Mapper接口
 */
@Mapper
public interface ReviewMapper extends BaseMapper<Review> {
    
    /**
     * 获取审核历史
     */
    @Select("<script>" +
            "SELECT " +
            "r.id, r.item_id, r.reviewer_id, r.action, r.reason, r.review_time, " +
            "i.item_name, i.category, i.submitter_id, u.username as user_name " +
            "FROM reviews r " +
            "LEFT JOIN items i ON r.item_id = i.id " +
            "LEFT JOIN users u ON i.submitter_id = u.id " +
            "WHERE r.reviewer_id = #{reviewerId} " +
            "<if test='type != null and type != \"\"'>" +
            "AND r.action = #{type} " +
            "</if>" +
            "<if test='status != null and status != \"\"'>" +
            "AND r.action = #{status} " +
            "</if>" +
            "ORDER BY r.review_time DESC" +
            "</script>")
    IPage<Map<String, Object>> getReviewHistory(Page<Review> page, 
                                              @Param("type") String type, 
                                              @Param("status") String status,
                                              @Param("reviewerId") Long reviewerId);
    
    /**
     * 获取审核统计
     */
    @Select("SELECT " +
            "COUNT(*) as totalReviewed, " +
            "SUM(CASE WHEN action = 'approved' THEN 1 ELSE 0 END) as approved, " +
            "SUM(CASE WHEN action = 'rejected' THEN 1 ELSE 0 END) as rejected, " +
            "(SELECT COUNT(*) FROM items WHERE status = 'pending') as pending " +
            "FROM reviews WHERE reviewer_id = #{reviewerId}")
    Map<String, Object> getReviewStats(@Param("reviewerId") Long reviewerId);
    
    /**
     * 获取审核员今日审核数量
     */
    @Select("SELECT COUNT(*) FROM reviews " +
            "WHERE reviewer_id = #{reviewerId} " +
            "AND DATE(review_time) = CURDATE()")
    Integer getTodayReviewCount(@Param("reviewerId") Long reviewerId);
    
    /**
     * 获取待审核数量
     */
    @Select("SELECT COUNT(*) FROM items WHERE status = 'pending'")
    Integer getPendingCount();
    
    /**
     * 获取本周审核数量
     */
    @Select("SELECT COUNT(*) FROM reviews " +
            "WHERE reviewer_id = #{reviewerId} " +
            "AND DATE(review_time) >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)")
    Integer getWeeklyReviewCount(@Param("reviewerId") Long reviewerId);
    
    /**
     * 获取每日审核趋势
     */
    @Select("SELECT "+ 
            "DATE(review_time) as date, " +
            "COUNT(*) as total, " +
            "SUM(CASE WHEN action = 'approved' THEN 1 ELSE 0 END) as approved, " +
            "SUM(CASE WHEN action = 'rejected' THEN 1 ELSE 0 END) as rejected " +
            "FROM reviews " +
            "WHERE reviewer_id = #{reviewerId} " +
            "AND review_time >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) " +
            "GROUP BY DATE(review_time) " +
            "ORDER BY date")
    List<Map<String, Object>> getDailyReviewTrend(@Param("reviewerId") Long reviewerId);
    
    /**
     * 获取今日审核排名
     */
    @Select("SELECT COUNT(*) + 1 as review_rank FROM (SELECT reviewer_id, COUNT(*) as count FROM reviews WHERE DATE(review_time) = CURDATE() GROUP BY reviewer_id HAVING count > (SELECT COUNT(*) FROM reviews WHERE reviewer_id = #{reviewerId} AND DATE(review_time) = CURDATE())) as temp")
    Integer getTodayRank(@Param("reviewerId") Long reviewerId);
    
    /**
     * 获取本周审核排名
     */
    @Select("SELECT COUNT(*) + 1 as review_rank FROM (SELECT reviewer_id, COUNT(*) as count FROM reviews WHERE DATE(review_time) >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) GROUP BY reviewer_id HAVING count > (SELECT COUNT(*) FROM reviews WHERE reviewer_id = #{reviewerId} AND DATE(review_time) >= DATE_SUB(CURDATE(), INTERVAL 7 DAY))) as temp")
    Integer getWeeklyRank(@Param("reviewerId") Long reviewerId);
    
    /**
     * 获取本月审核排名
     */
    @Select("SELECT COUNT(*) + 1 as review_rank FROM (SELECT reviewer_id, COUNT(*) as count FROM reviews WHERE DATE(review_time) >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) GROUP BY reviewer_id HAVING count > (SELECT COUNT(*) FROM reviews WHERE reviewer_id = #{reviewerId} AND DATE(review_time) >= DATE_SUB(CURDATE(), INTERVAL 30 DAY))) as temp")
    Integer getMonthlyRank(@Param("reviewerId") Long reviewerId);
    
    /**
     * 获取本月审核数量
     */
    @Select("SELECT COUNT(*) FROM reviews " +
            "WHERE reviewer_id = #{reviewerId} " +
            "AND DATE(review_time) >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)")
    Integer getMonthlyReviewCount(@Param("reviewerId") Long reviewerId);
}