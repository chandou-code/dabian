package com.campus.errand.common.config;

import com.campus.errand.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * WebSocket握手拦截器
 */
@Slf4j
@Component
public class WebSocketInterceptor implements HandshakeInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, 
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        // 将ServerHttpRequest转换为HttpServletRequest
        if (request instanceof ServletServerHttpRequest) {
            HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
            
            // 从请求头或查询参数中获取token
            String token = servletRequest.getHeader("token");
            if (token == null || token.isEmpty()) {
                token = servletRequest.getParameter("token");
            }

            if (token != null && !token.isEmpty()) {
                try {
                    // 解析token获取用户ID
                    Long userId = jwtUtil.getUserIdFromToken(token);
                    if (userId != null) {
                        attributes.put("currentUserId", userId);
                        log.info("WebSocket握手认证成功: 用户ID={}", userId);
                        return true;
                    } else {
                        log.warn("WebSocket握手认证失败: token无效");
                        return false;
                    }
                } catch (Exception e) {
                    log.error("WebSocket握手认证异常", e);
                    return false;
                }
            } else {
                log.warn("WebSocket握手认证失败: 缺少token");
                return false;
            }
        }
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, 
                               WebSocketHandler wsHandler, Exception exception) {
        log.info("WebSocket握手完成");
    }
}
