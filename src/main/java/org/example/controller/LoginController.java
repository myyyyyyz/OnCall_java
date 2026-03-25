package org.example.controller;

import org.example.dto.LoginRequest;
import org.example.dto.LoginResponse;
import org.example.dto.User;
import org.example.service.CustomUserDetailsService;
import org.example.service.UserService;
import org.example.util.JwtTokenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 登录控制器
 * 
 * 提供用户登录、登出、注册和用户信息查询接口
 */
@RestController
@RequestMapping("/api")
public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 用户登录
     * 
     * @param loginRequest 登录请求（用户名 + 密码）
     * @return 登录响应（包含 Token 和用户信息）
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest loginRequest) {
        try {
            logger.info("用户登录请求：{}", loginRequest.getUsername());

            // 验证用户名和密码
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );

            // 设置认证信息
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 生成 Token
            String accessToken = jwtTokenUtil.generateAccessToken(loginRequest.getUsername());
            String refreshToken = jwtTokenUtil.generateRefreshToken(loginRequest.getUsername());

            // 获取用户信息
            User user = userService.getUserByUsername(loginRequest.getUsername());
            LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(
                user.getUsername(),
                user.getUserType()
            );

            // 构建响应
            LoginResponse response = new LoginResponse(
                accessToken,
                refreshToken,
                jwtTokenUtil.getExpiration(),
                userInfo
            );

            logger.info("用户登录成功：{}", loginRequest.getUsername());
            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (BadCredentialsException e) {
            logger.warn("登录失败 - 用户名或密码错误：{}", loginRequest.getUsername());
            return ResponseEntity.ok(ApiResponse.error("用户名或密码错误"));
        } catch (Exception e) {
            logger.error("登录失败", e);
            return ResponseEntity.ok(ApiResponse.error("登录失败：" + e.getMessage()));
        }
    }

    /**
     * 用户登出
     * 
     * @param request HTTP 请求（从 Header 获取 Token）
     * @return 登出响应
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        try {
            // 从 Authorization Header 提取 Token
            String token = extractTokenFromHeader(authorization);

            if (token != null) {
                // 将 Token 加入黑名单
                jwtTokenUtil.blacklistToken(token);
                logger.info("用户登出成功，Token 已加入黑名单");
            }

            // 清除 Security 上下文
            SecurityContextHolder.clearContext();

            return ResponseEntity.ok(ApiResponse.success("登出成功"));

        } catch (Exception e) {
            logger.error("登出失败", e);
            return ResponseEntity.ok(ApiResponse.error("登出失败：" + e.getMessage()));
        }
    }

    /**
     * 获取当前登录用户信息
     * 
     * @return 用户信息
     */
    @GetMapping("/userinfo")
    public ResponseEntity<ApiResponse<Map<String, String>>> getUserInfo() {
        try {
            // 从 Security 上下文获取当前用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated() || 
                "anonymousUser".equals(authentication.getPrincipal())) {
                return ResponseEntity.ok(ApiResponse.error("未登录"));
            }

            String username = authentication.getName();
            User user = userService.getUserByUsername(username);

            if (user == null) {
                return ResponseEntity.ok(ApiResponse.error("用户不存在"));
            }

            Map<String, String> userInfo = new HashMap<>();
            userInfo.put("username", user.getUsername());
            userInfo.put("userType", user.getUserType());
            userInfo.put("phone", user.getPhone() != null ? user.getPhone() : "");

            return ResponseEntity.ok(ApiResponse.success(userInfo));

        } catch (Exception e) {
            logger.error("获取用户信息失败", e);
            return ResponseEntity.ok(ApiResponse.error("获取用户信息失败：" + e.getMessage()));
        }
    }

    /**
     * 用户注册
     * 
     * @param registerRequest 注册请求（用户名 + 密码 + 手机号）
     * @return 注册响应
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@RequestBody RegisterRequest registerRequest) {
        try {
            logger.info("用户注册请求：{}", registerRequest.getUsername());

            // 检查用户名是否已存在
            User existingUser = userService.getUserByUsername(registerRequest.getUsername());
            if (existingUser != null) {
                return ResponseEntity.ok(ApiResponse.error("用户名已存在"));
            }

            // 创建新用户
            User user = new User();
            user.setUsername(registerRequest.getUsername());
            user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
            user.setPhone(registerRequest.getPhone());
            user.setUserType("user"); // 默认普通用户

            userService.createUser(user);

            logger.info("用户注册成功：{}", registerRequest.getUsername());
            return ResponseEntity.ok(ApiResponse.success("注册成功"));

        } catch (Exception e) {
            logger.error("注册失败", e);
            return ResponseEntity.ok(ApiResponse.error("注册失败：" + e.getMessage()));
        }
    }

    /**
     * 刷新 Token
     * 
     * @param refreshToken Refresh Token
     * @return 新的 Access Token
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Map<String, String>>> refreshToken(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        try {
            String token = extractTokenFromHeader(authorization);

            if (token == null || !jwtTokenUtil.validateToken(token)) {
                return ResponseEntity.ok(ApiResponse.error("无效的 Refresh Token"));
            }

            String newAccessToken = jwtTokenUtil.refreshAccessToken(token);
            if (newAccessToken == null) {
                return ResponseEntity.ok(ApiResponse.error("刷新 Token 失败"));
            }

            Map<String, String> response = new HashMap<>();
            response.put("accessToken", newAccessToken);
            response.put("tokenType", "Bearer");
            response.put("expiresIn", String.valueOf(jwtTokenUtil.getExpiration()));

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (Exception e) {
            logger.error("刷新 Token 失败", e);
            return ResponseEntity.ok(ApiResponse.error("刷新 Token 失败：" + e.getMessage()));
        }
    }

    /**
     * 从 Authorization Header 提取 Token
     */
    private String extractTokenFromHeader(String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return null;
    }

    /**
     * 注册请求 DTO
     */
    @lombok.Getter
    @lombok.Setter
    public static class RegisterRequest {
        private String username;
        private String password;
        private String phone;
    }

    /**
     * 统一 API 响应格式
     */
    @lombok.Getter
    @lombok.Setter
    public static class ApiResponse<T> {
        private int code;
        private String message;
        private T data;

        public static <T> ApiResponse<T> success(T data) {
            ApiResponse<T> response = new ApiResponse<>();
            response.setCode(200);
            response.setMessage("success");
            response.setData(data);
            return response;
        }

        public static <T> ApiResponse<T> error(String message) {
            ApiResponse<T> response = new ApiResponse<>();
            response.setCode(500);
            response.setMessage(message);
            return response;
        }
    }
}
