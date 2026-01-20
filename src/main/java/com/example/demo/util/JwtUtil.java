package com.example.demo.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 * 用于生成和验证JWT Token
 */
// 自动装配依赖
@Component
@Data
@Configuration
@ConfigurationProperties(prefix = "encoder.crypt")
public class JwtUtil {
    
    // 密钥 -- uuid
    private String secret;
    
    // Token有效期：7天，过期需要重新登录
    private static final long EXPIRATION_TIME = 7 * 24 * 60 * 60 * 1000;
    
    // 获取签名密钥
    private Key getSigningKey() {
        // 生成 HMAC-SHA 密钥，用于签名和验证 Token
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
    
    /**
     * 生成JWT Token
     * @param userId 用户ID
     * @param username 用户名
     * @param role 用户角色
     * @return JWT Token字符串
     */
    public String generateToken(Long userId, String username, String role) {
        // 存储 JWT 声明(用户数据)
        Map<String, Object> claims = new HashMap<>();
        // 添加声明
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("role", role);
        // 构建 JWT - 设置声明、主题、签发时间、过期时间、签名使用 HMAC-SHA、compact 生成最终的字符串
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    /**
     * 从Token中解析Claims -- 用于验证用户权限
     * @param token JWT Token
     * @return Claims对象
     */
    public Claims parseToken(String token) {
        try {
            // parseClaimsJws 解析 token 并签名验证，getBody 提取声明部分
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 从Token中获取用户ID
     * @param token JWT Token
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        // 解析 token 获取声明
        Claims claims = parseToken(token);
        if (claims != null) {
            // 获取 userId 字段，并转换为 Long 类型
            return claims.get("userId", Long.class);
        }
        return null;
    }
    
    /**
     * 从Token中获取用户名
     * @param token JWT Token
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims != null) {
            return claims.get("username", String.class);
        }
        return null;
    }
    
    /**
     * 从Token中获取用户角色类型
     * @param token JWT Token
     * @return 角色
     */
    public String getRoleFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims != null) {
            return claims.get("role", String.class);
        }
        return null;
    }
    
    /**
     * 验证Token是否有效
     * @param token JWT Token
     * @return true=有效, false=无效
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = parseToken(token);
            if (claims == null) {
                return false;
            }
            
            // 检查是否过期
            Date expiration = claims.getExpiration();
            return expiration.after(new Date());
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 从请求头中提取Token
     * Authorization: Bearer <token>
     * @param authHeader 授权头
     * @return Token字符串
     */
    public String extractTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
