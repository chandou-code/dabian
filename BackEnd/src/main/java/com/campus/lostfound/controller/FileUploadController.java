package com.campus.lostfound.controller;

import com.campus.lostfound.common.Result;
import com.campus.lostfound.service.FileUploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 文件上传控制器
 */
@Slf4j
@RestController
@RequestMapping("/upload")
@CrossOrigin
public class FileUploadController {
    
    @Autowired
    private FileUploadService fileUploadService;
    
    @PostMapping("/image")
    public Result<Map<String, Object>> uploadImage(@RequestParam("file") MultipartFile file,
                                                  @RequestParam(value = "folder", required = false) String folder) {
        try {
            if (file == null || file.isEmpty()) {
                return Result.error("请选择要上传的文件");
            }
            
            String fileUrl = fileUploadService.uploadFile(file, folder);
            if (fileUrl != null) {
                Map<String, Object> result = Map.of(
                    "url", fileUrl,
                    "filename", file.getOriginalFilename(),
                    "size", file.getSize()
                );
                return Result.success("上传成功", result);
            } else {
                return Result.error("上传失败，请检查文件类型和大小");
            }
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return Result.error("上传失败：" + e.getMessage());
        }
    }
    
    @PostMapping("/images")
    public Result<List<Map<String, Object>>> uploadImages(@RequestParam("files") List<MultipartFile> files,
                                                        @RequestParam(value = "folder", required = false) String folder) {
        try {
            if (files == null || files.isEmpty()) {
                return Result.error("请选择要上传的文件");
            }
            
            List<String> fileUrls = fileUploadService.uploadFiles(files, folder);
            List<Map<String, Object>> result = new java.util.ArrayList<>();
            
            for (int i = 0; i < fileUrls.size(); i++) {
                MultipartFile file = files.get(i);
                String fileUrl = fileUrls.get(i);
                
                result.add(Map.of(
                    "url", fileUrl,
                    "filename", file.getOriginalFilename(),
                    "size", file.getSize()
                ));
            }
            
            return Result.success("上传成功", result);
        } catch (Exception e) {
            log.error("批量文件上传失败", e);
            return Result.error("上传失败：" + e.getMessage());
        }
    }
    
    @DeleteMapping("/image")
    public Result deleteFile(@RequestBody Map<String, String> request) {
        try {
            String fileUrl = request.get("fileUrl");
            if (fileUrl == null || fileUrl.trim().isEmpty()) {
                return Result.error("文件路径不能为空");
            }
            
            boolean success = fileUploadService.deleteFile(fileUrl);
            if (success) {
                return Result.success("删除成功");
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            log.error("删除文件失败", e);
            return Result.error("删除失败：" + e.getMessage());
        }
    }
}