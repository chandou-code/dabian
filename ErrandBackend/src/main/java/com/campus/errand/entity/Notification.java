package com.campus.errand.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

/**
 * 通知实体类
 */
@Data
@TableName("notifications")
public class Notification {
    /**
     * 通知ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 通知类型：task, system, chat, order
     */
    private String type;
    
    /**
     * 通知标题
     */
    private String title;
    
    /**
     * 通知内容
     */
    private String content;
    
    /**
     * 通知标签
     */
    private String tagName;
    
    /**
     * 关联任务ID（如果是任务相关通知）
     */
    private Long taskId;
    
    /**
     * 关联公告ID（如果是系统公告）
     */
    private Long noticeId;
    
    /**
     * 是否已读
     */
    private Boolean read;
    
    /**
     * 创建时间
     */
    private Date createdAt;
    
    /**
     * 已读时间
     */
    private Date readAt;
}
