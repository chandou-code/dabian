package com.campus.errand.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.errand.entity.Chat;
import com.campus.errand.entity.ChatMessage;

import java.util.List;

/**
 * 聊天服务接口
 */
public interface ChatService {

    /**
     * 创建或获取聊天会话
     * @param userId1 用户1 ID
     * @param userId2 用户2 ID
     * @param taskId 关联任务ID（可选）
     * @return 聊天会话
     */
    Chat createOrGetChat(Long userId1, Long userId2, Long taskId);

    /**
     * 获取用户的聊天会话列表
     * @param userId 用户ID
     * @param page 当前页码
     * @param pageSize 每页大小
     * @return 聊天会话列表
     */
    Page<Chat> getUserChatList(Long userId, int page, int pageSize);

    /**
     * 获取聊天消息列表
     * @param chatId 聊天会话ID
     * @param page 当前页码
     * @param pageSize 每页大小
     * @return 聊天消息列表
     */
    Page<ChatMessage> getChatMessages(Long chatId, int page, int pageSize);

    /**
     * 发送消息
     * @param message 消息信息
     * @return 发送的消息
     */
    ChatMessage sendMessage(ChatMessage message);

    /**
     * 标记消息为已读
     * @param chatId 聊天会话ID
     * @param userId 用户ID
     * @return 是否标记成功
     */
    boolean markMessagesAsRead(Long chatId, Long userId);

    /**
     * 获取未读消息数量
     * @param userId 用户ID
     * @return 未读消息数量
     */
    int getUnreadMessageCount(Long userId);

    /**
     * 删除聊天会话
     * @param chatId 聊天会话ID
     * @param userId 用户ID
     * @return 是否删除成功
     */
    boolean deleteChat(Long chatId, Long userId);

    /**
     * 通过ID获取聊天会话
     * @param chatId 聊天会话ID
     * @return 聊天会话
     */
    Chat getChatById(Long chatId);
    
    /**
     * 清理聊天记录
     * @param chatId 聊天会话ID
     * @param userId 用户ID
     * @return 是否清理成功
     */
    boolean clearChatMessages(Long chatId, Long userId);
}
