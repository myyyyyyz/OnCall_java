package org.example.config;

import org.example.filter.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security 配置类
 *
 * 功能：
 * 1. 配置公开路径，避免 302 重定向到登录页
 * 2. 配置 CORS 跨域支持
 * 3. 配置 JWT 认证过滤器
 * 4. 禁用 CSRF（使用 JWT 不需要 CSRF）
 * 5. 配置无状态会话管理
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    /**
     * 不需要鉴权的公开路径
     */
    private static final String[] PUBLIC_PATHS = {
        "/",
        "/index.html",
        "/login.html",
        "/styles.css",
        "/app.js",
        "/static/**",
        "/css/**",
        "/js/**",
        "/images/**",
        "/api/login",               // 登录接口（公开）
        "/api/register",            // 注册接口（公开）
        "/api/upload",              // 文件上传（公开，make init 需要）
        "/milvus/health",           // Milvus 健康检查
        "/actuator/**",             // Spring Actuator 监控
        "/error"                    // 错误页面
    };

    /**
     * 配置安全过滤链
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
            // 禁用 CSRF（使用 JWT 不需要 CSRF 保护）
            .csrf(AbstractHttpConfigurer::disable)

            // 配置 CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // 配置请求授权
            .authorizeHttpRequests(auth -> auth
                // 公开路径，允许匿名访问
                .requestMatchers(PUBLIC_PATHS).permitAll()
                // 其他所有请求需要认证
                .anyRequest().authenticated()
            )

            // 配置无状态会话管理（使用 JWT 不需要 Session）
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // 配置异常处理
            .exceptionHandling(exception -> exception
                // 认证失败处理（未登录或 Token 无效）
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setContentType("application/json;charset=UTF-8");
                    response.setStatus(401);
                    response.getWriter().write("{\"code\":401,\"message\":\"未认证或 Token 无效\"}");
                })
                // 授权失败处理（无权限访问）
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setContentType("application/json;charset=UTF-8");
                    response.setStatus(403);
                    response.getWriter().write("{\"code\":403,\"message\":\"无权限访问\"}");
                })
            )

            // 添加 JWT 认证过滤器（在 UsernamePasswordAuthenticationFilter 之前执行）
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 配置 AuthenticationManager（用于登录认证）
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * 配置密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 配置 CORS 跨域
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 允许所有源（生产环境建议指定具体域名）
        configuration.setAllowedOrigins(List.of("*"));
        
        // 允许所有请求头
        configuration.setAllowedHeaders(List.of("*"));
        
        // 允许所有请求方法
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        
        // 允许携带凭证
        configuration.setAllowCredentials(true);
        
        // 预检请求缓存时间（秒）
        configuration.setMaxAge(3600L);
        
        // 暴露响应头（用于前端获取新 Token）
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization",
            "X-New-Access-Token"
        ));

        // 应用到所有路径
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}
