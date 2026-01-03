package com.campus.lostfound.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.campus.lostfound.dto.ClueDTO;
import com.campus.lostfound.entity.Clue;
import com.campus.lostfound.mapper.ClueMapper;
import com.campus.lostfound.service.ClueService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 线索服务实现
 */
@Slf4j
@Service
public class ClueServiceImpl implements ClueService {
    
    @Autowired
    private ClueMapper clueMapper;
    
    @Value("${upload.path:./uploads}")
    private String uploadPath;
    
    @Override
    @Transactional
    public boolean submitClue(Long itemId, ClueDTO clueDTO) {
        try {
            log.info("开始保存线索: itemId={}, clueDTO={}", itemId, clueDTO);
            
            Clue clue = new Clue();
            BeanUtils.copyProperties(clueDTO, clue);
            clue.setItemId(itemId);
            clue.setIsDeleted(false);
            clue.setCreatedAt(LocalDateTime.now());
            clue.setUpdatedAt(LocalDateTime.now());
            
            // 处理图片列表 - 转换为JSON格式
            if (clueDTO.getImages() != null && !clueDTO.getImages().isEmpty()) {
                try {
                    // 使用Jackson将List转换为JSON字符串
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    String imagesJson = mapper.writeValueAsString(clueDTO.getImages());
                    log.info("处理图片列表: {} -> {}", clueDTO.getImages(), imagesJson);
                    clue.setImages(imagesJson);
                } catch (Exception e) {
                    log.error("转换图片JSON失败", e);
                    clue.setImages("[]");
                }
            } else {
                clue.setImages("[]");
                log.info("图片列表为空，设置为空JSON数组");
            }
            
            // 如果没有状态，默认为pending
            if (clue.getStatus() == null) {
                clue.setStatus("pending");
            }
            
            log.info("准备插入数据库的线索: {}", clue);
            int result = clueMapper.insert(clue);
            log.info("数据库插入结果: {}", result);
            return result > 0;
        } catch (Exception e) {
            log.error("提交线索失败", e);
            return false;
        }
    }
    
    @Override
    public String uploadClueImage(MultipartFile file, Long itemId) {
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("文件为空");
            }
            
            // 创建上传目录
            String dateFolder = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String folderPath = uploadPath + "/clue_images/" + dateFolder;
            Path uploadDir = Paths.get(folderPath);
            
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
            
            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = UUID.randomUUID().toString() + extension;
            
            // 保存文件
            Path filePath = uploadDir.resolve(filename);
            Files.copy(file.getInputStream(), filePath);
            
            // 返回访问URL
            String relativePath = "/uploads/clue_images/" + dateFolder + "/" + filename;
            return relativePath;
            
        } catch (IOException e) {
            log.error("上传线索图片失败", e);
            throw new RuntimeException("图片上传失败：" + e.getMessage());
        }
    }
    
    @Override
    public List<Map<String, Object>> getCluesByItemId(Long itemId) {
        try {
            // 查询该物品的所有未删除线索
            QueryWrapper<Clue> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("item_id", itemId)
                       .eq("is_deleted", false)
                       .orderByDesc("created_at");
            
            List<Clue> clues = clueMapper.selectList(queryWrapper);
            
            // 转换为前端需要的格式
            return clues.stream().map(clue -> {
                Map<String, Object> clueMap = new java.util.HashMap<>();
                clueMap.put("id", clue.getId());
                clueMap.put("content", clue.getContent());
                clueMap.put("contactName", clue.getContactName());
                clueMap.put("contactPhone", clue.getContactPhone());
                // 解析JSON格式的图片列表
                if (clue.getImages() != null && !clue.getImages().trim().isEmpty()) {
                    try {
                        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                        String[] imagesArray = mapper.readValue(clue.getImages(), String[].class);
                        clueMap.put("images", imagesArray);
                    } catch (Exception e) {
                        log.error("解析图片JSON失败，使用空数组: {}", clue.getImages(), e);
                        clueMap.put("images", new String[0]);
                    }
                } else {
                    clueMap.put("images", new String[0]);
                }
                clueMap.put("created_at", clue.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                return clueMap;
            }).toList();
            
        } catch (Exception e) {
            log.error("获取线索列表失败，itemId={}", itemId, e);
            return new java.util.ArrayList<>();
        }
    }
}