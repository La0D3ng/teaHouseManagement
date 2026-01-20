package com.example.demo.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.entity.Rooms;
import com.example.demo.entity.Users;
import com.example.demo.mapper.UsersMapper;
import com.example.demo.service.EmailSender;
import com.example.demo.service.IUsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static com.example.demo.service.EmailSender.sendEmail;

@Service
public class UsersServiceImpl extends ServiceImpl<UsersMapper, Users> implements IUsersService {
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
    // 头像存储路径，可以在application.yml中配置
    @Value("${file.avatar-path:uploads/avatars/}")
    private String avatarPath;

    // 用户登录校验 -- 支持输入用户名 / 邮箱 / 手机号 + 密码
    @Override
    public Users login(String loginWay, String account, String password) {
        // 输入不允许为空
        if (account == null || account.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            return null;
        }
        // 创建查询：
        QueryWrapper<Users> queryWrapper = new QueryWrapper<>();
        switch (loginWay) {
            case "username":
                queryWrapper.eq("username", account);
                break;
            case "email":
                queryWrapper.eq("email", account);
                break;
            case "phone":
                queryWrapper.eq("phone", account);
                break;
            default:
                break;
        }
        // 获取查询结果：
        Users user = this.getOne(queryWrapper);

        
        // 用户不存在
        if (user == null) {
            return null;
        }
        
        // 校验用户状态(0 = 禁用, 1 = 正常)
        if (user.getUserStatus() == 0) {
            throw new RuntimeException("账号已被禁用,请联系管理员");
        }
        
        // 校验密码
        boolean passwordMatch = passwordEncoder.matches(password, user.getPasswordHash());
        if (!passwordMatch) {
            return null;
        }

        // 手机号信息隐藏返回
        String hiddenPhone = user.getPhone().substring(0,4) + " **** *" +user.getPhone().substring(9, 11);
        user.setPhone(hiddenPhone);
        
        // 登录成功,返回用户信息,注意清空密码字段，此处在 PasswordHash 字段设置了 JsonIgnore,会自动忽略这个返回值
        return user;
    }

    // 用户注册(需要邮箱验证码)
    @Override
    public boolean registerUser(Users user, String password, String verificationCode) {
        // 1. 验证邮箱验证码
        if (!EmailSender.verifyCode(user.getEmail(), verificationCode)) {
            throw new RuntimeException("验证码错误或已过期,请重新获取验证码");
        }
        
        // 2. 检查用户名是否已存在
        QueryWrapper<Users> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", user.getUsername());
        if (this.getOne(queryWrapper) != null) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 3. 检查手机号是否已存在
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("phone", user.getPhone());
        if (this.getOne(queryWrapper) != null) {
            throw new RuntimeException("该手机号已被注册");
        }

        // 4. 检查邮箱是否已存在
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", user.getEmail());
        if (this.getOne(queryWrapper) != null) {
            throw new RuntimeException("该邮箱已被注册");
        }
        
        // 5. 密码加密
        String encodedPassword = passwordEncoder.encode(password);
        user.setPasswordHash(encodedPassword);

        user.setUserStatus(1); // 默认状态为正常
        user.setRole(Users.UserRole.customer); // 默认角色为客户

        // 6. 插入用户
        boolean result = this.save(user);
        
        // 7. 注册成功后清除验证码
        if (result) {
            EmailSender.removeCode(user.getEmail());
        }
        
        return result;
    }

    // 修改密码(需要邮箱验证码)
    @Override
    public boolean changePassword(String username, String email, String oldPassword, String newPassword, String verificationCode) {
        // 1. 验证邮箱验证码
        if (!EmailSender.verifyCode(email, verificationCode)) {
            throw new RuntimeException("验证码错误或已过期,请重新获取验证码");
        }
        
        // 2. 根据用户名查询用户
        QueryWrapper<Users> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        Users user = this.getOne(queryWrapper);
        
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 3. 验证邮箱是否匹配
        if (!user.getEmail().equals(email)) {
            throw new RuntimeException("邮箱与用户名不匹配");
        }
        
        // 4. 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new RuntimeException("原密码错误");
        }
        
        // 5. 加密新密码并更新
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPasswordHash(encodedPassword);
        
        boolean result = this.updateById(user);
        
        // 6. 密码修改成功后清除验证码
        if (result) {
            EmailSender.removeCode(email);
        }
        
