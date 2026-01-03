package com.campus.lostfound.service;

import com.campus.lostfound.dto.ClueDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 线索服务接口
 */
public interface ClueService {
    
    /**
     * 提交线索
     */
    boolean submitClue(Long itemId, ClueDTO clueDTO);
    
    /**
     * 上传线索图片
     */
    String uploadClueImage(MultipartFile file, Long itemId);
    
    /**
     * 获取物品的线索列表
     */
    List<Map<String, Object>> getCluesByItemId(Long itemId);
}