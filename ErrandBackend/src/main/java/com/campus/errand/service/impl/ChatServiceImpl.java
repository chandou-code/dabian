package com.campus.errand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.errand.entity.Chat;
import com.campus.errand.entity.ChatMessage;
import com.campus.errand.mapper.ChatMapper;
import com.campus.errand.mapper.ChatMessageMapper;
import com.campus.errand.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 聊天服务实现类
 */
@Slf4j
@Service
public class ChatServiceImpl implements ChatService {

    @Autowired
    private ChatMapper chatMapper;

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Override
    public Chat createOrGetChat(Long userId1, Long userId2, Long taskId) {
        // 防止创建与自己的聊天会话
        if (userId1.equals(userId2)) {
            log.warn("尝试创建与自己的聊天会话: {}", userId1);
            return null;
        }
        
        // 确保userId1 < userId2，避免重复创建会话
        if (userId1 > userId2) {
            Long temp = userId1;
            userId1 = userId2;
            userId2 = temp;
        }

        // 查询是否已存在会话
        LambdaQueryWrapper<Chat> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Chat::getUserId1, userId1)
                .eq(Chat::getUserId2, userId2);

        Chat existingChat = chatMapper.selectOne(queryWrapper);
        if (existingChat != null) {
            log.info("聊天会话已存在: {}, 用户1: {}, 用户2: {}", existingChat.getId(), userId1, userId2);
            return existingChat;
        }

        // 创建新会话
        Chat chat = new Chat();
        chat.setUserId1(userId1);
        chat.setUserId2(userId2);

        int result = chatMapper.insert(chat);
        if (result > 0) {
            log.info("聊天会话创建成功: {}, 用户1: {}, 用户2: {}", chat.getId(), userId1, userId2);
            return chat;
        } else {
            log.error("聊天会话创建失败: 用户1: {}, 用户2: {}", userId1, userId2);
            return null;
        }
    }

    @Override
    public Page<Chat> getUserChatList(Long userId, int page, int pageSize) {
        Page<Chat> chatPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<Chat> queryWrapper = new LambdaQueryWrapper<>();

        // 查询用户参与的所有会话
        queryWrapper.eq(Chat::getUserId1, userId)
                .or().eq(Chat::getUserId2, userId);

        // 按最后消息时间倒序排序
        queryWrapper.orderByDesc(Chat::getLastMessageTime);

        return chatMapper.selectPage(chatPage, queryWrapper);
    }

    @Override
    public Page<ChatMessage> getChatMessages(Long chatId, int page, int pageSize) {
        Page<ChatMessage> messagePage = new Page<>(page, pageSize);
        LambdaQueryWrapper<ChatMessage> queryWrapper = new LambdaQueryWrapper<>();

        // 查询指定会话的消息
        queryWrapper.eq(ChatMessage::getChatId, chatId);

        // 按创建时间倒序排序
        queryWrapper.orderByDesc(ChatMessage::getCreateTime);

        return chatMessageMapper.selectPage(messagePage, queryWrapper);
    }

    @Override
    public ChatMessage sendMessage(ChatMessage message) {
        // 设置默认值
        message.setCreateTime(new Date());
        message.setIsRead(0);

        // 保存消息
        int result = chatMessageMapper.insert(message);
        if (result > 0) {
            // 更新会话的最后消息时间
            Chat chat = chatMapper.selectById(message.getChatId());
            if (chat != null) {
                chat.setLastMessage(message.getContent());
                chat.setLastMessageTime(new Date());
                chatMapper.updateById(chat);
            }

            log.info("消息发送成功: {}, 会话: {}, 发送人: {}", message.getId(), message.getChatId(), message.getSenderId());
            return message;
        } else {
            log.error("消息发送失败: 会话: {}, 发送人: {}", message.getChatId(), message.getSenderId());
            return null;
        }
    }

    @Override
    public boolean markMessagesAsRead(Long chatId, Long userId) {
        LambdaQueryWrapper<ChatMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatMessage::getChatId, chatId)
                .ne(ChatMessage::getSenderId, userId)
                .eq(ChatMessage::getIsRead, 0);

        // 批量更新为已读
        ChatMessage updateMessage = new ChatMessage();
        updateMessage.setIsRead(1);

        int result = chatMessageMapper.update(updateMessage, queryWrapper);
        if (result > 0) {
            log.info("消息标记已读成功: 会话: {}, 用户: {}, 数量: {}", chatId, userId, result);
            return true;
        } else {
            log.warn("消息标记已读失败: 会话: {}, 用户: {}", chatId, userId);
            return false;
        }
    }

    @Override
    public int getUnreadMessageCount(Long userId) {
        LambdaQueryWrapper<ChatMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ne(ChatMessage::getSenderId, userId)
                .eq(ChatMessage::getIsRead, 0);

        return Math.toIntExact(chatMessageMapper.selectCount(queryWrapper));
    }

    @Override
    public boolean deleteChat(Long chatId, Long userId) {
        // 查询会话是否存在
        Chat chat = chatMapper.selectById(chatId);
        if (chat == null) {
            log.warn("聊天会话不存在: {}", chatId);
            return false;
        }

        // 检查用户是否有权限删除
        if (!chat.getUserId1().equals(userId) && !chat.getUserId2().equals(userId)) {
            log.warn("无权限删除聊天会话: {}, 用户: {}", chatId, userId);
            return false;
        }

        // 删除会话相关的所有消息
        LambdaQueryWrapper<ChatMessage> messageQueryWrapper = new LambdaQueryWrapper<>();
        messageQueryWrapper.eq(ChatMessage::getChatId, chatId);
        chatMessageMapper.delete(messageQueryWrapper);

        // 删除会话
        int result = chatMapper.deleteById(chatId);
        if (result > 0) {
            log.info("聊天会话删除成功: {}, 用户: {}", chatId, userId);
            return true;
        } else {
            log.error("聊天会话删除失败: {}, 用户: {}", chatId, userId);
            return false;
        }
    }

    @Override
    public Chat getChatById(Long chatId) {
        return chatMapper.selectById(chatId);
    }
    
    @Override
    public boolean clearChatMessages(Long chatId, Long userId) {
        // 查询会话是否存在
        Chat chat = chatMapper.selectById(chatId);
        if (chat == null) {
            log.warn("聊天会话不存在: {}", chatId);
            return false;
        }
        
        // 检查用户是否有权限清理该会话的聊天记录
        if (!chat.getUserId1().equals(userId) && !chat.getUserId2().equals(userId)) {
            log.warn("无权限清理聊天记录: {}, 用户: {}", chatId, userId);
            return false;
        }
        
        // 删除会话相关的所有消息
        LambdaQueryWrapper<ChatMessage> messageQueryWrapper = new LambdaQueryWrapper<>();
        messageQueryWrapper.eq(ChatMessage::getChatId, chatId);
        int deletedCount = chatMessageMapper.delete(messageQueryWrapper);
        
        if (deletedCount > 0) {
            // 更新会话的最后消息信息
            chat.setLastMessage("聊天记录已清理");
            chat.setLastMessageTime(new Date());
            chatMapper.updateById(chat);
            
            log.info("聊天记录清理成功: {}, 用户: {}, 删除消息数: {}", chatId, userId, deletedCount);
            return true;
        } else {
            log.warn("聊天记录清理失败或无消息可清理: {}, 用户: {}", chatId, userId);
            return true; // 即使没有消息可清理，也返回成功
        }
    }
}
