package com.campus.lostfound.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 线索实体类
 */
@Data
@TableName("item_clues")
public class Clue {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("item_id")
    private Long itemId;
    
    @TableField("contact_name")
    private String contactName;
    
    @TableField("contact_phone")
    private String contactPhone;
    
    @TableField("contact_email")
    private String contactEmail;
    
    @TableField("content")
    private String content;
    
    @TableField("images")
    private String images; // JSON格式存储
    
    @TableField("status")
    private String status; // pending, reviewed, useful, invalid
    
    @TableField("is_deleted")
    private Boolean isDeleted;
    
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}