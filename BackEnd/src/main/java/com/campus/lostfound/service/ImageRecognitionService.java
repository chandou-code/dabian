package com.campus.lostfound.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 图像识别服务接口
 */
public interface ImageRecognitionService {

    /**
     * 根据上传的图片生成物品描述
     * @param file 上传的图片文件
     * @return 物品信息，包含item、detail、category
     */
    Map<String, String> generateItemDescription(MultipartFile file);
}
