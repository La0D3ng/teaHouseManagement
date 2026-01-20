package com.example.demo.dto.users;

import lombok.Data;

@Data
public class UpdatePrivilegesRequest {
    private String username;
    private String role; // manager, staff, customer
    private Integer userStatus; // 0=禁用, 1=正常
}
