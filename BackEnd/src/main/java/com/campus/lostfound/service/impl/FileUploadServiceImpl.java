package com.campus.lostfound.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.campus.lostfound.service.FileUploadService;
import com.campus.lostfound.service.SystemConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 文件上传服务实现类（本地存储）
 */
@Slf4j
@Service
public class FileUploadServiceImpl implements FileUploadService {
    
    @Autowired
    private SystemConfigService systemConfigService;
    
    @Value("${app.upload.path:/uploads/}")
    private String uploadPath;
    
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
        "jpg", "jpeg", "png", "gif", "webp"
    );
    
    @Override
    public String uploadFile(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        
        try {
            // 检查文件类型
            if (!isAllowedFileType(file.getOriginalFilename())) {
                log.warn("不支持的文件类型：{}", file.getOriginalFilename());
                return null;
            }
            
            // 检查文件大小
            long maxSize = getMaxFileSize();
            if (file.getSize() > maxSize) {
                log.warn("文件大小超出限制：{}, 最大允许：{}", file.getSize(), maxSize);
                return null;
            }
            
            // 创建目录
            String dateFolder = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String targetFolder = uploadPath + (StrUtil.isNotBlank(folder) ? folder + "/" : "") + dateFolder;
            File directory = new File(targetFolder);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            
            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            String fileName = generateFileName(originalFilename);
            String filePath = targetFolder + "/" + fileName;
            
            // 保存文件
            file.transferTo(new File(filePath));
            
            // 返回相对路径
            String relativePath = (StrUtil.isNotBlank(folder) ? folder + "/" : "") + dateFolder + "/" + fileName;
            log.info("文件上传成功：{}", relativePath);
            
            return relativePath;
            
        } catch (IOException e) {
            log.error("文件上传失败：{}", file.getOriginalFilename(), e);
            return null;
        }
    }
    
    @Override
    public List<String> uploadFiles(List<MultipartFile> files, String folder) {
        List<String> fileUrls = new ArrayList<>();
        
        if (files == null || files.isEmpty()) {
            return fileUrls;
        }
        
        int maxCount = getMaxImageCount();
        int count = 0;
        
        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                if (count >= maxCount) {
                    log.warn("超出最大文件数量限制：{}", maxCount);
                    break;
                }
                
                String fileUrl = uploadFile(file, folder);
                if (fileUrl != null) {
                    fileUrls.add(fileUrl);
                    count++;
                }
            }
        }
        
        return fileUrls;
    }
    
    @Override
    public boolean deleteFile(String fileUrl) {
        if (StrUtil.isBlank(fileUrl)) {
            return false;
        }
        
        try {
            String filePath = uploadPath + fileUrl;
            File file = new File(filePath);
            
            if (file.exists() && file.delete()) {
                log.info("文件删除成功：{}", fileUrl);
                return true;
            } else {
                log.warn("文件删除失败或文件不存在：{}", fileUrl);
                return false;
            }
        } catch (Exception e) {
            log.error("文件删除异常：{}", fileUrl, e);
            return false;
        }
    }
    
    @Override
    public boolean isAllowedFileType(String filename) {
        if (StrUtil.isBlank(filename)) {
            return false;
        }
        
        String extension = getFileExtension(filename).toLowerCase();
        return ALLOWED_IMAGE_TYPES.contains(extension);
    }
    
    @Override
    public String getFileExtension(String filename) {
        if (StrUtil.isBlank(filename)) {
            return "";
        }
        
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "";
        }
        
        return filename.substring(lastDotIndex + 1);
    }
    
    @Override
    public String generateFileName(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String timestamp = String.valueOf(System.currentTimeMillis());
        
        return timestamp + "_" + uuid + (StrUtil.isNotBlank(extension) ? "." + extension : "");
    }
    
    /**
     * 获取最大文件大小
     */
    private long getMaxFileSize() {
        String maxSizeStr = systemConfigService.getConfigValue("upload_image_size", "10485760"); // 默认10MB
        try {
            return Long.parseLong(maxSizeStr);
        } catch (NumberFormatException e) {
            return 10485760; // 10MB
        }
    }
    
    /**
     * 获取最大图片数量
     */
    private int getMaxImageCount() {
        String maxCountStr = systemConfigService.getConfigValue("max_image_count", "6"); // 默认6张
        try {
            return Integer.parseInt(maxCountStr);
        } catch (NumberFormatException e) {
            return 6;
        }
    }
}