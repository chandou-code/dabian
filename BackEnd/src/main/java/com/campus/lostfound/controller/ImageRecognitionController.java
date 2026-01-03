package com.campus.lostfound.controller;

import com.campus.lostfound.common.Result;
import com.campus.lostfound.service.ImageRecognitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 图像识别控制器
 */
@RestController
@RequestMapping("/image")
@CrossOrigin
public class ImageRecognitionController {

    @Autowired
    private ImageRecognitionService imageRecognitionService;

    /**
     * 智能生成物品描述
     * @param file 上传的图片文件
     * @return 物品信息
     */
    @PostMapping("/generate-description")
    public Result<Map<String, String>> generateDescription(@RequestParam("file") MultipartFile file) {
        try {
            // 调用服务层处理图片和AI调用
            Map<String, String> itemInfo = imageRecognitionService.generateItemDescription(file);
            return Result.success(itemInfo);
        } catch (Exception e) {
            return Result.error("生成描述失败：" + e.getMessage());
        }
    }
}
