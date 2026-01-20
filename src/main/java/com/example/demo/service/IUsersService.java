package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.entity.Users;

// 1.继承IService这个接口，<实体类名>
/*
    IService接口里面有mybatis-plus封装好我们经常会用到的增删改查的一些方法
    里面本质上还调用了上数据层mapper，是对mapper的封装优化
*/

public interface IUsersService extends IService<Users> {
    // 用户登录校验
    Users login(String loginWay, String account, String password);

    // 用户注册(需要邮箱验证码)
    boolean registerUser(Users user, String password, String verificationCode);

    // 修改密码(需要邮箱验证码)
    boolean changePassword(String username, String email, String oldPassword, String newPassword, String verificationCode);

    // 忘记密码(密码重置)
    boolean forgetPassword(String email, String verificationCode, String newPassword);
    
    // 发送验证码(通用接口)
    boolean sendVerificationCode(String email);
    
    // 发送验证码(用于注册)
    boolean sendVerificationCodeForRegister(String email);
    
    // 发送验证码(用于找回密码)
    boolean sendVerificationCodeForForgetPassword(String email);

    // 用户修改头像
    boolean updateAvatarUrl(String username, String newAvatarUrl);

    // 上传头像文件
    String uploadAvatar(String username, org.springframework.web.multipart.MultipartFile file) throws java.io.IOException;

    // 根据用户ID返回用户信息
    Users getUserInfo(Long userId);

    // 编辑用户权限
    boolean updateUserPrivileges(String username, String role, Integer userStatus);
}
