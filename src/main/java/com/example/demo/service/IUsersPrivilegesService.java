package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.entity.UsersPrivileges;

import java.util.List;

public interface IUsersPrivilegesService extends IService<UsersPrivileges> {
    // 根据 user_id 返回用户权限信息
    UsersPrivileges getUserPrivilegesInfo(Long userId);
    
    // 获取所有用户权限信息
    List<UsersPrivileges> getAllUsersPrivileges();
}
