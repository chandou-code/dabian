package com.campus.lostfound.config;

import com.campus.lostfound.service.SystemConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 应用启动时数据初始化
 */
@Slf4j
@Component
public class DataInitializer implements ApplicationRunner {
    
    @Autowired
    private SystemConfigService systemConfigService;
    
    @Override
    public void run(ApplicationArguments args) {
        log.info("开始初始化系统数据...");
        
        try {
            // 初始化系统配置
            systemConfigService.initSystemConfigs();
            log.info("系统配置初始化完成");
            
            // 创建上传目录
            createUploadDirectories();
            
            log.info("系统数据初始化完成");
        } catch (Exception e) {
            log.error("系统数据初始化失败", e);
        }
    }
    
    /**
     * 创建上传目录
     */
    private void createUploadDirectories() {
        try {
            java.io.File uploadDir = new java.io.File("uploads");
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
                log.info("创建上传目录：uploads");
            }
            
            java.io.File imageDir = new java.io.File("uploads/images");
            if (!imageDir.exists()) {
                imageDir.mkdirs();
                log.info("创建图片目录：uploads/images");
            }
            
            java.io.File tempDir = new java.io.File("uploads/temp");
            if (!tempDir.exists()) {
                tempDir.mkdirs();
                log.info("创建临时目录：uploads/temp");
            }
        } catch (Exception e) {
            log.error("创建上传目录失败", e);
        }
    }
}