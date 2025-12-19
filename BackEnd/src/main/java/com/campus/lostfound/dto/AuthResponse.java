package com.campus.lostfound.dto;

import lombok.Data;

/**
 * 认证响应数据传输对象
 */
@Data
public class AuthResponse {
    
    /**
     * 用户信息
     */
    private UserInfo user;
    
    /**
     * JWT Token
     */
    private String token;

    @Data
    public static class UserInfo {
        private Long id;
        private String username;
        private String realName;
        private String role;
        private String email;
        private String phone;
        private String avatar;
        private String college;
        private String grade;
        private String createdAt;
    }
}