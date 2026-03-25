package org.example.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.config.JwtConfig;
import org.example.util.JwtTokenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

/**
 * JWT 认证过滤器
 * 
 * 功能：
 * 1. 从请求头提取 JWT Token
 * 2. 验证 Token 有效性
 * 3. 设置 Spring Security 上下文
 * 4. 放行公开路径
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtConfig jwtConfig;

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // 从请求头获取 JWT Token
            String jwt = getJwtFromRequest(request);

            // 如果 Token 存在且有效，设置认证信息
            if (StringUtils.hasText(jwt) && jwtTokenUtil.validateToken(jwt)) {
                // 从 Token 中获取用户名
                String username = jwtTokenUtil.getUsernameFromToken(jwt);

                // 加载用户详情
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // 创建认证令牌
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                    );

                // 设置认证详情
                authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // 设置 Security 上下文
                SecurityContextHolder.getContext().setAuthentication(authentication);

                logger.debug("设置 Security 上下文，用户：{}", username);
            }
        } catch (Exception ex) {
            logger.error("无法设置用户认证信息：{}", ex.getMessage());
        }

        // 继续执行过滤链
        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头提取 JWT Token
     * 
     * @param request HTTP 请求
     * @return JWT Token
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(jwtConfig.getHeader());

        if (StringUtils.hasText(bearerToken) && 
            bearerToken.startsWith(jwtConfig.getPrefixWithSpace())) {
            // 去掉 "Bearer " 前缀
            return bearerToken.substring(jwtConfig.getPrefixWithSpace().length());
        }

        return null;
    }
}
