package com.example.demo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.entity.UsersPrivileges;
import com.example.demo.mapper.UserPrivilegesMapper;
import com.example.demo.service.IUsersPrivilegesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsersPrivilegesImpl extends ServiceImpl<UserPrivilegesMapper, UsersPrivileges> implements IUsersPrivilegesService {
    @Autowired
    private UserPrivilegesMapper userPrivilegesMapper;
    // 根据 userId 返回用户权限信息
    @Override
    public UsersPrivileges getUserPrivilegesInfo(Long userId) {
        if (userId == null) {
            throw new RuntimeException("用户ID不能为空");
        }
        UsersPrivileges usersPrivileges = this.getById(userId);
        if (usersPrivileges == null) {
            throw new RuntimeException("用户不存在");
        }

        return usersPrivileges;
    }
    
    // 获取所有用户权限信息
    @Override
    public List<UsersPrivileges> getAllUsersPrivileges() {
        return userPrivilegesMapper.getAllUsersPrivileges();
    }
}
