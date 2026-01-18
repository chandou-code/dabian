package com.campus.errand.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

/**
 * 聊天会话实体类
 */
@Data
@TableName("chats")
public class Chat {
    private Long id;
    private Long userId1;
    private Long userId2;
    private String lastMessage;
    private Date lastMessageTime;
    private Integer unreadCount1;
    private Integer unreadCount2;
}
