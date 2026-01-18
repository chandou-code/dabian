package com.campus.errand.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.errand.entity.Chat;
import com.campus.errand.entity.ChatMessage;
import com.campus.errand.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 聊天控制器
 */
@Slf4j
@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    /**
     * 创建或获取聊天会话
     */
    @PostMapping("/create")
    public Map<String, Object> createOrGetChat(
            @RequestBody Map<String, Object> requestBody,
            @RequestAttribute("currentUserId") Long userId) {
        try {
            Long targetUserId = Long.parseLong(requestBody.get("targetUserId").toString());
            Long taskId = requestBody.get("taskId") != null ? Long.parseLong(requestBody.get("taskId").toString()) : null;
            
            Chat chat = chatService.createOrGetChat(userId, targetUserId, taskId);
            if (chat != null) {
                log.info("创建或获取聊天会话成功: {}, 用户: {}, 目标用户: {}", chat.getId(), userId, targetUserId);
                return Map.of("code", 200, "msg", "成功", "data", chat);
            } else {
                log.warn("创建或获取聊天会话失败: 用户: {}, 目标用户: {}", userId, targetUserId);
                return Map.of("code", 400, "msg", "失败", "data", null);
            }
        } catch (Exception e) {
            log.error("创建或获取聊天会话异常", e);
            return Map.of("code", 500, "msg", "失败：" + e.getMessage(), "data", null);
        }
    }

    /**
     * 获取用户的聊天会话列表
     */
    @GetMapping("/list")
    public Map<String, Object> getUserChatList(
            @RequestAttribute("currentUserId") Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        try {
            Page<Chat> chatPage = chatService.getUserChatList(userId, page, pageSize);
            Map<String, Object> data = new HashMap<>();
            data.put("total", chatPage.getTotal());
            data.put("list", chatPage.getRecords());
            data.put("current", chatPage.getCurrent());
            data.put("size", chatPage.getSize());

            return Map.of("code", 200, "msg", "获取成功", "data", data);
        } catch (Exception e) {
            log.error("获取聊天会话列表异常", e);
            return Map.of("code", 500, "msg", "获取失败：" + e.getMessage(), "data", null);
        }
    }

    /**
     * 获取聊天消息列表
     */
    @GetMapping("/{chatId}/messages")
    public Map<String, Object> getChatMessages(
            @PathVariable Long chatId,
            @RequestAttribute("currentUserId") Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        try {
            // 标记消息为已读
            chatService.markMessagesAsRead(chatId, userId);

            Page<ChatMessage> messagePage = chatService.getChatMessages(chatId, page, pageSize);
            Map<String, Object> data = new HashMap<>();
            data.put("total", messagePage.getTotal());
            data.put("list", messagePage.getRecords());
            data.put("current", messagePage.getCurrent());
            data.put("size", messagePage.getSize());

            return Map.of("code", 200, "msg", "获取成功", "data", data);
        } catch (Exception e) {
            log.error("获取聊天消息列表异常", e);
            return Map.of("code", 500, "msg", "获取失败：" + e.getMessage(), "data", null);
        }
    }

    /**
     * 发送消息
     */
    @PostMapping("/send")
    public Map<String, Object> sendMessage(
            @RequestBody ChatMessage message,
            @RequestAttribute("currentUserId") Long userId) {
        try {
            message.setSenderId(userId);
            ChatMessage sentMessage = chatService.sendMessage(message);
            if (sentMessage != null) {
                log.info("发送消息成功: {}, 会话: {}, 发送人: {}", sentMessage.getId(), message.getChatId(), userId);
                return Map.of("code", 200, "msg", "发送成功", "data", sentMessage);
            } else {
                log.warn("发送消息失败: 会话: {}, 发送人: {}", message.getChatId(), userId);
                return Map.of("code", 400, "msg", "发送失败", "data", null);
            }
        } catch (Exception e) {
            log.error("发送消息异常", e);
            return Map.of("code", 500, "msg", "发送失败：" + e.getMessage(), "data", null);
        }
    }

    /**
     * 标记消息为已读
     */
    @PutMapping("/{chatId}/read")
    public Map<String, Object> markMessagesAsRead(
            @PathVariable Long chatId,
            @RequestAttribute("currentUserId") Long userId) {
        try {
            boolean result = chatService.markMessagesAsRead(chatId, userId);
            if (result) {
                log.info("标记消息已读成功: 会话: {}, 用户: {}", chatId, userId);
                return Map.of("code", 200, "msg", "标记成功", "data", null);
            } else {
                log.warn("标记消息已读失败: 会话: {}, 用户: {}", chatId, userId);
                return Map.of("code", 400, "msg", "标记失败", "data", null);
            }
        } catch (Exception e) {
            log.error("标记消息已读异常", e);
            return Map.of("code", 500, "msg", "标记失败：" + e.getMessage(), "data", null);
        }
    }

    /**
     * 获取未读消息数量
     */
    @GetMapping("/unread-count")
    public Map<String, Object> getUnreadMessageCount(@RequestAttribute("currentUserId") Long userId) {
        try {
            int count = chatService.getUnreadMessageCount(userId);
            log.info("获取未读消息数量: 用户: {}, 数量: {}", userId, count);
            return Map.of("code", 200, "msg", "获取成功", "data", count);
        } catch (Exception e) {
            log.error("获取未读消息数量异常", e);
            return Map.of("code", 500, "msg", "获取失败：" + e.getMessage(), "data", 0);
        }
    }

    /**
     * 删除聊天会话
     */
    @DeleteMapping("/{chatId}")
    public Map<String, Object> deleteChat(
            @PathVariable Long chatId,
            @RequestAttribute("currentUserId") Long userId) {
        try {
            boolean result = chatService.deleteChat(chatId, userId);
            if (result) {
                log.info("删除聊天会话成功: {}, 用户: {}", chatId, userId);
                return Map.of("code", 200, "msg", "删除成功", "data", null);
            } else {
                log.warn("删除聊天会话失败: {}, 用户: {}", chatId, userId);
                return Map.of("code", 400, "msg", "删除失败", "data", null);
            }
        } catch (Exception e) {
            log.error("删除聊天会话异常", e);
            return Map.of("code", 500, "msg", "删除失败：" + e.getMessage(), "data", null);
        }
    }
    
    /**
     * 清理聊天记录
     */
    @DeleteMapping("/{chatId}/messages")
    public Map<String, Object> clearChatMessages(
            @PathVariable Long chatId,
            @RequestAttribute("currentUserId") Long userId) {
        try {
            boolean result = chatService.clearChatMessages(chatId, userId);
            if (result) {
                log.info("清理聊天记录成功: {}, 用户: {}", chatId, userId);
                return Map.of("code", 200, "msg", "清理成功", "data", null);
            } else {
                log.warn("清理聊天记录失败: {}, 用户: {}", chatId, userId);
                return Map.of("code", 400, "msg", "清理失败", "data", null);
            }
        } catch (Exception e) {
            log.error("清理聊天记录异常", e);
            return Map.of("code", 500, "msg", "清理失败：" + e.getMessage(), "data", null);
        }
    }
}
