package org.example.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * JWT 配置类
 */
@Component
public class JwtConfig {

    /**
     * JWT 密钥（Base64 编码，必须至少 512 位 = 64 字节）
     * 使用 Base64 编码的 128 字节随机字符串，确保 HS512 安全
     */
    @Value("${security.jwt.secret:c2VjdXJlU2VjcmV0S2V5Rm9ySldUU2lnbmluZ1dpdGhIUzUxMkFsZ29yaXRobVJlcXVpcmVzQXRMZWFzdDUxMkJpdHNLZXlTaXplVGhpc0lzQVN1cGVyTG9uZ0Jhc2U2NEVuY29kZWRTdHJpbmdGb3JKV1RTZWN1cml0eQ==}")
    private String secret;

    /**
     * Access Token 有效期（秒）默认 15 分钟
     */
    @Value("${security.jwt.expiration:900}")
    private Long expiration;

    /**
     * Refresh Token 有效期（秒）默认 7 天
     */
    @Value("${security.jwt.refresh-expiration:604800}")
    private Long refreshExpiration;

    /**
     * JWT Header 名称
     */
    @Value("${security.jwt.header:Authorization}")
    private String header;

    /**
     * JWT Token 前缀
     */
    @Value("${security.jwt.prefix:Bearer }")
    private String prefix;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public Long getExpiration() {
        return expiration;
    }

    public void setExpiration(Long expiration) {
        this.expiration = expiration;
    }

    public Long getRefreshExpiration() {
        return refreshExpiration;
    }

    public void setRefreshExpiration(Long refreshExpiration) {
        this.refreshExpiration = refreshExpiration;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * 获取完整的 Token 前缀（包含空格）
     */
    public String getPrefixWithSpace() {
        return prefix.endsWith(" ") ? prefix : prefix + " ";
    }
}
