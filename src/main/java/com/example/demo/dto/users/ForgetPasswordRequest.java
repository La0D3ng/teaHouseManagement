package com.example.demo.dto.users;

import lombok.Data;

@Data
public class ForgetPasswordRequest {
    private String email;
    private String verificationCode;
    private String newPassword;
}
