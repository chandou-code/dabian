package com.campus.errand.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

/**
 * 聊天消息实体类
 */
@Data
@TableName("chat_messages")
public class ChatMessage {
    private Long id;
    private Long chatId;
    private Long senderId;
    private String content;
    private String messageType;
    private Integer isRead;
    private Date createTime;
}
