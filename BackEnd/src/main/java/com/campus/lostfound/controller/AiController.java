package com.campus.lostfound.controller;

import com.campus.lostfound.common.Result;
import com.campus.lostfound.service.AiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * AI功能控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/ai")
@CrossOrigin
public class AiController {
    
    @Autowired
    private AiService aiService;
    
    /**
     * AI图像识别
     */
    @PostMapping("/recognize")
    public Result<Map<String, Object>> recognizeImage(
            @RequestParam("image") MultipartFile image,
            @RequestHeader("Authorization") String token) {
        
        try {
            if (image.isEmpty()) {
                return Result.error("请选择要识别的图片");
            }
            
            // 验证文件类型
            String contentType = image.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return Result.error("请上传图片文件");
            }
            
            // 验证文件大小（限制10MB）
            if (image.getSize() > 10 * 1024 * 1024) {
                return Result.error("图片文件大小不能超过10MB");
            }
            
            // 调用AI识别
            Map<String, Object> result = aiService.recognizeImage(
                image.getBytes(), 
                image.getOriginalFilename()
            );
            
            return Result.success("识别成功", result);
            
        } catch (IOException e) {
            log.error("读取图片文件失败", e);
            return Result.error("读取图片文件失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("AI图像识别失败", e);
            return Result.error("AI识别失败：" + e.getMessage());
        }
    }
    
    /**
     * 以图搜图
     */
    @PostMapping("/search-image")
    public Result<Map<String, Object>> searchByImage(
            @RequestParam("image") MultipartFile image,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestHeader("Authorization") String token) {
        
        try {
            if (image.isEmpty()) {
                return Result.error("请选择要搜索的图片");
            }
            
            // 验证文件类型
            String contentType = image.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return Result.error("请上传图片文件");
            }
            
            // 验证文件大小（限制10MB）
            if (image.getSize() > 10 * 1024 * 1024) {
                return Result.error("图片文件大小不能超过10MB");
            }
            
            // 先进行AI识别
            Map<String, Object> recognitionResult = aiService.recognizeImage(
                image.getBytes(), 
                image.getOriginalFilename()
            );
            
            String category = (String) recognitionResult.get("category");
            String description = (String) recognitionResult.get("description");
            
            // 基于识别结果进行搜索
            Map<String, Object> searchResult = Map.of(
                "total", 0,
                "list", new java.util.ArrayList<>(),
                "recognitionResult", recognitionResult,
                "searchParams", Map.of(
                    "category", category,
                    "keyword", extractKeywords(description)
                )
            );
            
            return Result.success("搜索成功", searchResult);
            
        } catch (IOException e) {
            log.error("读取图片文件失败", e);
            return Result.error("读取图片文件失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("以图搜图失败", e);
            return Result.error("搜索失败：" + e.getMessage());
        }
    }
    
    /**
     * 智能匹配
     */
    @GetMapping("/match/{itemId}")
    public Result<Map<String, Object>> intelligentMatching(
            @PathVariable Long itemId,
            @RequestHeader("Authorization") String token) {
        
        try {
            Map<String, Object> matchResult = aiService.intelligentMatching(itemId);
            
            return Result.success("智能匹配完成", matchResult);
        } catch (Exception e) {
            log.error("智能匹配失败", e);
            return Result.error("智能匹配失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取AI配置（管理员专用）
     */
    @GetMapping("/config")
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
     * 更新AI配置（管理员专用）
     */
    @PutMapping("/config")
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
    @PostMapping("/test")
    public Result<Map<String, Object>> testAiInterface(
            @RequestHeader("Authorization") String token) {
        
        try {
            Map<String, Object> testResult = aiService.testAiInterface();
            
            if ("success".equals(testResult.get("status"))) {
                return Result.success("AI接口测试成功", testResult);
            } else {
                return Result.error("AI接口测试失败：" + testResult.get("message"));
            }
        } catch (Exception e) {
            log.error("测试AI接口失败", e);
            return Result.error("测试AI接口失败：" + e.getMessage());
        }
    }
    
    @GetMapping("/test")
    public Result<Map<String, String>> test() {
        Map<String, String> data = Map.of("message", "AiController正常工作", "status", "ok");
        return Result.success("测试成功", data);
    }
    
    /**
     * 从描述中提取关键词
     */
    private String extractKeywords(String description) {
        if (description == null || description.trim().isEmpty()) {
            return "";
        }
        
        // 简化处理，实际应该使用更复杂的NLP算法
        String[] words = description.split("\\s+");
        StringBuilder keywords = new StringBuilder();
        
        for (String word : words) {
            if (word.length() > 2) { // 只保留长度大于2的词
                if (keywords.length() > 0) {
                    keywords.append(" ");
                }
                keywords.append(word);
            }
        }
        
        return keywords.toString();
    }
}