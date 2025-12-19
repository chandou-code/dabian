package com.campus.lostfound.controller;

import com.campus.lostfound.common.Result;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试控制器
 */
@RestController
@RequestMapping("/test")
@CrossOrigin
public class TestController {
    
    @GetMapping("/health")
    public Result<Map<String, Object>> health() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "running");
        data.put("message", "校园失物招领系统运行正常");
        data.put("timestamp", System.currentTimeMillis());
        return Result.success("系统正常", data);
    }
    
    @GetMapping("/info")
    public Result<Map<String, Object>> info() {
        Map<String, Object> data = new HashMap<>();
        data.put("system", "校园失物招领系统");
        data.put("version", "1.0.0");
        data.put("author", "Campus Team");
        data.put("description", "基于SpringBoot + Vue的校园失物招领平台");
        return Result.success(data);
    }
}