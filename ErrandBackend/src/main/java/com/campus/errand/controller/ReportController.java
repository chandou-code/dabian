package com.campus.errand.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.errand.entity.UserReport;
import com.campus.errand.service.UserReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户举报控制器
 */
@Slf4j
@RestController
@RequestMapping("/reports")
public class ReportController {
    
    @Autowired
    private UserReportService userReportService;
    
    /**
     * 创建举报
     */
    @PostMapping
    public Map<String, Object> createReport(@RequestBody UserReport report, @RequestAttribute(value = "currentUserId", required = false) Long currentUserId) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (currentUserId == null) {
                response.put("code", 401);
                response.put("msg", "用户未认证");
                response.put("data", null);
                return response;
            }
            
            // 设置举报者ID
            report.setReporterId(currentUserId);
            
            // 创建举报
            UserReport createdReport = userReportService.createReport(report);
            if (createdReport != null) {
                response.put("code", 200);
                response.put("msg", "举报成功");
                response.put("data", createdReport);
            } else {
                response.put("code", 500);
                response.put("msg", "举报失败");
                response.put("data", null);
            }
        } catch (Exception e) {
            log.error("创建举报异常", e);
            response.put("code", 500);
            response.put("msg", "创建举报失败：" + e.getMessage());
            response.put("data", null);
        }
        return response;
    }
    
    /**
     * 获取举报统计数据
     */
    @GetMapping("/stats")
    public Map<String, Object> getReportStats() {
        Map<String, Object> response = new HashMap<>();
        try {
            // 调用服务层获取真实统计数据
            Map<String, Object> stats = userReportService.getReportStats();
            
            response.put("code", 200);
            response.put("msg", "获取成功");
            response.put("data", stats);
        } catch (Exception e) {
            log.error("获取举报统计异常", e);
            response.put("code", 500);
            response.put("msg", "获取举报统计失败：" + e.getMessage());
            response.put("data", null);
        }
        return response;
    }
    
    /**
     * 获取举报列表（管理员使用）
     */
    @GetMapping
    public Map<String, Object> getReportList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        Map<String, Object> response = new HashMap<>();
        try {
            Page<UserReport> reportPage = userReportService.getReportList(page, pageSize, status, keyword);
            
            response.put("code", 200);
            response.put("msg", "获取成功");
            response.put("data", reportPage.getRecords());
            response.put("total", reportPage.getTotal());
            response.put("hasMore", reportPage.hasNext());
        } catch (Exception e) {
            log.error("获取举报列表异常", e);
            response.put("code", 500);
            response.put("msg", "获取举报列表失败：" + e.getMessage());
            response.put("data", null);
        }
        return response;
    }
    
    /**
     * 获取用户的举报列表
     */
    @GetMapping("/user")
    public Map<String, Object> getUserReportList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestAttribute(value = "currentUserId", required = false) Long currentUserId) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (currentUserId == null) {
                response.put("code", 401);
                response.put("msg", "用户未认证");
                response.put("data", null);
                return response;
            }
            
            Page<UserReport> reportPage = userReportService.getUserReportList(currentUserId, page, pageSize);
            
            response.put("code", 200);
            response.put("msg", "获取成功");
            response.put("data", reportPage.getRecords());
            response.put("total", reportPage.getTotal());
            response.put("hasMore", reportPage.hasNext());
        } catch (Exception e) {
            log.error("获取用户举报列表异常", e);
            response.put("code", 500);
            response.put("msg", "获取用户举报列表失败：" + e.getMessage());
            response.put("data", null);
        }
        return response;
    }
    
    /**
     * 获取举报详情
     */
    @GetMapping("/{id}")
    public Map<String, Object> getReportDetail(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            UserReport report = userReportService.getReportById(id);
            if (report != null) {
                response.put("code", 200);
                response.put("msg", "获取成功");
                response.put("data", report);
            } else {
                response.put("code", 404);
                response.put("msg", "举报不存在");
                response.put("data", null);
            }
        } catch (Exception e) {
            log.error("获取举报详情异常", e);
            response.put("code", 500);
            response.put("msg", "获取举报详情失败：" + e.getMessage());
            response.put("data", null);
        }
        return response;
    }
    
    /**
     * 更新举报状态
     */
    @PutMapping("/{id}/status")
    public Map<String, Object> updateReportStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Object> requestBody) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 获取请求参数
            String status = (String) requestBody.get("status");
            String adminRemark = (String) requestBody.getOrDefault("adminRemark", "");
            
            // 参数验证
            if (status == null || status.isEmpty()) {
                response.put("code", 400);
                response.put("msg", "状态不能为空");
                response.put("data", null);
                return response;
            }
            
            // 调用服务更新状态
            boolean success = userReportService.updateReportStatus(id, status, adminRemark);
            if (success) {
                response.put("code", 200);
                response.put("msg", "状态更新成功");
                response.put("data", null);
            } else {
                response.put("code", 400);
                response.put("msg", "状态更新失败");
                response.put("data", null);
            }
        } catch (Exception e) {
            log.error("更新举报状态异常", e);
            response.put("code", 500);
            response.put("msg", "更新举报状态失败：" + e.getMessage());
            response.put("data", null);
        }
        return response;
    }
}