package com.campus.lostfound.service;

import com.campus.lostfound.dto.CommentDTO;
import java.util.Map;

/**
 * 评论服务接口
 */
public interface CommentService {
    
    /**
     * 获取物品评论列表
     */
    Map<String, Object> getCommentsByItemId(Long itemId, int current, int pageSize);
    
    /**
     * 提交评论
     */
    boolean submitComment(Long itemId, Long userId, CommentDTO commentDTO);
    
    /**
     * 点赞/取消点赞
     */
    boolean toggleLike(Long commentId, Long userId);
}