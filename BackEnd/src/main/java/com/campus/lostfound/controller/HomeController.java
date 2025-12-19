package com.campus.lostfound.controller;

import com.campus.lostfound.common.Result;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 首页控制器
 */
@RestController
@RequestMapping("/")
@CrossOrigin
public class HomeController {
    
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
}