package com.campus.lostfound.controller;

import com.campus.lostfound.common.Result;
import com.campus.lostfound.dto.DashboardDTO;
import com.campus.lostfound.service.ActivityService;
import com.campus.lostfound.service.MatchService;
import com.campus.lostfound.service.StatisticsService;
import com.campus.lostfound.util.JwtUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 统计控制器
 */
@Slf4j
@RestController
@RequestMapping("/statistics")
@CrossOrigin
public class StatisticsController {
    
    @Autowired
    private StatisticsService statisticsService;
    
    @Autowired
    private ActivityService activityService;
    
    @Autowired
    private MatchService matchService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 获取统计数据
     */
    @GetMapping
    public Result<Map<String, Object>> getStatistics(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestHeader("Authorization") String token) {
        
        try {
            Long userId = extractUserIdFromToken(token);
            if (userId == null) {
                return Result.error("无效的访问令牌");
            }
            
            Map<String, Object> statistics = statisticsService.getStatistics(
                period, startDate, endDate, userId
            );
            
            return Result.success("获取成功", statistics);
        } catch (Exception e) {
            log.error("获取统计数据失败", e);
            return Result.error("获取统计数据失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取用户仪表盘统计数据（新接口，返回DTO格式）
     */
    @GetMapping("/dashboard/v2")
    public Result<DashboardDTO> getDashboardStatisticsV2(
            @RequestHeader("Authorization") String token) {
        
        try {
            Long userId = extractUserIdFromToken(token);
            if (userId == null) {
                return Result.error("无效的访问令牌");
            }
            
            DashboardDTO dashboard = statisticsService.getUserDashboard(userId);
            
            return Result.success("获取成功", dashboard);
        } catch (Exception e) {
            log.error("获取仪表盘统计失败", e);
            return Result.error("获取仪表盘统计失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取用户仪表盘统计数据（兼容旧接口）
     */
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboardStatistics(
            @RequestHeader("Authorization") String token) {
        
        try {
            Long userId = extractUserIdFromToken(token);
            if (userId == null) {
                return Result.error("无效的访问令牌");
            }
            
            Map<String, Object> dashboard = statisticsService.getUserDashboardMap(userId);
            
            return Result.success("获取成功", dashboard);
        } catch (Exception e) {
            log.error("获取仪表盘统计失败", e);
            return Result.error("获取仪表盘统计失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取用户最近活动（分页）
     */
    @GetMapping("/dashboard/activities")
    public Result<List<DashboardDTO.ActivityDTO>> getDashboardActivities(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestHeader("Authorization") String token) {
        
        try {
            Long userId = extractUserIdFromToken(token);
            if (userId == null) {
                return Result.error("无效的访问令牌");
            }
            
            List<DashboardDTO.ActivityDTO> activities = activityService.getUserRecentActivities(userId, page, size);
            
            return Result.success("获取成功", activities);
        } catch (Exception e) {
            log.error("获取用户最近活动失败", e);
            return Result.error("获取用户最近活动失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取用户推荐匹配（分页）
     */
    @GetMapping("/dashboard/matches")
    public Result<List<DashboardDTO.MatchItemDTO>> getDashboardMatches(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestHeader("Authorization") String token) {
        
        try {
            Long userId = extractUserIdFromToken(token);
            if (userId == null) {
                return Result.error("无效的访问令牌");
            }
            
            List<DashboardDTO.MatchItemDTO> matches = matchService.getRecommendedMatches(userId, page, size);
            
            return Result.success("获取成功", matches);
        } catch (Exception e) {
            log.error("获取推荐匹配失败", e);
            return Result.error("获取推荐匹配失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取智能推荐匹配
     */
    @GetMapping("/dashboard/matches/ai")
    public Result<List<DashboardDTO.MatchItemDTO>> getAiMatches(
            @RequestHeader("Authorization") String token) {
        
        try {
            Long userId = extractUserIdFromToken(token);
            if (userId == null) {
                return Result.error("无效的访问令牌");
            }
            
            List<DashboardDTO.MatchItemDTO> matches = matchService.getAiRecommendedMatches(userId);
            
            return Result.success("获取成功", matches);
        } catch (Exception e) {
            log.error("获取智能推荐匹配失败", e);
            return Result.error("获取智能推荐匹配失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取管理员核心指标
     */
    @GetMapping("/admin/core")
    public Result<Map<String, Object>> getAdminCoreStatistics(
            @RequestHeader("Authorization") String token) {
        
        try {
            Map<String, Object> coreStats = statisticsService.getAdminCoreStatistics();
            
            return Result.success("获取成功", coreStats);
        } catch (Exception e) {
            log.error("获取管理员核心指标失败", e);
            return Result.error("获取管理员核心指标失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取物品类别统计
     */
    @GetMapping("/admin/category")
    public Result<Map<String, Object>> getCategoryStatistics(
            @RequestHeader("Authorization") String token) {
        
        try {
            Map<String, Object> categoryStats = statisticsService.getCategoryStatistics();
            
            return Result.success("获取成功", categoryStats);
        } catch (Exception e) {
            log.error("获取物品类别统计失败", e);
            return Result.error("获取物品类别统计失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取趋势数据
     */
    @GetMapping("/trends")
    public Result<Map<String, Object>> getTrendStatistics(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestHeader("Authorization") String token) {
        
        try {
            Map<String, Object> trends = statisticsService.getTrendStatistics(period, startDate, endDate);
            
            return Result.success("获取成功", trends);
        } catch (Exception e) {
            log.error("获取趋势数据失败", e);
            return Result.error("获取趋势数据失败：" + e.getMessage());
        }
    }
    
    @GetMapping("/user")
    public Result<Map<String, Object>> getUserStatistics(
            @RequestHeader("Authorization") String token) {
        try {
            Long userId = extractUserIdFromToken(token);
            if (userId == null) {
                return Result.error("无效的访问令牌");
            }
            
            Map<String, Object> userStats = statisticsService.getUserDashboardMap(userId);
            
            return Result.success("获取成功", userStats);
        } catch (Exception e) {
            log.error("获取用户统计失败", e);
            return Result.error("获取用户统计失败：" + e.getMessage());
        }
    }
    
    @GetMapping("/test")
    public Result<Map<String, String>> test() {
        Map<String, String> data = Map.of("message", "StatisticsController正常工作", "status", "ok");
        return Result.success("测试成功", data);
    }
    
    /**
     * 调试用户统计数据
     */
    @GetMapping("/debug/stats")
    public Result<DashboardDTO> debugCurrentUserStats(
            @RequestHeader("Authorization") String token) {
        
        try {
            Long userId = extractUserIdFromToken(token);
            if (userId == null) {
                return Result.error("无效的访问令牌");
            }
            
            log.info("=== 开始调试用户 {} 的统计数据 ===", userId);
            
            // 直接获取仪表板数据
            DashboardDTO dashboard = statisticsService.getUserDashboard(userId);
            
            log.info("仪表板数据统计: totalLost={}, totalFound={}, recovered={}, pending={}", 
                dashboard.getUserStats() != null ? dashboard.getUserStats().getTotalLost() : "null",
                dashboard.getUserStats() != null ? dashboard.getUserStats().getTotalFound() : "null",
                dashboard.getUserStats() != null ? dashboard.getUserStats().getRecovered() : "null",
                dashboard.getUserStats() != null ? dashboard.getUserStats().getPending() : "null"
            );
            
            return Result.success("调试数据", dashboard);
        } catch (Exception e) {
            log.error("调试用户统计数据失败", e);
            return Result.error("调试失败：" + e.getMessage());
        }
    }
    
    /**
     * 从Authorization头中提取用户ID
     */
    private Long extractUserIdFromToken(String authorization) {
        if (!StringUtils.hasText(authorization) || !authorization.startsWith("Bearer ")) {
            log.warn("无效的Authorization头: {}", authorization);
            return null;
        }
        
        String token = authorization.substring(7);
        try {
            return jwtUtil.getUserIdFromToken(token);
        } catch (Exception e) {
            log.error("从token中提取用户ID失败", e);
            return null;
        }
    }
}