package com.campus.lostfound.common.constant;

/**
 * JWT相关常量
 */
public class JwtConstant {
    
    /**
     * JWT密钥
     */
    public static final String JWT_SECRET = "campusLostFoundSecretKey2024_extended_for_jwt_compliance";
    
    /**
     * JWT过期时间（毫秒）- 24小时
     */
    public static final long JWT_EXPIRATION = 24 * 60 * 60 * 1000;
    
    /**
     * JWT刷新时间（毫秒）- 30分钟
     */
    public static final long JWT_REFRESH_EXPIRATION = 30 * 60 * 1000;
    
    /**
     * Token前缀
     */
    public static final String TOKEN_PREFIX = "Bearer ";
    
    /**
     * Token请求头名称
     */
    public static final String TOKEN_HEADER = "Authorization";
    
    /**
     * 用户ID键
     */
    public static final String USER_ID_KEY = "userId";
    
    /**
     * 用户名键
     */
    public static final String USERNAME_KEY = "username";
    
    /**
     * 角色键
     */
    public static final String ROLE_KEY = "role";
    
    /**
     * 邮箱键
     */
    public static final String EMAIL_KEY = "email";
}