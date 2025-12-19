package com.campus.lostfound.controller;

import com.campus.lostfound.common.Result;
import com.campus.lostfound.service.SystemConfigService;
import com.campus.lostfound.service.AiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 系统设置控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/system")
@CrossOrigin
public class SystemController {
    
    @Autowired
    private SystemConfigService systemConfigService;
    
    @Autowired
    private AiService aiService;
    
    // ========== 系统配置管理 ==========
    
    /**
     * 获取系统配置
     */
    @GetMapping("/config")
    public Result<Map<String, Object>> getSystemConfig(
            @RequestHeader("Authorization") String token) {
        
        try {
            Map<String, Object> config = systemConfigService.getSystemConfig();
            
            return Result.success("获取成功", config);
        } catch (Exception e) {
            log.error("获取系统配置失败", e);
            return Result.error("获取系统配置失败：" + e.getMessage());
        }
    }
    
    /**
     * 更新系统配置
     */
    @PutMapping("/config")
    public Result<Map<String, Object>> updateSystemConfig(
            @RequestBody Map<String, Object> configData,
            @RequestHeader("Authorization") String token) {
        
        try {
            Map<String, Object> updatedConfig = systemConfigService.updateSystemConfig(configData);
            
            return Result.success("更新成功", updatedConfig);
        } catch (Exception e) {
            log.error("更新系统配置失败", e);
            return Result.error("更新系统配置失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取邮件模板
     */
    @GetMapping("/email-templates")
    public Result<Map<String, String>> getEmailTemplates(
            @RequestHeader("Authorization") String token) {
        
        try {
            Map<String, String> templates = systemConfigService.getEmailTemplates();
            
            return Result.success("获取成功", templates);
        } catch (Exception e) {
            log.error("获取邮件模板失败", e);
            return Result.error("获取邮件模板失败：" + e.getMessage());
        }
    }
    
    /**
     * 更新邮件模板
     */
    @PutMapping("/email-templates")
    public Result<Map<String, String>> updateEmailTemplates(
            @RequestBody Map<String, String> templates,
            @RequestHeader("Authorization") String token) {
        
        try {
            Map<String, String> updatedTemplates = systemConfigService.updateEmailTemplates(templates);
            
            return Result.success("更新成功", updatedTemplates);
        } catch (Exception e) {
            log.error("更新邮件模板失败", e);
            return Result.error("更新邮件模板失败：" + e.getMessage());
        }
    }
    
    // ========== AI配置管理 ==========
    
    /**
     * 获取AI配置
     */
    @GetMapping("/ai-config")
    public Result<Map<String, Object>> getAiConfig(
            @RequestHeader("Authorization") String token) {
        
        try {
            Map<String, Object> aiConfig = aiService.getAiConfig();
            
            return Result.success("获取成功", aiConfig);
        } catch (Exception e) {
            log.error("获取AI配置失败", e);
            return Result.error("获取AI配置失败：" + e.getMessage());
        }
    }
    
    /**
     * 更新AI配置
     */
    @PutMapping("/ai-config")
    public Result<Map<String, Object>> updateAiConfig(
            @RequestBody Map<String, Object> aiConfigData,
            @RequestHeader("Authorization") String token) {
        
        try {
            Map<String, Object> updatedConfig = aiService.updateAiConfig(aiConfigData);
            
            return Result.success("更新成功", updatedConfig);
        } catch (Exception e) {
            log.error("更新AI配置失败", e);
            return Result.error("更新AI配置失败：" + e.getMessage());
        }
    }
    
    /**
     * 测试AI接口
     */
    @PostMapping("/test-ai")
    public Result<Map<String, Object>> testAiInterface(
            @RequestHeader("Authorization") String token) {
        
        try {
            Map<String, Object> testResult = aiService.testAiInterface();
            
            return Result.success("AI接口测试成功", testResult);
        } catch (Exception e) {
            log.error("测试AI接口失败", e);
            return Result.error("测试AI接口失败：" + e.getMessage());
        }
    }
    
    // ========== 公告管理 ==========
    
    /**
     * 获取公告列表
     */
    @GetMapping("/announcements")
    public Result<java.util.List<Map<String, Object>>> getAnnouncements(
            @RequestHeader("Authorization") String token) {
        
        try {
            java.util.List<Map<String, Object>> announcements = systemConfigService.getAnnouncements();
            
            return Result.success("获取成功", announcements);
        } catch (Exception e) {
            log.error("获取公告列表失败", e);
            return Result.error("获取公告列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 创建公告
     */
    @PostMapping("/announcements")
    public Result<Map<String, Object>> createAnnouncement(
            @RequestBody Map<String, Object> announcementData,
            @RequestHeader("Authorization") String token) {
        
        try {
            String title = (String) announcementData.get("title");
            String content = (String) announcementData.get("content");
            String status = (String) announcementData.getOrDefault("status", "active");
            
            Map<String, Object> announcement = systemConfigService.createAnnouncement(title, content, status);
            
            return Result.success("创建成功", announcement);
        } catch (Exception e) {
            log.error("创建公告失败", e);
            return Result.error("创建公告失败：" + e.getMessage());
        }
    }
    
    /**
     * 更新公告
     */
    @PutMapping("/announcements/{id}")
    public Result<Map<String, Object>> updateAnnouncement(
            @PathVariable Long id,
            @RequestBody Map<String, Object> announcementData,
            @RequestHeader("Authorization") String token) {
        
        try {
            String title = (String) announcementData.get("title");
            String content = (String) announcementData.get("content");
            String status = (String) announcementData.get("status");
            
            Map<String, Object> announcement = systemConfigService.updateAnnouncement(id, title, content, status);
            
            return Result.success("更新成功", announcement);
        } catch (Exception e) {
            log.error("更新公告失败", e);
            return Result.error("更新公告失败：" + e.getMessage());
        }
    }
    
    /**
     * 删除公告
     */
    @DeleteMapping("/announcements/{id}")
    public Result<String> deleteAnnouncement(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        
        try {
            systemConfigService.deleteAnnouncement(id);
            
            return Result.success("删除成功", null);
        } catch (Exception e) {
            log.error("删除公告失败", e);
            return Result.error("删除公告失败：" + e.getMessage());
        }
    }
    
    // ========== 导出功能 ==========
    
    /**
     * 导出统计数据
     */
    @GetMapping("/statistics/export")
    public Result<String> exportStatistics(
            @RequestParam String type,
            @RequestParam(required = false, defaultValue = "xlsx") String format,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestHeader("Authorization") String token) {
        
        try {
            String filePath = systemConfigService.exportStatistics(type, format, startDate, endDate);
            
            return Result.success("导出成功", filePath);
        } catch (Exception e) {
            log.error("导出统计数据失败", e);
            return Result.error("导出统计数据失败：" + e.getMessage());
        }
    }
    
    @GetMapping("/test")
    public Result<Map<String, String>> test() {
        Map<String, String> data = Map.of("message", "SystemController正常工作", "status", "ok");
        return Result.success("测试成功", data);
    }
}