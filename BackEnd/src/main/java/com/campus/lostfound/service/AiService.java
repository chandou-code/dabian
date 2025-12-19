package com.campus.lostfound.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.lostfound.entity.FileUpload;

import java.util.Map;

/**
 * AI服务接口
 */
public interface AiService extends IService<FileUpload> {
    
    /**
     * AI图像识别
     */
    Map<String, Object> recognizeImage(byte[] imageData, String fileName);
    
    /**
     * 获取AI配置
     */
    Map<String, Object> getAiConfig();
    
    /**
     * 更新AI配置
     */
    Map<String, Object> updateAiConfig(Map<String, Object> aiConfigData);
    
    /**
     * 测试AI接口
     */
    Map<String, Object> testAiInterface();
    
    /**
     * 智能匹配
     */
    Map<String, Object> intelligentMatching(Long itemId);
}