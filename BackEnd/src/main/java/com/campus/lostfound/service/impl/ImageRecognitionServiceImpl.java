package com.campus.lostfound.service.impl;

import com.campus.lostfound.service.ImageRecognitionService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 图像识别服务实现
 */
@Service
public class ImageRecognitionServiceImpl implements ImageRecognitionService {

    private static final Logger log = LoggerFactory.getLogger(ImageRecognitionServiceImpl.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // 从配置文件读取API密钥和URL
    @Value("${ai.api.key:sk-xnscttiarytendwlcahuumvpqtvqpibbkipejzcfxjsmjuwa}")
    private String apiKey;

    @Value("${ai.api.url:https://api.siliconflow.cn/v1/chat/completions}")
    private String apiUrl;

    @Override
    public Map<String, String> generateItemDescription(MultipartFile file) {
        try {
            // 1. 将图片转换为base64
            String base64Image = convertImageToBase64(file);
            
            // 2. 创建请求payload
            String payload = createRequestPayload(base64Image);
            
            // 3. 发送HTTP请求到AI API
            String response = sendRequest(payload);
            
            // 4. 解析响应并提取物品信息
            return extractItemInfo(response);
        } catch (Exception e) {
            log.error("生成物品描述失败", e);
            throw new RuntimeException("生成物品描述失败: " + e.getMessage());
        }
    }

    /**
     * 将图片转换为base64格式
     */
    private String convertImageToBase64(MultipartFile file) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(file.getBytes());
        byte[] bytes = outputStream.toByteArray();
        String base64 = Base64.getEncoder().encodeToString(bytes);
        return "data:image/jpeg;base64," + base64;
    }

    /**
     * 创建请求payload
     */
    private String createRequestPayload(String base64Image) throws IOException {
        Map<String, Object> payload = new HashMap<>();
        payload.put("model", "Qwen/Qwen3-Omni-30B-A3B-Instruct");
        
        // 创建messages数组
        Map<String, Object> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", "你是一个专业的物品识别助手，用于失物招领场景。请严格按照JSON格式输出结果，包含三个字段：item（物品的具体名称）、detail（物品的特征细节）和category（物品分类）。分类必须从以下列表中选择：电子产品、钱包证件、书籍文具、生活用品、衣物饰品、体育用品、其他物品。");
        
        Map<String, Object> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        
        // 创建user message的content数组，包含图片和文本
        Map<String, Object> imageContent = new HashMap<>();
        imageContent.put("type", "image_url");
        Map<String, Object> imageUrl = new HashMap<>();
        imageUrl.put("url", base64Image);
        imageUrl.put("detail", "high");
        imageContent.put("image_url", imageUrl);
        
        Map<String, Object> textContent = new HashMap<>();
        textContent.put("type", "text");
        textContent.put("text", "请识别图片中的物品，返回物品的具体名称、详细特征和分类，严格按照JSON格式输出，例如：{\"item\": \"笔记本电脑\", \"detail\": \"银色外壳，13英寸屏幕，背面有苹果标志\", \"category\": \"电子产品\"}。分类必须从以下列表中选择：电子产品、钱包证件、书籍文具、生活用品、衣物饰品、体育用品、其他物品。");
        
        userMessage.put("content", new Object[]{imageContent, textContent});
        
        payload.put("messages", new Object[]{systemMessage, userMessage});
        
        return objectMapper.writeValueAsString(payload);
    }

    /**
     * 发送HTTP请求到AI API
     */
    private String sendRequest(String payload) throws IOException {
        java.net.HttpURLConnection conn = null;
        java.io.BufferedReader reader = null;
        
        try {
            java.net.URL url = new java.net.URL(apiUrl);
            conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            
            // 写入请求体
            java.io.OutputStream os = conn.getOutputStream();
            os.write(payload.getBytes(StandardCharsets.UTF_8));
            os.flush();
            os.close();
            
            // 读取响应
            reader = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            
            return response.toString();
        } finally {
            if (reader != null) {
                reader.close();
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    /**
     * 从AI响应中提取物品信息
     */
    private Map<String, String> extractItemInfo(String response) throws IOException {
        // 解析响应JSON
        JsonNode rootNode = objectMapper.readTree(response);
        String aiContent = rootNode.path("choices").get(0).path("message").path("content").asText();
        
        log.info("AI原始响应: {}", aiContent);
        
        // 处理代码块标记
        String processedContent = aiContent;
        if (processedContent.startsWith("```json")) {
            processedContent = processedContent.replace("```json", "").replace("```", "").trim();
        } else if (processedContent.startsWith("```")) {
            processedContent = processedContent.replace("```", "").trim();
        }
        
        try {
            // 尝试直接解析JSON
            JsonNode itemNode = objectMapper.readTree(processedContent);
            Map<String, String> itemInfo = new HashMap<>();
            itemInfo.put("item", itemNode.path("item").asText(null));
            itemInfo.put("detail", itemNode.path("detail").asText(null));
            itemInfo.put("category", itemNode.path("category").asText(null));
            return itemInfo;
        } catch (Exception e) {
            // 如果直接解析失败，尝试用正则表达式提取
            log.warn("直接解析JSON失败，尝试正则提取", e);
            return extractWithRegex(processedContent);
        }
    }

    /**
     * 使用正则表达式提取物品信息
     */
    private Map<String, String> extractWithRegex(String content) {
        Map<String, String> itemInfo = new HashMap<>();
        
        // 提取item字段
        Pattern itemPattern = Pattern.compile("\"item\"\s*:\s*\"([^\"]+)\"");
        Matcher itemMatcher = itemPattern.matcher(content);
        if (itemMatcher.find()) {
            itemInfo.put("item", itemMatcher.group(1));
        }
        
        // 提取detail字段
        Pattern detailPattern = Pattern.compile("\"detail\"\s*:\s*\"([^\"]+)\"");
        Matcher detailMatcher = detailPattern.matcher(content);
        if (detailMatcher.find()) {
            itemInfo.put("detail", detailMatcher.group(1));
        }
        
        // 提取category字段
        Pattern categoryPattern = Pattern.compile("\"category\"\s*:\s*\"([^\"]+)\"");
        Matcher categoryMatcher = categoryPattern.matcher(content);
        if (categoryMatcher.find()) {
            itemInfo.put("category", categoryMatcher.group(1));
        }
        
        return itemInfo;
    }
}
