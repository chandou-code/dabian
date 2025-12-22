package com.campus.lostfound.controller;

import com.campus.lostfound.common.Result;
import com.campus.lostfound.entity.FileUpload;
import com.campus.lostfound.service.FileUploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
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
    
    @Autowired
    private com.campus.lostfound.service.AiService aiService;
    
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
    
    /**
     * 失物招领专用图片上传接口
     * @param files 图片文件列表
     * @param folder 文件夹类型（lost_items/found_items）
     * @param itemType 物品类型（lost/found）
     * @param relatedItemId 相关物品ID（可选，用于后期关联）
     * @return 上传结果
     */
    @PostMapping("/item-images")
    public Result<List<Map<String, Object>>> uploadItemImages(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "folder", defaultValue = "lost_items") String folder,
            @RequestParam(value = "itemType", required = false) String itemType,
            @RequestParam(value = "relatedItemId", required = false) Long relatedItemId) {
        
        try {
            // 从请求头获取用户ID
            // 注意：这里应该从JWT token中获取，现在简化处理
            Long uploadUserId = getCurrentUserId();
            if (uploadUserId == null) {
                return Result.error("用户未登录");
            }
            
            if (files == null || files.isEmpty()) {
                return Result.error("请选择要上传的图片");
            }
            
            // 检查图片数量限制
            if (files.size() > 6) {
                return Result.error("最多只能上传6张图片");
            }
            
            // 根据物品类型确定文件夹
            String targetFolder = folder;
            if ("lost".equals(itemType)) {
                targetFolder = "lost_items";
            } else if ("found".equals(itemType)) {
                targetFolder = "found_items";
            }
            
            List<String> fileUrls = fileUploadService.uploadFiles(files, targetFolder);
            List<Map<String, Object>> result = new java.util.ArrayList<>();
            
            // 记录上传信息到数据库
            for (int i = 0; i < fileUrls.size(); i++) {
                MultipartFile file = files.get(i);
                String fileUrl = fileUrls.get(i);
                
                // 创建文件上传记录
                FileUpload uploadRecord = new FileUpload()
                    .setOriginalName(file.getOriginalFilename())
                    .setFileName(fileUrl.substring(fileUrl.lastIndexOf('/') + 1))
                    .setFilePath(fileUrl)
                    .setFileSize(file.getSize())
                    .setFileType("image")
                    .setMimeType(file.getContentType())
                    .setUploadUserId(uploadUserId)
                    .setRelatedItemId(relatedItemId);
                
                aiService.save(uploadRecord);
                
                result.add(Map.of(
                    "id", uploadRecord.getId(),
                    "url", "/uploads/" + fileUrl, // 返回可访问的完整URL
                    "filename", file.getOriginalFilename(),
                    "size", file.getSize(),
                    "uploadTime", uploadRecord.getCreatedAt()
                ));
            }
            
            log.info("用户 {} 成功上传 {} 张图片到 {}", uploadUserId, fileUrls.size(), targetFolder);
            return Result.success("图片上传成功", result);
            
        } catch (Exception e) {
            log.error("失物招领图片上传失败", e);
            return Result.error("上传失败：" + e.getMessage());
        }
    }
    
    /**
     * 根据物品ID获取关联的图片列表
     */
    @GetMapping("/item-images/{itemId}")
    public Result<List<Map<String, Object>>> getItemImages(@PathVariable Long itemId) {
        try {
            Long currentUserId = getCurrentUserId();
            if (currentUserId == null) {
                return Result.error("用户未登录");
            }
            
            // 查询物品关联的图片
            List<FileUpload> uploads = aiService.lambdaQuery()
                .eq(FileUpload::getRelatedItemId, itemId)
                .orderByDesc(FileUpload::getCreatedAt)
                .list();
            
            List<Map<String, Object>> result = new java.util.ArrayList<>();
            for (FileUpload upload : uploads) {
                result.add(Map.of(
                    "id", upload.getId(),
                    "url", "/uploads/" + upload.getFilePath(),
                    "filename", upload.getOriginalName(),
                    "size", upload.getFileSize(),
                    "uploadTime", upload.getCreatedAt(),
                    "uploaderId", upload.getUploadUserId()
                ));
            }
            
            return Result.success("获取成功", result);
            
        } catch (Exception e) {
            log.error("获取物品图片失败", e);
            return Result.error("获取失败：" + e.getMessage());
        }
    }
    
    /**
     * 从当前请求中获取用户ID（简化版本）
     * 实际应用中应该从JWT token中解析
     */
    private Long getCurrentUserId() {
        // TODO: 实现从JWT token解析用户ID
        // 现在返回默认用户ID用于测试
        return 1L; // 暂时返回用户ID为1
    }
}