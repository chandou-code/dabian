package com.campus.errand.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JWT工具类
 */
@Component
public class JwtUtil {

    private SecretKey secretKey;

    @Value("${app.jwt.expiration}")
    private long expiration;

    public JwtUtil() {
        // 生成安全的HS512密钥
        this.secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);
    }

    /**
     * 生成Token
     */
    public String generateToken(Long userId, String username, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("username", username)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    /**
     * 从Token中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return Long.parseLong(claims.getSubject());
    }

    /**
     * 从Token中获取用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("username", String.class);
    }

    /**
     * 从Token中获取角色
     */
    public String getRoleFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("role", String.class);
    }

    /**
     * 从Token中获取Claims
     */
    private Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 验证Token是否过期
     */
    public boolean isTokenExpired(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getExpiration().before(new Date());
    }

    /**
     * 验证Token
     */
    public boolean validateToken(String token) {
        try {
            getClaimsFromToken(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
}
