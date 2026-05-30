package com.example.dzcom.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JWT工具类
 */
@Slf4j
@Component
public class JwtUtil {
    
    @Value("${jwt.secret:YWlXZWFsdGhTZWNyZXRLZXkyMDI2QEludmVzdA==}")
    private String secret;
    
    @Value("${jwt.expiration:604800}") // 7天
    private Long expiration;
    
    /**
     * 获取签名密钥
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = java.util.Base64.getDecoder().decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
    /**
     * 生成Token
     */
    public String generateToken(String userId, String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration * 1000);
        
        return Jwts.builder()
            .subject(userId)
            .claim("username", username)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(getSigningKey())
            .compact();
    }
    
    /**
     * 解析Token
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
    
    /**
     * 验证Token是否有效
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            log.warn("Token验证失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 从Token获取用户ID
     */
    public String getUserIdFromToken(String token) {
        return parseToken(token).getSubject();
    }
    
    /**
     * 从Token获取用户名
     */
    public String getUsernameFromToken(String token) {
        return parseToken(token).get("username", String.class);
    }
}