        return result;
    }

    // 忘记密码 -- 密码重置（需要验证码验证）
    @Override
    public boolean forgetPassword(String email, String verificationCode, String newPassword) {
        // 根据邮箱查询用户
        QueryWrapper<Users> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", email);
        Users user = this.getOne(queryWrapper);

        if (user == null) {
            throw new RuntimeException("该邮箱未注册");
        }

        // 验证验证码是否正确
        if (!EmailSender.verifyCode(email, verificationCode)) {
            throw new RuntimeException("验证码错误或已过期");
        }

        // 加密新密码并更新
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPasswordHash(encodedPassword);
        
        boolean result = this.updateById(user);
        
        // 密码重置成功后，清除验证码
        if (result) {
            EmailSender.removeCode(email);
        }
        
        return result;
    }

    // 发送验证码到邮箱(用于找回密码,邮箱必须已注册)
    public boolean sendVerificationCodeForForgetPassword(String email) {
        // 检查邮箱是否已注册
        QueryWrapper<Users> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", email);
        Users user = this.getOne(queryWrapper);
        
        if (user == null) {
            throw new RuntimeException("该邮箱未注册");
        }
        
        // 发送验证码
        return EmailSender.sendVerificationCode(email);
    }
    
    // 发送验证码到邮箱(用于注册,邮箱不能已注册)
    public boolean sendVerificationCodeForRegister(String email) {
        // 检查邮箱是否已注册
        QueryWrapper<Users> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", email);
        Users user = this.getOne(queryWrapper);
        
        if (user != null) {
            throw new RuntimeException("该邮箱已被注册,请直接登录");
        }
        
        // 发送验证码
        return EmailSender.sendVerificationCode(email);
    }
    
    // 发送验证码到邮箱(统一接口,自动判断场景)
    public boolean sendVerificationCode(String email) {
        // 发送验证码(不检查是否注册,支持注册和找回密码两种场景)
        return EmailSender.sendVerificationCode(email);
    }

    // 用户修改头像
    @Override
    public boolean updateAvatarUrl(String username, String newAvatarUrl) {
        // 根据用户名查询用户
        QueryWrapper<Users> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        Users user = this.getOne(queryWrapper);

        // 检测 user 是否存在
        if (user == null) {
            throw new RuntimeException("该用户不存在");
        }

        // 更新用户头像
        user.setAvatarUrl(newAvatarUrl);

        return updateById(user);
    }

    // 根据用户ID返回用户信息
    @Override
    public Users getUserInfo(Long userId) {
        if (userId == null) {
            throw new RuntimeException("用户ID不能为空");
        }
        
        Users user = this.getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        // 手机号信息隐藏返回
        String hiddenPhone = user.getPhone().substring(0,4) + " **** *" +user.getPhone().substring(9, 11);
        user.setPhone(hiddenPhone);

        return user;
    }

    // 编辑用户权限
    @Override
    public boolean updateUserPrivileges(String username, String role, Integer userStatus) {
        // 根据用户名查询用户
        QueryWrapper<Users> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        Users user = this.getOne(queryWrapper);

        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 更新用户信息
        if (username != null && !username.trim().isEmpty()) {
            user.setUsername(username);
        }
        if (role != null && !role.trim().isEmpty()) {
            user.setRole(Users.UserRole.valueOf(role));
        }
        if (userStatus != null) {
            user.setUserStatus(userStatus);
        }
        
        return this.updateById(user);
    }

    // 上传头像文件
    @Override
    public String uploadAvatar(String username, MultipartFile file) throws IOException {
        // 验证用户是否存在
        QueryWrapper<Users> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        Users user = this.getOne(queryWrapper);
        
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 验证文件是否为空
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("请选择要上传的文件");
        }
        
        // 验证文件类型
        String originalFilename = file.getOriginalFilename();
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("只能上传图片文件");
        }
        
        // 获取文件扩展名并验证
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        }
        
        // 验证文件扩展名是否为允许的图片格式
        String[] allowedExtensions = {".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp"};
        boolean isValidExtension = false;
        for (String ext : allowedExtensions) {
            if (ext.equals(fileExtension)) {
                isValidExtension = true;
                break;
            }
        }
        if (!isValidExtension) {
            throw new RuntimeException("不支持的文件格式，仅支持: jpg, jpeg, png, gif, bmp, webp");
        }
        
        // 验证文件大小 (限制为5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new RuntimeException("文件大小不能超过5MB");
        }
        
        // 生成唯一文件名: username_timestamp_uuid.ext
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String newFilename = username + "_" + timestamp + "_" + uuid + fileExtension;
        
        // 创建存储目录
        Path uploadPath = Paths.get(avatarPath);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // 保存文件
        Path filePath = uploadPath.resolve(newFilename);
        file.transferTo(filePath.toFile());
        
        // 生成访问 URL (相对路径)
        String avatarUrl = "/avatars/" + newFilename;
        
        // 更新用户头像 URL
        user.setAvatarUrl(avatarUrl);
        this.updateById(user);
        
        return avatarUrl;
    }
}
