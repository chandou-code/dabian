package com.campus.lostfound.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 评论实体类
 */
@Data
@TableName("item_comments")
public class Comment {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("item_id")
    private Long itemId;
    
    @TableField("user_id")
    private Long userId;
    
    @TableField("content")
    private String content;
    
    @TableField("like_count")
    private Integer likeCount;
    
    @TableField("is_deleted")
    private Boolean isDeleted;
    
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}