package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * CORS 跨域配置
 * 允许前端 Vue 应用跨域访问后端 API
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        
        // 允许的前端域名（开发环境）
        config.addAllowedOriginPattern("http://localhost:*");  // 允许所有 localhost 端口
        config.addAllowedOriginPattern("http://127.0.0.1:*");  // 允许 127.0.0.1

        // 允许的前端域名（生产环境）
        config.addAllowedOriginPattern("http://123.56.66.83:*");  // 云服务器HTTP
        config.addAllowedOriginPattern("https://123.56.66.83:*");  // 云服务器HTTPS
        config.addAllowedOriginPattern("http://123.56.66.83");  // 无端口号
        config.addAllowedOriginPattern("https://123.56.66.83");  // HTTPS无端口号
        
        // 允许所有请求头
        config.addAllowedHeader("*");
        
        // 允许所有 HTTP 方法
        config.addAllowedMethod("*");
        
        // 允许携带凭证（如 Cookie、Authorization header）
        config.setAllowCredentials(true);
        
        // 预检请求的有效期（秒），减少预检请求次数
        config.setMaxAge(3600L);
        
        // 暴露的响应头，前端可以访问这些头信息
        config.addExposedHeader("Authorization");
        config.addExposedHeader("Content-Type");
        
        // 应用配置到所有路径
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        return new CorsFilter(source);
    }
}
