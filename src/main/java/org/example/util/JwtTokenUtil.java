package org.example.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.SignatureAlgorithm;
import org.example.config.JwtConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JWT Token 工具类
 * 
 * 功能：
 * 1. 生成 Access Token 和 Refresh Token
 * 2. 验证 Token 有效性
 * 3. 解析 Token 获取用户信息
 * 4. Token 黑名单机制（用于登出）
 */
@Component
public class JwtTokenUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenUtil.class);

    @Autowired
    private JwtConfig jwtConfig;

    // Token 黑名单（用于登出功能）
    private final Set<String> tokenBlacklist = ConcurrentHashMap.newKeySet();

    // 静态密钥（固定值，确保 HS512 需要 512 位 = 64 字节）
    private static final String STATIC_SECRET = "ThisIsAStaticSecretKeyForJWTThatIsExactly64BytesLongAndSecureEnoughForHS512Algorithm!";

    /**
     * 获取签名密钥
     * 使用静态密钥，确保重启后 Token 仍然有效
     */
    private Key getSigningKey() {
        // 使用静态密钥的字节（64 字节 = 512 位，符合 HS512 要求）
        byte[] keyBytes = STATIC_SECRET.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成 Access Token（有效期较短，默认 15 分钟）
     * 
     * @param username 用户名
     * @return JWT Token
     */
    public String generateAccessToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtConfig.getExpiration() * 1000);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .claim("type", "access")
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * 生成 Refresh Token（有效期较长，默认 7 天）
     * 
     * @param username 用户名
     * @return JWT Token
     */
    public String generateRefreshToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtConfig.getRefreshExpiration() * 1000);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .claim("type", "refresh")
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * 从 Token 中获取用户名
     * 
     * @param token JWT Token
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return claims.getSubject();
        } catch (Exception e) {
            logger.error("解析 Token 失败：{}", e.getMessage());
        }
        return null;
    }

    /**
     * 获取 Token 类型（access 或 refresh）
     * 
     * @param token JWT Token
     * @return Token 类型
     */
    public String getTokenType(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return claims.get("type", String.class);
        } catch (Exception e) {
            logger.error("解析 Token 类型失败：{}", e.getMessage());
        }
        return null;
    }

    /**
     * 验证 Token 是否有效
     * 
     * @param token JWT Token
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        // 检查 Token 是否在黑名单中
        if (tokenBlacklist.contains(token)) {
            logger.info("Token 已在黑名单中");
            return false;
        }

        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (SecurityException ex) {
            logger.error("Invalid JWT signature: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            logger.error("Invalid JWT token: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            logger.error("Expired JWT token: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            logger.error("Unsupported JWT token: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string is empty: {}", ex.getMessage());
        }
        return false;
    }

    /**
     * 检查 Token 是否过期
     * 
     * @param token JWT Token
     * @return 是否过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 将 Token 加入黑名单（用于登出）
     * 
     * @param token JWT Token
     */
    public void blacklistToken(String token) {
        if (token != null) {
            tokenBlacklist.add(token);
            logger.info("Token 已加入黑名单");
        }
    }

    /**
     * 检查 Token 是否在黑名单中
     * 
     * @param token JWT Token
     * @return 是否在黑名单中
     */
    public boolean isTokenBlacklisted(String token) {
        return tokenBlacklist.contains(token);
    }

    /**
     * 从 Refresh Token 生成新的 Access Token
     * 
     * @param refreshToken Refresh Token
     * @return 新的 Access Token，如果 Refresh Token 无效则返回 null
     */
    public String refreshAccessToken(String refreshToken) {
        if (validateToken(refreshToken) && "refresh".equals(getTokenType(refreshToken))) {
            String username = getUsernameFromToken(refreshToken);
            return generateAccessToken(username);
        }
        return null;
    }

    /**
     * 获取 Access Token 的剩余有效期（秒）
     * 
     * @param token JWT Token
     * @return 剩余秒数
     */
    public long getRemainingValiditySeconds(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            Date expiration = claims.getExpiration();
            long remaining = (expiration.getTime() - System.currentTimeMillis()) / 1000;
            return Math.max(0, remaining);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 获取配置的 Access Token 有效期（秒）
     * 
     * @return 有效期（秒）
     */
    public long getExpiration() {
        return jwtConfig.getExpiration();
    }
}
