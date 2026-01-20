package com.example.demo.dto.users;

import com.example.demo.entity.Users;
import lombok.Data;

/**
 * 登录响应对象
 * 包含用户信息和JWT Token
 */
@Data
public class LoginResponse {
    private Long userId;
    private String username;
    private String email;
    private String phone;
    private String role;
    private String avatarUrl;
    private Integer userStatus;
    private String token;  // JWT Token
    
    /**
     * 从Users实体创建LoginResponse
     */
    public static LoginResponse fromUser(Users user, String token) {
        LoginResponse response = new LoginResponse();
        response.setUserId(user.getUserId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setRole(user.getRole().name());
        response.setAvatarUrl(user.getAvatarUrl());
        response.setUserStatus(user.getUserStatus());
        response.setToken(token);
        return response;
    }
}
