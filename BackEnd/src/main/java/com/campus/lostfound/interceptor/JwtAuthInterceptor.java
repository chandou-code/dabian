package com.campus.lostfound.interceptor;

import com.campus.lostfound.common.constant.JwtConstant;
import com.campus.lostfound.common.Result;
import com.campus.lostfound.util.JwtTokenUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

/**
 * JWT认证拦截器
 */
@Slf4j
@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

    private static final String[] AUTH_WHITELIST = {
        "/api/auth/login",
        "/api/auth/register", 
        "/api/test/**",
        "/error",
        "/actuator/**",
        "/api/items/lost-items",
        "/api/items/found-items", 
        "/api/items/search",
        "/api/items/lost-item/**",
        "/api/items/found-item/**"
    };

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        
        // 处理跨域预检请求
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }
        
        String requestURI = request.getRequestURI();
        
        // 白名单路径直接放行
        for (String whitelistPath : AUTH_WHITELIST) {
            if (matchesPath(requestURI, whitelistPath)) {
                return true;
            }
        }
        
        // 获取Authorization头
        String authHeader = request.getHeader(JwtConstant.TOKEN_HEADER);
        
        if (authHeader == null || !authHeader.startsWith(JwtConstant.TOKEN_PREFIX)) {
            log.warn("未提供有效的Token，请求路径: {}", requestURI);
            sendErrorResponse(response, 401, "未授权，请先登录");
            return false;
        }
        
        // 提取Token
        String token = JwtTokenUtil.extractTokenFromHeader(authHeader);
        
        if (token == null) {
            log.warn("Token提取失败，请求路径: {}", requestURI);
            sendErrorResponse(response, 401, "Token格式错误");
            return false;
        }
        
        // 验证Token
        if (!JwtTokenUtil.validateToken(token)) {
            log.warn("Token验证失败，请求路径: {}", requestURI);
            sendErrorResponse(response, 401, "Token已过期或无效");
            return false;
        }
        
        // 将用户信息存入请求属性
        Long userId = JwtTokenUtil.getUserIdFromToken(authHeader);
        String username = JwtTokenUtil.getUsernameFromToken(authHeader);
        String role = JwtTokenUtil.getRoleFromToken(authHeader);
        
        if (userId != null) {
            request.setAttribute("currentUserId", userId);
            request.setAttribute("currentUsername", username);
            request.setAttribute("currentRole", role);
            request.setAttribute("authToken", authHeader);
        }
        
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 清理请求属性
        request.removeAttribute("currentUserId");
        request.removeAttribute("currentUsername");
        request.removeAttribute("currentRole");
        request.removeAttribute("authToken");
    }

    /**
     * 发送错误响应
     */
    private void sendErrorResponse(HttpServletResponse response, int statusCode, String message) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(statusCode);
        
        Result<?> result = Result.error(message);
        ObjectMapper objectMapper = new ObjectMapper();
        
        try (PrintWriter writer = response.getWriter()) {
            writer.write(objectMapper.writeValueAsString(result));
            writer.flush();
        }
    }

    /**
     * 路径匹配检查
     */
    private boolean matchesPath(String requestURI, String pattern) {
        if (pattern.endsWith("/**")) {
            String prefix = pattern.substring(0, pattern.length() - 3);
            return requestURI.startsWith(prefix);
        }
        return requestURI.equals(pattern);
    }

    /**
     * 检查用户是否有管理员权限
     */
    public static boolean hasAdminRole(HttpServletRequest request) {
        String role = (String) request.getAttribute("currentRole");
        return "admin".equals(role);
    }

    /**
     * 检查用户是否有审核员权限
     */
    public static boolean hasReviewerRole(HttpServletRequest request) {
        String role = (String) request.getAttribute("currentRole");
        return "admin".equals(role) || "reviewer".equals(role);
    }

    /**
     * 获取当前用户ID
     */
    public static Long getCurrentUserId(HttpServletRequest request) {
        Object userId = request.getAttribute("currentUserId");
        return userId != null ? (Long) userId : null;
    }

    /**
     * 获取当前用户名
     */
    public static String getCurrentUsername(HttpServletRequest request) {
        Object username = request.getAttribute("currentUsername");
        return username != null ? (String) username : null;
    }

    /**
     * 获取当前用户角色
     */
    public static String getCurrentRole(HttpServletRequest request) {
        Object role = request.getAttribute("currentRole");
        return role != null ? (String) role : null;
    }
}