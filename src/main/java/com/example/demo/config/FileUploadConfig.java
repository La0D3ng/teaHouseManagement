package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 文件上传配置类
 * 用于配置静态资源访问路径
 */
@Configuration
public class FileUploadConfig implements WebMvcConfigurer {
    
    @Value("${file.avatar-path:uploads/avatars/}")
    private String avatarPath;
    
    @Value("${file.room-image-path:uploads/rooms/}")
    private String roomImagePath;
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置头像访问路径映射
        // 访问 /avatars/** 时会映射到本地的 uploads/avatars/ 目录
        registry.addResourceHandler("/avatars/**")
                .addResourceLocations("file:" + avatarPath);
        
        // 配置包间图片访问路径映射
        // 访问 /rooms/images/** 时会映射到本地的 uploads/rooms/ 目录
        registry.addResourceHandler("/rooms/images/**")
                .addResourceLocations("file:" + roomImagePath);
    }
}
