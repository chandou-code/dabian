package com.campus.lostfound.service;

import com.campus.lostfound.entity.FileUpload;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文件上传服务接口
 */
public interface FileUploadService {
    
    /**
     * 上传单个文件
     */
    String uploadFile(MultipartFile file, String folder);
    
    /**
     * 上传多个文件
     */
    List<String> uploadFiles(List<MultipartFile> files, String folder);
    
    /**
     * 删除文件
     */
    boolean deleteFile(String fileUrl);
    
    /**
     * 检查文件类型是否允许
     */
    boolean isAllowedFileType(String filename);
    
    /**
     * 获取文件扩展名
     */
    String getFileExtension(String filename);
    
    /**
     * 生成文件名
     */
    String generateFileName(String originalFilename);
    
    /**
     * 根据文件URL查找上传记录
     */
    List<FileUpload> getUploadsByFileUrl(String fileUrl);
    
    /**
     * 更新上传记录
     */
    boolean updateUpload(FileUpload upload);
}