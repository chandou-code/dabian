package com.campus.lostfound.dto;

import lombok.Data;

/**
 * 评论DTO
 */
@Data
public class CommentDTO {
    private Long id;
    private Long itemId;
    private Long userId;
    private String content;
    private Integer likeCount;
    private Boolean isLiked; // 当前用户是否已点赞
    private String userName;
    private String userAvatar;
    private String createdAt;
}