package com.campus.lostfound.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * 文件处理器配置
 */
@Configuration
public class FileHandlerConfig implements WebMvcConfigurer {
    
    @Value("${app.upload.path:/uploads/}")
    private String uploadPath;
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 处理相对路径，转换为绝对路径
        String resourceLocation = uploadPath;
        File directory = new File(uploadPath);
        if (!directory.isAbsolute()) {
            // 如果是相对路径，基于应用工作目录创建
            String workingDir = System.getProperty("user.dir");
            resourceLocation = workingDir + File.separator + uploadPath;
        }
        
        // 确保路径格式正确
        if (!resourceLocation.endsWith(File.separator)) {
            resourceLocation = resourceLocation + File.separator;
        }
        
        // 配置文件访问路径
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + resourceLocation);
    }
}