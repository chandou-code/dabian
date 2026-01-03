package com.campus.lostfound.controller;

import com.campus.lostfound.common.Result;
import com.campus.lostfound.service.StatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 首页控制器
 */
@Slf4j
@RestController
@RequestMapping("/")
@CrossOrigin
public class HomeController {
    
    @Autowired
    private StatisticsService statisticsService;
    
    /**
     * 健康检查
     */
    @GetMapping
    public Result<Map<String, Object>> health() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "UP");
        data.put("timestamp", System.currentTimeMillis());
        data.put("application", "校园失物招领系统");
        data.put("version", "1.0.0");
        
        return Result.success(data);
    }
    
    /**
     * API信息
     */
    @GetMapping("/info")
    public Result<Map<String, Object>> info() {
        Map<String, Object> data = new HashMap<>();
        data.put("application", "校园失物招领系统 API");
        data.put("version", "1.0.0");
        data.put("description", "Campus Lost and Found System API");
        data.put("endpoints", new String[]{
            "POST /auth/login - 用户登录",
            "POST /auth/register - 用户注册",
            "GET /lost-items - 获取失物列表",
            "GET /found-items - 获取招领列表",
            "POST /lost-items - 发布失物",
            "POST /found-items - 发布招领"
        });
        
        return Result.success(data);
    }
    
    /**
     * 获取首页统计数据
     */
    @GetMapping("/home")
    public Result<Map<String, Object>> getHomeStatistics() {
        try {
            log.info("获取首页统计数据");
            Map<String, Object> homeData = statisticsService.getHomeStatistics();
            return Result.success("获取成功", homeData);
        } catch (Exception e) {
            log.error("获取首页统计数据失败", e);
            return Result.error("获取首页统计数据失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取首页最新物品列表
     */
    @GetMapping("/home/recent")
    public Result<Map<String, Object>> getRecentItems(
            @RequestParam(defaultValue = "10") Integer limit) {
        try {
            log.info("获取首页最新物品列表，limit: {}", limit);
            Map<String, Object> recentData = statisticsService.getRecentItems(limit);
            return Result.success("获取成功", recentData);
        } catch (Exception e) {
            log.error("获取首页最新物品列表失败", e);
            return Result.error("获取首页最新物品列表失败：" + e.getMessage());
        }
    }
}