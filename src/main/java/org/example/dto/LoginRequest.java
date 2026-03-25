package org.example.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 登录请求 DTO
 */
@Getter
@Setter
public class LoginRequest {

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    public LoginRequest() {
    }

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
