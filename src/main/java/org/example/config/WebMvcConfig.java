package org.example.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 * 配置跨域和静态资源
 * 
 * 注意：Spring Boot 3.x 默认已经配置了静态资源处理 (classpath:/static/)
 * 不需要手动添加 ResourceHandler，否则会导致与 Security 配置冲突
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);
    }

    // 删除 addResourceHandlers 方法，使用 Spring Boot 默认的静态资源配置
    // 避免与 Spring Security 的公开路径配置冲突
}
