package com.campus.lostfound.util;

import com.campus.lostfound.common.constant.JwtConstant;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Date;

/**
 * JWT Token工具类
 */
@Slf4j
@Component
public class JwtTokenUtil {

    private static final Key SECRET_KEY = new SecretKeySpec(
        JwtConstant.JWT_SECRET.getBytes(), 
        SignatureAlgorithm.HS256.getJcaName()
    );

    /**
     * 从Token中获取用户ID
     */
    public static Long getUserIdFromToken(String token) {
        try {
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7); // 移除 "Bearer " 前缀
            }
            
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
            
            return Long.valueOf(claims.getSubject());
        } catch (Exception e) {
            log.error("解析Token获取用户ID失败", e);
            return null;
        }
    }

    /**
     * 从Token中获取用户名
     */
    public static String getUsernameFromToken(String token) {
        try {
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
            
            return claims.get("username", String.class);
        } catch (Exception e) {
            log.error("解析Token获取用户名失败", e);
            return null;
        }
    }

    /**
     * 从Token中获取用户角色
     */
    public static String getRoleFromToken(String token) {
        try {
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
            
            return claims.get("role", String.class);
        } catch (Exception e) {
            log.error("解析Token获取用户角色失败", e);
            return null;
        }
    }

    /**
     * 验证Token是否有效
     */
    public static boolean validateToken(String token) {
        try {
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
            
            // 检查Token是否过期
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            log.error("Token验证失败", e);
            return false;
        }
    }

    /**
     * 从Token中获取所有Claims
     */
    public static Claims getClaimsFromToken(String token) {
        try {
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
        } catch (Exception e) {
            log.error("解析Token获取Claims失败", e);
            return null;
        }
    }

    /**
     * 检查Token是否即将过期（30分钟内）
     */
    public static boolean isTokenExpiringSoon(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            if (claims == null) return true;
            
            Date expiration = claims.getExpiration();
            Date thirtyMinutesFromNow = new Date(System.currentTimeMillis() + 30 * 60 * 1000);
            
            return expiration.before(thirtyMinutesFromNow);
        } catch (Exception e) {
            log.error("检查Token过期时间失败", e);
            return true;
        }
    }

    /**
     * 获取Token过期时间
     */
    public static Date getExpirationFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims != null ? claims.getExpiration() : null;
        } catch (Exception e) {
            log.error("获取Token过期时间失败", e);
            return null;
        }
    }

    /**
     * 从Request Header中提取Token
     */
    public static String extractTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    /**
     * 获取用户信息Map
     */
    public static java.util.Map<String, Object> getUserInfoFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            if (claims == null) return null;
            
            java.util.Map<String, Object> userInfo = new java.util.HashMap<>();
            userInfo.put("userId", Long.valueOf(claims.getSubject()));
            userInfo.put("username", claims.get("username", String.class));
            userInfo.put("role", claims.get("role", String.class));
            userInfo.put("email", claims.get("email", String.class));
            userInfo.put("expiration", claims.getExpiration());
            
            return userInfo;
        } catch (Exception e) {
            log.error("获取用户信息失败", e);
            return null;
        }
    }
}