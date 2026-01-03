package com.campus.lostfound.controller;

import com.campus.lostfound.common.Result;
import com.campus.lostfound.service.ItemService;
import com.campus.lostfound.service.AiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * 搜索控制器
 */
@Slf4j
@RestController
@RequestMapping("/search")
@CrossOrigin
public class SearchController {
    
    @Autowired
    private ItemService itemService;
    
    @Autowired
    private AiService aiService;
    
    /**
     * 文本搜索
     */
    @GetMapping("/text")
    public Result<Map<String, Object>> textSearch(
            @RequestParam String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false, defaultValue = "all") String timeRange,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestHeader("Authorization") String token) {
        
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                return Result.error("请输入搜索关键词");
            }
            
            var searchResult = itemService.searchItems(page, pageSize, keyword, null, category, timeRange);
            
            Map<String, Object> result = Map.of(
                "total", searchResult.getTotal(),
                "list", searchResult.getRecords().stream().map(item -> {
                    Map<String, Object> itemMap = new java.util.HashMap<>();
                    itemMap.put("id", item.getId());
                    itemMap.put("title", item.getItemName());
                    itemMap.put("description", item.getDescription());
                    itemMap.put("category", item.getCategory());
                    itemMap.put("location", item.getLocation());
                    itemMap.put("time", item.getCreatedAt() != null ? item.getCreatedAt().toLocalDate().toString() : "");
                    itemMap.put("type", item.getType());
                    itemMap.put("status", item.getStatus());
                    itemMap.put("image", getFirstImage(item.getImages()));
                    return itemMap;
                }).toList()
            );
            
            return Result.success("搜索成功", result);
            
        } catch (Exception e) {
            log.error("文本搜索失败", e);
            return Result.error("搜索失败：" + e.getMessage());
        }
    }
    
    /**
     * 以图搜图
     */
    @PostMapping("/image")
    public Result<Map<String, Object>> imageSearch(
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
            Double confidence = (Double) recognitionResult.get("confidence");
            
            // 基于识别结果进行搜索
            String searchKeyword = extractKeywords(description);
            var searchResult = itemService.searchItems(page, pageSize, searchKeyword, null, category, "all");
            
            // 计算匹配度分数
            java.util.List<Map<String, Object>> itemsWithScore = searchResult.getRecords().stream()
                .map(item -> {
                    Map<String, Object> itemMap = new java.util.HashMap<>();
                    itemMap.put("id", item.getId());
                    itemMap.put("title", item.getItemName());
                    itemMap.put("description", item.getDescription());
                    itemMap.put("category", item.getCategory());
                    itemMap.put("location", item.getLocation());
                    itemMap.put("time", item.getCreatedAt() != null ? item.getCreatedAt().toLocalDate().toString() : "");
                    itemMap.put("type", item.getType());
                    itemMap.put("status", item.getStatus());
                    itemMap.put("image", getFirstImage(item.getImages()));
                    
                    // 计算匹配分数
                    double matchScore = calculateMatchScore(description, item.getDescription(), item.getItemName());
                    itemMap.put("matchScore", (int) matchScore);
                    
                    return itemMap;
                })
                .sorted((a, b) -> Integer.compare((Integer) b.get("matchScore"), (Integer) a.get("matchScore")))
                .toList();
            
            Map<String, Object> result = Map.of(
                "total", searchResult.getTotal(),
                "list", itemsWithScore,
                "recognitionResult", Map.of(
                    "description", description,
                    "category", category,
                    "confidence", confidence
                )
            );
            
            return Result.success("搜索成功", result);
            
        } catch (IOException e) {
            log.error("读取图片文件失败", e);
            return Result.error("读取图片文件失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("以图搜图失败", e);
            return Result.error("搜索失败：" + e.getMessage());
        }
    }
    
    /**
     * 高级搜索
     */
    @GetMapping("/advanced")
    public Result<Map<String, Object>> advancedSearch(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String type,
            @RequestParam(required = false, defaultValue = "all") String timeRange,
            @RequestParam(required = false) String dateRange,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestHeader("Authorization") String token) {
        
        try {
            // 构建复合搜索条件
            StringBuilder searchKeyword = new StringBuilder();
            
            if (keyword != null && !keyword.trim().isEmpty()) {
                searchKeyword.append(keyword);
            }
            
            if (category != null && !category.trim().isEmpty()) {
                if (searchKeyword.length() > 0) searchKeyword.append(" ");
                searchKeyword.append(category);
            }
            
            if (location != null && !location.trim().isEmpty()) {
                if (searchKeyword.length() > 0) searchKeyword.append(" ");
                searchKeyword.append(location);
            }
            
            String finalKeyword = searchKeyword.length() > 0 ? searchKeyword.toString() : null;
            
            var searchResult = itemService.searchItems(page, pageSize, finalKeyword, type, category, timeRange);
            
            Map<String, Object> result = Map.of(
                "total", searchResult.getTotal(),
                "list", searchResult.getRecords().stream().map(item -> {
                    Map<String, Object> itemMap = new java.util.HashMap<>();
                    itemMap.put("id", item.getId());
                    itemMap.put("title", item.getItemName());
                    itemMap.put("description", item.getDescription());
                    itemMap.put("category", item.getCategory());
                    itemMap.put("location", item.getLocation());
                    itemMap.put("time", item.getCreatedAt() != null ? item.getCreatedAt().toLocalDate().toString() : "");
                    itemMap.put("type", item.getType());
                    itemMap.put("status", item.getStatus());
                    itemMap.put("image", getFirstImage(item.getImages()));
                    return itemMap;
                }).toList()
            );
            
            return Result.success("搜索成功", result);
            
        } catch (Exception e) {
            log.error("高级搜索失败", e);
            return Result.error("搜索失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取搜索建议
     */
    @GetMapping("/suggestions")
    public Result<java.util.List<String>> getSearchSuggestions(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "5") int limit,
            @RequestHeader("Authorization") String token) {
        
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                return Result.success(java.util.Collections.emptyList());
            }
            
            // 简化处理，实际应该基于搜索历史和热门词汇生成建议
            java.util.List<String> suggestions = java.util.Arrays.asList(
                keyword + "包",
                keyword + "手机", 
                keyword + "证件",
                keyword + "书籍",
                "黑色" + keyword
            ).subList(0, Math.min(5, limit));
            
            return Result.success("获取成功", suggestions);
            
        } catch (Exception e) {
            log.error("获取搜索建议失败", e);
            return Result.error("获取搜索建议失败：" + e.getMessage());
        }
    }
    
    @GetMapping("/test")
    public Result<Map<String, String>> test() {
        Map<String, String> data = Map.of("message", "SearchController正常工作", "status", "ok");
        return Result.success("测试成功", data);
    }
    
    /**
     * 获取第一张图片
     */
    private String getFirstImage(String images) {
        if (images == null || images.trim().isEmpty()) {
            return "";
        }
        
        try {
            // 简化处理，实际应该解析JSON数组
            if (images.startsWith("[") && images.endsWith("]")) {
                String cleanImages = images.substring(1, images.length() - 1);
                String[] imageArray = cleanImages.split(",");
                return imageArray.length > 0 ? imageArray[0].replaceAll("\"", "").trim() : "";
            }
        } catch (Exception e) {
            log.error("解析图片URL失败", e);
        }
        
        return "";
    }
    
    /**
     * 从描述中提取关键词
     */
    private String extractKeywords(String description) {
        if (description == null || description.trim().isEmpty()) {
            return "";
        }
        
        // 简化处理，移除常见的无意义词汇
        String cleaned = description.toLowerCase()
            .replaceAll("(\\s+|的|了|和|在|有|是|一个|这个|那个|什么|怎么|为什么)", " ")
            .replaceAll("\\s+", " ")
            .trim();
        
        String[] words = cleaned.split("\\s+");
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
    
    /**
     * 计算匹配分数
     */
    private double calculateMatchScore(String recognitionDesc, String itemDesc, String itemName) {
        if (recognitionDesc == null || itemDesc == null || itemName == null) {
            return 0;
        }
        
        double score = 0;
        String recognition = recognitionDesc.toLowerCase();
        String desc = itemDesc.toLowerCase();
        String name = itemName.toLowerCase();
        
        // 描述相似度（权重50%）
        double descSimilarity = calculateSimilarity(recognition, desc);
        score += descSimilarity * 0.5;
        
        // 名称相似度（权重30%）
        double nameSimilarity = calculateSimilarity(recognition, name);
        score += nameSimilarity * 0.3;
        
        // 关键词匹配（权重20%）
        double keywordMatch = calculateKeywordMatch(recognition, desc + " " + name);
        score += keywordMatch * 0.2;
        
        return score * 100;
    }
    
    /**
     * 计算字符串相似度
     */
    private double calculateSimilarity(String s1, String s2) {
        if (s1 == null || s2 == null) return 0;
        if (s1.equals(s2)) return 1.0;
        
        // 使用简化的编辑距离算法
        int maxLength = Math.max(s1.length(), s2.length());
        if (maxLength == 0) return 1.0;
        
        int editDistance = calculateEditDistance(s1, s2);
        return 1.0 - (double) editDistance / maxLength;
    }
    
    /**
     * 计算编辑距离
     */
    private int calculateEditDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        
        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }
        
        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j], dp[i][j - 1]) + 1,
                        dp[i - 1][j - 1] + 1
                    );
                }
            }
        }
        
        return dp[s1.length()][s2.length()];
    }
    
    /**
     * 计算关键词匹配度
     */
    private double calculateKeywordMatch(String text1, String text2) {
        String[] words1 = text1.split("\\s+");
        String[] words2 = text2.split("\\s+");
        
        int matchCount = 0;
        for (String word1 : words1) {
            for (String word2 : words2) {
                if (word1.length() > 2 && word1.equals(word2)) {
                    matchCount++;
                    break;
                }
            }
        }
        
        return words1.length > 0 ? (double) matchCount / words1.length : 0;
    }
}