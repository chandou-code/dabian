package com.campus.lostfound.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 评论点赞实体类
 */
@Data
@TableName("comment_likes")
public class CommentLike {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("comment_id")
    private Long commentId;
    
    @TableField("user_id")
    private Long userId;
    
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}