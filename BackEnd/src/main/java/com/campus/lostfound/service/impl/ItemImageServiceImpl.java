package com.campus.lostfound.service.impl;

import com.campus.lostfound.entity.FileUpload;
import com.campus.lostfound.entity.Item;
import com.campus.lostfound.mapper.ItemMapper;
import com.campus.lostfound.service.AiService;
import com.campus.lostfound.service.FileUploadService;
import com.campus.lostfound.service.ItemImageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 物品图片服务实现类
 */
@Slf4j
@Service
public class ItemImageServiceImpl implements ItemImageService {
    
    @Autowired
    private FileUploadService fileUploadService;
    
    @Autowired
    private AiService aiService;
    
    @Autowired
    private ItemMapper itemMapper;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    @Transactional
    public List<Map<String, Object>> uploadItemImages(List<MultipartFile> files, String itemType, Long itemId, Long uploadUserId) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        if (files == null || files.isEmpty()) {
            return result;
        }
        
        // 检查图片数量限制
        if (files.size() > 6) {
            throw new RuntimeException("最多只能上传6张图片");
        }
        
        // 根据物品类型确定文件夹
        String folder = "lost_items".equals(itemType) ? "lost_items" : "found_items";
        
        // 上传文件
        List<String> fileUrls = fileUploadService.uploadFiles(files, folder);
        
        // 记录上传信息
        List<String> newImageUrls = new ArrayList<>();
        
        for (int i = 0; i < fileUrls.size(); i++) {
            MultipartFile file = files.get(i);
            String fileUrl = fileUrls.get(i);
            
            if (fileUrl != null) {
                // 创建上传记录
                FileUpload uploadRecord = new FileUpload()
                    .setOriginalName(file.getOriginalFilename())
                    .setFileName(fileUrl.substring(fileUrl.lastIndexOf('/') + 1))
                    .setFilePath(fileUrl)
                    .setFileSize(file.getSize())
                    .setFileType("image")
                    .setMimeType(file.getContentType())
                    .setUploadUserId(uploadUserId)
                    .setRelatedItemId(itemId);
                
                aiService.save(uploadRecord);
                
                // 构建可访问的URL
                String accessibleUrl = "/uploads/" + fileUrl;
                newImageUrls.add(accessibleUrl);
                
                result.add(new HashMap<String, Object>() {{
                    put("id", uploadRecord.getId());
                    put("url", accessibleUrl);
                    put("filename", file.getOriginalFilename());
                    put("size", file.getSize());
                    put("uploadTime", uploadRecord.getCreatedAt());
                }});
            }
        }
        
        // 更新物品的images字段
        if (!newImageUrls.isEmpty()) {
            updateItemImageUrls(itemId, newImageUrls);
        }
        
        log.info("物品 {} 上传了 {} 张图片", itemId, result.size());
        return result;
    }
    
    @Override
    public List<Map<String, Object>> getItemImages(Long itemId) {
        List<FileUpload> uploads = aiService.lambdaQuery()
            .eq(FileUpload::getRelatedItemId, itemId)
            .orderByDesc(FileUpload::getCreatedAt)
            .list();
        
        return uploads.stream().map(upload -> {
            Map<String, Object> imageMap = new HashMap<>();
            imageMap.put("id", upload.getId());
            imageMap.put("url", "/uploads/" + upload.getFilePath());
            imageMap.put("filename", upload.getOriginalName());
            imageMap.put("size", upload.getFileSize());
            imageMap.put("uploadTime", upload.getCreatedAt());
            imageMap.put("uploaderId", upload.getUploadUserId());
            return imageMap;
        }).collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public boolean deleteItemImage(Long uploadId, Long currentUserId) {
        FileUpload upload = aiService.getById(uploadId);
        if (upload == null) {
            return false;
        }
        
        // 检查权限：只有上传者本人或管理员可以删除
        if (!upload.getUploadUserId().equals(currentUserId)) {
            throw new RuntimeException("无权限删除此图片");
        }
        
        // 删除文件
        boolean fileDeleted = fileUploadService.deleteFile(upload.getFilePath());
        
        // 删除数据库记录
        boolean recordDeleted = aiService.removeById(uploadId);
        
        if (fileDeleted && recordDeleted) {
            // 更新物品的images字段
            updateItemImageUrls(upload.getRelatedItemId(), null);
            return true;
        }
        
        return false;
    }
    
    @Override
    @Transactional
    public void updateItemImageUrls(Long itemId, List<String> newImageUrls) {
        if (itemId == null) {
            return;
        }
        
        // 获取当前物品的所有图片
        List<FileUpload> allUploads = aiService.lambdaQuery()
            .eq(FileUpload::getRelatedItemId, itemId)
            .list();
        
        List<String> allImageUrls = new ArrayList<>();
        
        if (newImageUrls != null && !newImageUrls.isEmpty()) {
            // 添加新的图片URL
            allImageUrls.addAll(newImageUrls);
        }
        
        // 添加现有的图片URL
        for (FileUpload upload : allUploads) {
            String url = "/uploads/" + upload.getFilePath();
            if (!allImageUrls.contains(url)) {
                allImageUrls.add(url);
            }
        }
        
        // 生成JSON并更新
        String imagesJson = generateImagesJson(allImageUrls);
        
        Item item = new Item();
        item.setId(itemId);
        item.setImages(imagesJson);
        
        itemMapper.updateById(item);
        
        log.debug("更新物品 {} 的images字段: {}", itemId, imagesJson);
    }
    
    @Override
    public String generateImagesJson(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return null;
        }
        
        try {
            return objectMapper.writeValueAsString(imageUrls);
        } catch (JsonProcessingException e) {
            log.error("生成images JSON失败", e);
            return null;
        }
    }
    
    @Override
    public List<String> parseImagesJson(String imagesJson) {
        if (imagesJson == null || imagesJson.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        try {
            return objectMapper.readValue(imagesJson, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            log.error("解析images JSON失败: {}", imagesJson, e);
            return new ArrayList<>();
        }
    }
    
    @Override
    @Transactional
    public boolean updateItemImageAssociation(Long itemId, String itemType, List<String> imageUrls, Long userId) {
        try {
            // 验证物品是否存在且属于当前用户
            Item item = itemMapper.selectById(itemId);
            if (item == null) {
                log.warn("物品不存在: itemId={}", itemId);
                return false;
            }
            
            if (!item.getSubmitterId().equals(userId)) {
                log.warn("用户无权操作该物品: itemId={}, userId={}", itemId, userId);
                return false;
            }
            
            // 更新物品的images字段
            updateItemImageUrls(itemId, imageUrls);
            
            // 更新file_uploads表中的关联关系
            for (String imageUrl : imageUrls) {
                // 根据URL查找对应的上传记录
                List<FileUpload> uploads = fileUploadService.getUploadsByFileUrl(imageUrl);
                for (FileUpload upload : uploads) {
                    // 更新关联的物品ID
                    upload.setRelatedItemId(itemId);
                    fileUploadService.updateUpload(upload);
                }
            }
            
            log.info("成功更新物品图片关联关系: itemId={}, imageCount={}", itemId, imageUrls.size());
            return true;
            
        } catch (Exception e) {
            log.error("更新物品图片关联关系失败: itemId={}", itemId, e);
            return false;
        }
    }
}