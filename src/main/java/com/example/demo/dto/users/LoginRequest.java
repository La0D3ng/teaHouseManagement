package com.example.demo.dto.users;

import lombok.Data;

/**
 * 登录请求 DTO
 */
@Data
public class LoginRequest {
    private String loginWay;
    private String account;
    private String password;
}
