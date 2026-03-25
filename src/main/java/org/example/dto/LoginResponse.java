package org.example.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 登录响应 DTO
 */
@Getter
@Setter
public class LoginResponse {

    /**
     * Access Token
     */
    private String accessToken;

    /**
     * Refresh Token
     */
    private String refreshToken;

    /**
     * Token 类型
     */
    private String tokenType = "Bearer";

    /**
     * Access Token 有效期（秒）
     */
    private long expiresIn;

    /**
     * 用户信息
     */
    private UserInfo user;

    public LoginResponse() {
    }

    public LoginResponse(String accessToken, String refreshToken, long expiresIn, UserInfo user) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.user = user;
    }

    /**
     * 用户信息内部类
     */
    @Getter
    @Setter
    public static class UserInfo {
        private String username;
        private String userType;

        public UserInfo() {
        }

        public UserInfo(String username, String userType) {
            this.username = username;
            this.userType = userType;
        }
    }
}
