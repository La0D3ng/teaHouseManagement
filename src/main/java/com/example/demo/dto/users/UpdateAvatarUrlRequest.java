package com.example.demo.dto.users;

import lombok.Data;

@Data
public class UpdateAvatarUrlRequest {
    private String username;
    private String newAvatarUrl;
}
