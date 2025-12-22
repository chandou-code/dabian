package com.campus.lostfound.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.campus.lostfound.entity.FileUpload;
import com.campus.lostfound.mapper.FileUploadMapper;
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
    
    @Autowired
    private FileUploadMapper fileUploadMapper;
    
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
            
            // 处理相对路径，转换为绝对路径
            File directory = new File(targetFolder);
            if (!directory.isAbsolute()) {
                // 如果是相对路径，基于应用工作目录创建
                String workingDir = System.getProperty("user.dir");
                directory = new File(workingDir, targetFolder);
            }
            
            if (!directory.exists()) {
                // 使用mkdirs()创建多级目录，确保所有父目录都被创建
                boolean created = directory.mkdirs();
                if (!created) {
                    log.warn("目录创建失败：{}", directory.getAbsolutePath());
                }
            }
            
            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            String fileName = generateFileName(originalFilename);
            
            // 使用绝对路径保存文件
            File targetFile = new File(directory, fileName);
            String absoluteFilePath = targetFile.getAbsolutePath();
            
            // 保存文件
            file.transferTo(targetFile);
            
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
            
            // 处理相对路径
            if (!file.isAbsolute()) {
                String workingDir = System.getProperty("user.dir");
                file = new File(workingDir, filePath);
            }
            
            if (file.exists() && file.delete()) {
                log.info("文件删除成功：{}，绝对路径：{}", fileUrl, file.getAbsolutePath());
                return true;
            } else {
                log.warn("文件删除失败或文件不存在：{}，绝对路径：{}", fileUrl, file.getAbsolutePath());
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
    
    @Override
    public List<FileUpload> getUploadsByFileUrl(String fileUrl) {
        try {
            return fileUploadMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<FileUpload>()
                    .eq("file_url", fileUrl)
            );
        } catch (Exception e) {
            log.error("根据文件URL查找上传记录失败: fileUrl={}", fileUrl, e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public boolean updateUpload(FileUpload upload) {
        try {
            int result = fileUploadMapper.updateById(upload);
            return result > 0;
        } catch (Exception e) {
            log.error("更新上传记录失败: uploadId={}", upload.getId(), e);
            return false;
        }
    }
}