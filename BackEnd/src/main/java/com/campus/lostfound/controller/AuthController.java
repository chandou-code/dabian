package com.campus.lostfound.controller;

import com.campus.lostfound.common.Result;
import com.campus.lostfound.dto.AuthResponse;
import com.campus.lostfound.entity.User;
import com.campus.lostfound.service.UserService;
import com.campus.lostfound.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@CrossOrigin
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @PostMapping("/login")
    public Result<AuthResponse> login(@RequestBody Map<String, String> loginData) {
        try {
            String username = loginData.get("username");
            String password = loginData.get("password");
            String clientIp = getClientIpAddress(null);
            
            User user = userService.login(username, password, clientIp);
            
            if (user != null) {
                // 生成JWT Token
                String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
                
                // 构建响应数据
                AuthResponse response = new AuthResponse();
                AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo();
                
                userInfo.setId(user.getId());
                userInfo.setUsername(user.getUsername());
                userInfo.setRealName(user.getRealName());
                userInfo.setRole(user.getRole());
                userInfo.setEmail(user.getEmail());
                userInfo.setPhone(user.getPhone());
                userInfo.setAvatar(user.getAvatar());
                userInfo.setCollege(user.getCollege());
                userInfo.setGrade(user.getGrade());
                
                if (user.getCreatedAt() != null) {
                    userInfo.setCreatedAt(user.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                }
                
                response.setUser(userInfo);
                response.setToken(token);
                
                return Result.success("登录成功", response);
            } else {
                return Result.error("用户名或密码错误");
            }
        } catch (Exception e) {
            log.error("登录失败", e);
            return Result.error("登录失败：" + e.getMessage());
        }
    }
    
    @PostMapping("/register")
    public Result<AuthResponse> register(@RequestBody Map<String, String> registerData) {
        try {
            User user = new User();
            user.setUsername(registerData.get("username"));
            user.setPassword(registerData.get("password"));
            user.setEmail(registerData.get("email"));
            user.setPhone(registerData.get("phone"));
            user.setRealName(registerData.get("realName"));
            user.setRole("user");
            user.setStatus(1);
            
            User registeredUser = userService.register(user);
            
            if (registeredUser != null) {
                // 生成JWT Token
                String token = jwtUtil.generateToken(registeredUser.getId(), registeredUser.getUsername(), registeredUser.getRole());
                
                // 构建响应数据
                AuthResponse response = new AuthResponse();
                AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo();
                
                userInfo.setId(registeredUser.getId());
                userInfo.setUsername(registeredUser.getUsername());
                userInfo.setRealName(registeredUser.getRealName());
                userInfo.setRole(registeredUser.getRole());
                userInfo.setEmail(registeredUser.getEmail());
                userInfo.setPhone(registeredUser.getPhone());
                userInfo.setAvatar(registeredUser.getAvatar());
                userInfo.setCollege(registeredUser.getCollege());
                userInfo.setGrade(registeredUser.getGrade());
                
                if (registeredUser.getCreatedAt() != null) {
                    userInfo.setCreatedAt(registeredUser.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                }
                
                response.setUser(userInfo);
                response.setToken(token);
                
                return Result.success("注册成功", response);
            } else {
                return Result.error("注册失败，用户名已存在");
            }
        } catch (Exception e) {
            log.error("注册失败", e);
            return Result.error("注册失败：" + e.getMessage());
        }
    }
    
    @PostMapping("/logout")
    public Result logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        try {
            // 这里可以实现Token黑名单机制
            // 目前简单返回成功，前端删除本地Token即可
            return Result.success("登出成功", null);
        } catch (Exception e) {
            log.error("登出失败", e);
            return Result.error("登出失败：" + e.getMessage());
        }
    }
    
    @GetMapping("/check-username/{username}")
    public Result<Boolean> checkUsername(@PathVariable String username) {
        boolean exists = userService.checkUsernameExists(username);
        return Result.success(exists);
    }
    
    @GetMapping("/check-email/{email}")
    public Result<Boolean> checkEmail(@PathVariable String email) {
        boolean exists = userService.checkEmailExists(email);
        return Result.success(exists);
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        if (request == null) {
            return "127.0.0.1";
        }
        
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}