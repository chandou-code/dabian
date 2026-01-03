package com.campus.lostfound.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.lostfound.dto.CommentDTO;
import com.campus.lostfound.entity.Comment;
import com.campus.lostfound.entity.CommentLike;
import com.campus.lostfound.entity.User;
import com.campus.lostfound.mapper.CommentMapper;
import com.campus.lostfound.mapper.CommentLikeMapper;
import com.campus.lostfound.mapper.UserMapper;
import com.campus.lostfound.service.CommentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 评论服务实现
 */
@Slf4j
@Service
public class CommentServiceImpl implements CommentService {
    
    @Autowired
    private CommentMapper commentMapper;
    
    @Autowired
    private CommentLikeMapper commentLikeMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Override
    public Map<String, Object> getCommentsByItemId(Long itemId, int current, int pageSize) {
        try {
            // 分页查询评论
            Page<Comment> page = new Page<>(current, pageSize);
            QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("item_id", itemId)
                    .eq("is_deleted", false)
                    .orderByDesc("created_at");
            
            IPage<Comment> commentPage = commentMapper.selectPage(page, queryWrapper);
            
            // 转换为DTO并填充用户信息
            List<CommentDTO> commentDTOs = commentPage.getRecords().stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            
            Map<String, Object> result = new HashMap<>();
            result.put("total", commentPage.getTotal());
            result.put("list", commentDTOs);
            
            return result;
        } catch (Exception e) {
            log.error("获取评论列表失败", e);
            return Collections.emptyMap();
        }
    }
    
    @Override
    @Transactional
    public boolean submitComment(Long itemId, Long userId, CommentDTO commentDTO) {
        try {
            Comment comment = new Comment();
            comment.setItemId(itemId);
            comment.setUserId(userId);
            comment.setContent(commentDTO.getContent());
            comment.setLikeCount(0);
            comment.setIsDeleted(false);
            comment.setCreatedAt(LocalDateTime.now());
            comment.setUpdatedAt(LocalDateTime.now());
            
            int result = commentMapper.insert(comment);
            return result > 0;
        } catch (Exception e) {
            log.error("提交评论失败", e);
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean toggleLike(Long commentId, Long userId) {
        try {
            // 检查是否已经点赞
            QueryWrapper<CommentLike> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("comment_id", commentId)
                    .eq("user_id", userId);
            
            CommentLike existingLike = commentLikeMapper.selectOne(queryWrapper);
            
            if (existingLike != null) {
                // 取消点赞
                commentLikeMapper.deleteById(existingLike.getId());
                commentMapper.decrementLikeCount(commentId);
                return false;
            } else {
                // 添加点赞
                CommentLike commentLike = new CommentLike();
                commentLike.setCommentId(commentId);
                commentLike.setUserId(userId);
                commentLike.setCreatedAt(LocalDateTime.now());
                
                commentLikeMapper.insert(commentLike);
                commentMapper.incrementLikeCount(commentId);
                return true;
            }
        } catch (Exception e) {
            log.error("点赞操作失败", e);
            return false;
        }
    }
    
    /**
     * 转换为DTO
     */
    private CommentDTO convertToDTO(Comment comment) {
        CommentDTO dto = new CommentDTO();
        BeanUtils.copyProperties(comment, dto);
        
        // 获取用户信息
        User user = userMapper.selectById(comment.getUserId());
        if (user != null) {
            dto.setUserName(user.getRealName() != null ? user.getRealName() : user.getUsername());
            dto.setUserAvatar(user.getAvatar());
        } else {
            dto.setUserName("匿名用户");
        }
        
        // 格式化时间
        if (comment.getCreatedAt() != null) {
            dto.setCreatedAt(comment.getCreatedAt().toString());
        }
        
        // 默认未点赞
        dto.setIsLiked(false);
        
        return dto;
    }
}