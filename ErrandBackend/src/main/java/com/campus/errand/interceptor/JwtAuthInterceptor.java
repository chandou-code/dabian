package com.campus.errand.interceptor;

import com.campus.errand.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JWT认证拦截器
 */
@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 处理OPTIONS预检请求
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        // 从请求头中获取Authorization
        String authorization = request.getHeader("Authorization");

        // 检查Authorization头是否存在且格式正确
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return sendErrorResponse(response, "未授权，请登录");
        }

        // 提取Token
        String token = authorization.substring(7);

        // 验证Token
        if (!jwtUtil.validateToken(token)) {
            return sendErrorResponse(response, "Token已过期或无效");
        }

        // 将用户信息存储到请求属性中
        request.setAttribute("currentUserId", jwtUtil.getUserIdFromToken(token));
        request.setAttribute("currentUsername", jwtUtil.getUsernameFromToken(token));
        request.setAttribute("currentRole", jwtUtil.getRoleFromToken(token));

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // 后续处理
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 完成处理
    }

    /**
     * 发送错误响应
     */
    private boolean sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json; charset=utf-8");
        response.getWriter().write("{\"code\": 401, \"msg\": \"" + message + "\", \"data\": null}");
        return false;
    }
}
