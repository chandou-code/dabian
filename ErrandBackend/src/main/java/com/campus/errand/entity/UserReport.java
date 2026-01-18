package com.campus.errand.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

/**
 * 用户举报实体类
 */
@Data
@TableName("user_reports")
public class UserReport {
    /**
     * 举报ID
     */
    private Long id;
    
    /**
     * 举报者ID
     */
    private Long reporterId;
    
    /**
     * 被举报者ID
     */
    private Long reportedUserId;
    
    /**
     * 举报类型
     */
    private String type;
    
    /**
     * 举报内容
     */
    private String content;
    
    /**
     * 举报截图（JSON数组）
     */
    private String images;
    
    /**
     * 聊天ID
     */
    private Long chatId;
    
    /**
     * 聊天记录快照（JSON字符串）
     */
    private String chatRecords;
    
    /**
     * 举报状态：pending, processing, resolved, rejected
     */
    private String status;
    
    /**
     * 管理员处理备注
     */
    private String adminRemark;
    
    /**
     * 处理时间
     */
    private Date handledAt;
    
    /**
     * 创建时间
     */
    private Date createdAt;
    
    /**
     * 最后更新时间
     */
    private Date updatedAt;
    
    /**
     * 举报者姓名（非数据库字段，用于返回给前端）
     */
    @TableField(exist = false)
    private String reporterName;
    
    /**
     * 被举报者姓名（非数据库字段，用于返回给前端）
     */
    @TableField(exist = false)
    private String reportedName;
}