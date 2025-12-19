package com.campus.lostfound.controller;

import com.campus.lostfound.common.Result;
import com.campus.lostfound.service.StatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
            Long userId = 1L; // TODO: 从JWT解析用户ID
            
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
     * 获取用户仪表盘统计数据
     */
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboardStatistics(
            @RequestHeader("Authorization") String token) {
        
        try {
            Long userId = 1L; // TODO: 从JWT解析用户ID
            
            Map<String, Object> dashboard = statisticsService.getUserDashboard(userId);
            
            return Result.success("获取成功", dashboard);
        } catch (Exception e) {
            log.error("获取仪表盘统计失败", e);
            return Result.error("获取仪表盘统计失败：" + e.getMessage());
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
            Long userId = 1L; // TODO: 从JWT解析用户ID
            
            Map<String, Object> userStats = statisticsService.getUserDashboard(userId);
            
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
}