package com.campus.errand.common.handler;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.campus.errand.entity.ChatMessage;
import com.campus.errand.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket消息处理器
 */
@Slf4j
@Component
public class WebSocketHandler extends AbstractWebSocketHandler {

    private static final Map<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    @Autowired
    private ChatService chatService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 从会话属性中获取用户ID
        Long userId = (Long) session.getAttributes().get("currentUserId");
        if (userId != null) {
            userSessions.put(userId, session);
            log.info("WebSocket连接建立成功: 用户ID={}, 会话ID={}", userId, session.getId());
            
            // 发送连接成功消息
            JSONObject message = new JSONObject();
            message.put("type", "connect");
            message.put("message", "连接成功");
            session.sendMessage(new TextMessage(message.toJSONString()));
        } else {
            log.warn("WebSocket连接建立失败: 缺少用户ID");
            session.close(CloseStatus.SESSION_NOT_RELIABLE);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) throws Exception {
        // 从会话属性中获取用户ID
        Long userId = (Long) session.getAttributes().get("currentUserId");
        if (userId == null) {
            log.warn("处理消息失败: 缺少用户ID");
            return;
        }

        try {
            // 解析消息内容
            JSONObject messageData = JSON.parseObject(textMessage.getPayload());
            String type = messageData.getString("type");

            switch (type) {
                case "send":
                    // 处理发送消息
                    handleSendMessage(userId, messageData);
                    break;
                case "read":
                    // 处理消息已读
                    handleReadMessage(userId, messageData);
                    break;
                default:
                    log.warn("未知消息类型: {}", type);
            }
        } catch (Exception e) {
            log.error("处理WebSocket消息异常", e);
            
            // 发送错误消息
            JSONObject errorMessage = new JSONObject();
            errorMessage.put("type", "error");
            errorMessage.put("message", "处理消息失败: " + e.getMessage());
            session.sendMessage(new TextMessage(errorMessage.toJSONString()));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // 从会话属性中获取用户ID
        Long userId = (Long) session.getAttributes().get("currentUserId");
        if (userId != null) {
            userSessions.remove(userId);
            log.info("WebSocket连接关闭: 用户ID={}, 会话ID={}, 状态={}", userId, session.getId(), status);
        } else {
            log.warn("WebSocket连接关闭: 缺少用户ID, 会话ID={}", session.getId());
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket传输错误: 会话ID={}", session.getId(), exception);
        // 连接错误时关闭会话
        if (session.isOpen()) {
            session.close(CloseStatus.SERVER_ERROR);
        }
    }

    /**
     * 处理发送消息
     */
    private void handleSendMessage(Long userId, JSONObject messageData) {
        try {
            Long chatId = messageData.getLong("chatId");
            String content = messageData.getString("content");
            String messageType = messageData.getString("messageType");

            if (chatId == null || content == null) {
                log.warn("发送消息失败: 缺少必要参数");
                return;
            }

            // 创建消息对象
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setChatId(chatId);
            chatMessage.setSenderId(userId);
            chatMessage.setContent(content);
            chatMessage.setMessageType(messageType != null ? messageType : "text");

            // 保存消息到数据库
            ChatMessage savedMessage = chatService.sendMessage(chatMessage);
            if (savedMessage != null) {
                // 发送消息给接收方
                sendMessageToUser(getRecipientId(chatId, userId), savedMessage);
                
                log.info("消息发送成功: 消息ID={}, 会话ID={}, 发送人ID={}", 
                        savedMessage.getId(), chatId, userId);
            } else {
                log.warn("消息发送失败: 保存消息失败");
            }
        } catch (Exception e) {
            log.error("处理发送消息异常", e);
        }
    }

    /**
     * 处理消息已读
     */
    private void handleReadMessage(Long userId, JSONObject messageData) {
        try {
            Long chatId = messageData.getLong("chatId");
            if (chatId == null) {
                log.warn("标记消息已读失败: 缺少会话ID");
                return;
            }

            // 标记消息为已读
            boolean result = chatService.markMessagesAsRead(chatId, userId);
            if (result) {
                log.info("标记消息已读成功: 会话ID={}, 用户ID={}", chatId, userId);
            } else {
                log.warn("标记消息已读失败: 会话ID={}, 用户ID={}", chatId, userId);
            }
        } catch (Exception e) {
            log.error("处理标记已读异常", e);
        }
    }

    /**
     * 发送消息给指定用户
     */
    public static void sendMessageToUser(Long userId, Object message) {
        if (userId == null) {
            log.warn("发送消息失败: 目标用户ID为null");
            return;
        }

        WebSocketSession session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                String messageJson = JSON.toJSONString(message);
                session.sendMessage(new TextMessage(messageJson));
                log.info("消息发送到用户: 用户ID={}, 消息={}", userId, messageJson);
            } catch (Exception e) {
                log.error("发送消息给用户异常: 用户ID={}", userId, e);
                // 发送失败时移除会话
                userSessions.remove(userId);
            }
        } else {
            log.warn("发送消息失败: 用户未在线或会话已关闭: 用户ID={}", userId);
        }
    }

    /**
     * 获取接收方用户ID
     */
    private Long getRecipientId(Long chatId, Long senderId) {
        try {
            // 从数据库中查询会话信息
            com.campus.errand.entity.Chat chat = chatService.getChatById(chatId);
            if (chat != null) {
                // 判断当前用户是userId1还是userId2，返回另一个用户ID
                if (chat.getUserId1().equals(senderId)) {
                    return chat.getUserId2();
                } else if (chat.getUserId2().equals(senderId)) {
                    return chat.getUserId1();
                }
            }
            log.warn("获取接收方用户ID失败: 会话不存在或用户不在会话中");
        } catch (Exception e) {
            log.error("获取接收方用户ID异常", e);
        }
        return null;
    }

    /**
     * 获取用户在线状态
     */
    public static boolean isUserOnline(Long userId) {
        WebSocketSession session = userSessions.get(userId);
        return session != null && session.isOpen();
    }

    /**
     * 获取在线用户数量
     */
    public static int getOnlineUserCount() {
        return userSessions.size();
    }
}
