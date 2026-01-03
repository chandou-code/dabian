package com.campus.lostfound.controller;

import com.campus.lostfound.common.Result;
import com.campus.lostfound.dto.CommentDTO;
import com.campus.lostfound.dto.ClueDTO;
import com.campus.lostfound.dto.ItemDetailDTO;
import com.campus.lostfound.service.CommentService;
import com.campus.lostfound.service.ClueService;
import com.campus.lostfound.service.ItemService;
import com.campus.lostfound.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 失物招领详情页控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/item")
public class ItemDetailController {
    
    @Autowired
    private ItemService itemService;
    
    @Autowired
    private CommentService commentService;
    
    @Autowired
    private ClueService clueService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 获取物品详情
     */
    @GetMapping("/{id}")
    public Result<ItemDetailDTO> getItemDetail(@PathVariable Long id) {
        try {
            log.info("获取物品详情: id={}", id);
            ItemDetailDTO itemDetail = itemService.getItemDetailWithUser(id);
            if (itemDetail == null) {
                return Result.error("物品不存在");
            }
            return Result.success(itemDetail);
        } catch (Exception e) {
            log.error("获取物品详情失败", e);
            return Result.error("获取物品详情失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取物品评论列表
     */
    @GetMapping("/{id}/comments")
    public Result<Map<String, Object>> getItemComments(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int pageSize) {
        try {
            log.info("获取物品评论: id={}, current={}, pageSize={}", id, current, pageSize);
            Map<String, Object> result = commentService.getCommentsByItemId(id, current, pageSize);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取物品评论失败", e);
            return Result.error("获取评论失败：" + e.getMessage());
        }
    }
    
    /**
     * 提交评论
     */
    @PostMapping("/{id}/comment")
    public Result<String> submitComment(
            @PathVariable Long id,
            @RequestBody CommentDTO commentDTO,
            @RequestHeader("Authorization") String authorization) {
        try {
            Long userId = getUserIdFromToken(authorization);
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            log.info("提交评论: itemId={}, userId={}, content={}", id, userId, commentDTO.getContent());
            boolean success = commentService.submitComment(id, userId, commentDTO);
            
            if (success) {
                return Result.success("评论提交成功");
            } else {
                return Result.error("评论提交失败");
            }
        } catch (Exception e) {
            log.error("提交评论失败", e);
            return Result.error("评论提交失败：" + e.getMessage());
        }
    }
    
    /**
     * 点赞/取消点赞评论
     */
    @PostMapping("/{id}/comment/{commentId}/like")
    public Result<String> toggleLikeComment(
            @PathVariable Long id,
            @PathVariable Long commentId,
            @RequestHeader("Authorization") String authorization) {
        try {
            Long userId = getUserIdFromToken(authorization);
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            boolean liked = commentService.toggleLike(commentId, userId);
            String message = liked ? "点赞成功" : "取消点赞成功";
            
            return Result.success(message);
        } catch (Exception e) {
            log.error("点赞操作失败", e);
            return Result.error("操作失败：" + e.getMessage());
        }
    }
    
    /**
     * 提交线索
     */
    @PostMapping("/{id}/clue")
    public Result<String> submitClue(
            @PathVariable Long id,
            @RequestBody ClueDTO clueDTO) {
        try {
            log.info("提交线索 - 完整数据: itemId={}, clueDTO={}", id, clueDTO);
            log.info("提交线索: itemId={}, contactName={}, contactPhone={}, images={}", 
                    id, clueDTO.getContactName(), maskPhone(clueDTO.getContactPhone()), clueDTO.getImages());
            
            boolean success = clueService.submitClue(id, clueDTO);
            if (success) {
                return Result.success("线索提交成功，感谢您的帮助！");
            } else {
                return Result.error("线索提交失败");
            }
        } catch (Exception e) {
            log.error("提交线索失败", e);
            return Result.error("线索提交失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取物品线索列表
     */
    @GetMapping("/{id}/clues")
    public Result<java.util.List<Map<String, Object>>> getItemClues(@PathVariable Long id) {
        try {
            log.info("获取物品线索列表: itemId={}", id);
            
            // 验证物品是否存在
            ItemDetailDTO itemDetail = itemService.getItemDetailWithUser(id);
            if (itemDetail == null) {
                return Result.error("物品不存在");
            }
            
            // 从数据库查询线索列表
            java.util.List<Map<String, Object>> clues = clueService.getCluesByItemId(id);
            
            log.info("获取物品线索成功: itemId={}, 线索数量={}", id, clues.size());
            return Result.success(clues);
        } catch (Exception e) {
            log.error("获取物品线索失败", e);
            return Result.error("获取线索列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 上传线索图片
     */
    @PostMapping("/{id}/clue/image")
    public Result<String> uploadClueImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return Result.error("请选择要上传的图片");
            }
            
            // 保存图片并返回URL
            String imageUrl = clueService.uploadClueImage(file, id);
            return Result.success("图片上传成功", imageUrl);
        } catch (Exception e) {
            log.error("上传线索图片失败", e);
            return Result.error("图片上传失败：" + e.getMessage());
        }
    }
    
    /**
     * 从token中提取用户ID
     */
    private Long getUserIdFromToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        
        String token = authorization.substring(7);
        try {
            return jwtUtil.getUserIdFromToken(token);
        } catch (Exception e) {
            log.error("从token中提取用户ID失败", e);
            return null;
        }
    }
    
    /**
     * 手机号脱敏
     */
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }
}