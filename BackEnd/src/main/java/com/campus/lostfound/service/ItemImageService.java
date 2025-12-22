package com.campus.lostfound.service;

import com.campus.lostfound.entity.Item;
import com.campus.lostfound.entity.FileUpload;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 物品图片服务接口
 */
public interface ItemImageService {
    
    /**
     * 为物品上传图片
     * @param files 图片文件列表
     * @param itemType 物品类型（lost/found）
     * @param itemId 物品ID
     * @param uploadUserId 上传用户ID
     * @return 上传结果
     */
    List<Map<String, Object>> uploadItemImages(List<MultipartFile> files, String itemType, Long itemId, Long uploadUserId);
    
    /**
     * 获取物品关联的图片列表
     * @param itemId 物品ID
     * @return 图片列表
     */
    List<Map<String, Object>> getItemImages(Long itemId);
    
    /**
     * 删除物品图片
     * @param uploadId 上传记录ID
     * @param currentUserId 当前用户ID
     * @return 删除结果
     */
    boolean deleteItemImage(Long uploadId, Long currentUserId);
    
    /**
     * 更新物品的images字段
     * @param itemId 物品ID
     * @param imageUrls 图片URL列表
     */
    void updateItemImageUrls(Long itemId, List<String> imageUrls);
    
    /**
     * 根据图片URL列表生成images JSON字符串
     * @param imageUrls 图片URL列表
     * @return JSON字符串
     */
    String generateImagesJson(List<String> imageUrls);
    
    /**
     * 解析images JSON字符串为URL列表
     * @param imagesJson images字段的JSON字符串
     * @return URL列表
     */
    List<String> parseImagesJson(String imagesJson);
    
    /**
     * 更新物品图片关联关系
     * @param itemId 物品ID
     * @param itemType 物品类型
     * @param imageUrls 图片URL列表
     * @param userId 用户ID
     * @return 更新结果
     */
    boolean updateItemImageAssociation(Long itemId, String itemType, List<String> imageUrls, Long userId);
}