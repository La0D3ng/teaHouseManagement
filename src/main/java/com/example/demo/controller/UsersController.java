package com.example.demo.controller;

import com.example.demo.dto.Result;
import com.example.demo.dto.users.*;
import com.example.demo.entity.Users;
import com.example.demo.service.IUsersService;
import com.example.demo.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Tag(name = "用户管理", description = "用户相关接口，包括登录、注册、密码管理、用户信息管理")
@RestController
@RequestMapping("/users")
public class UsersController {
    @Autowired  // 自动注解
    private IUsersService usersService;  // 定义调用服务层的接口
    
    @Autowired
    private JwtUtil jwtUtil;  // JWT工具类
    
    @Operation(summary = "用户登录", description = "支持用户名/邮箱/手机号登录，登录成功返回JWT Token")
    @ApiResponse(responseCode = "200", description = "登录成功", content = @Content(schema = @Schema(implementation = LoginResponse.class)))
    @ApiResponse(responseCode = "401", description = "账号或密码错误")
    @ApiResponse(responseCode = "403", description = "账号已被禁用")
    @PostMapping("/login")
    public Result<LoginResponse> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "登录信息", required = true)
            @RequestBody LoginRequest loginRequest) {
        try {
            // 调用 Service 层的登录方法
            Users user = usersService.login(loginRequest.getLoginWay(), loginRequest.getAccount(), loginRequest.getPassword());
            
            if (user != null) {
                // 生成JWT Token
                String token = jwtUtil.generateToken(
                    user.getUserId(),
                    user.getUsername(),
                    user.getRole().name()
                );
                
                // 创建登录响应对象
                LoginResponse loginResponse = LoginResponse.fromUser(user, token);
                
                // 登录成功
                return Result.success("登录成功", loginResponse);
            } else {
                // 登录失败(用户名或密码错误)
                return Result.error(401, "账号或密码错误");
            }
        } catch (RuntimeException e) {
            // 捕获其他异常，比如账号被禁用
            return Result.error(403, e.getMessage());
        } catch (Exception e) {
            // 捕获其他异常
            return Result.error(500, "登录失败,请稍后重试");
        }
    }
    
    @Operation(summary = "用户注册", description = "用户注册需要先发送验证码到邮箱,然后填写验证码完成注册。仅客户(customer)可注册，管理员和员工账号需预先创建")
    @ApiResponse(responseCode = "200", description = "注册成功")
    @ApiResponse(responseCode = "400", description = "注册失败，用户名/邮箱/手机号已存在或验证码错误")
    @PostMapping("/register")
    public Result<String> register(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "注册信息(需包含邮箱验证码)", required = true)
            @RequestBody RegisterRequest registerRequest) {
        try {
            // 参数验证
            if (registerRequest.getUsername() == null || registerRequest.getUsername().trim().isEmpty()) {
                return Result.error("用户名不能为空");
            }
            if (registerRequest.getEmail() == null || registerRequest.getEmail().trim().isEmpty()) {
                return Result.error("邮箱不能为空");
            }
            if (registerRequest.getPhone() == null || registerRequest.getPhone().trim().isEmpty()) {
                return Result.error("手机号不能为空");
            }
            if (registerRequest.getPassword() == null || registerRequest.getPassword().trim().isEmpty()) {
                return Result.error("密码不能为空");
            }
            if (registerRequest.getVerificationCode() == null || registerRequest.getVerificationCode().trim().isEmpty()) {
                return Result.error("验证码不能为空");
            }
            
            // 创建用户对象
            Users user = new Users();
            user.setUsername(registerRequest.getUsername());
            user.setEmail(registerRequest.getEmail());
            user.setPhone(registerRequest.getPhone());
            user.setAvatarUrl(registerRequest.getAvatarUrl());
            
            boolean success = usersService.registerUser(user, registerRequest.getPassword(), registerRequest.getVerificationCode());
            if (success) {
                return Result.success("注册成功");
            } else {
                return Result.error("注册失败");
            }
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("注册失败,请稍后重试");
        }
    }
    
    @Operation(summary = "修改密码", description = "用户修改密码，需要提供原密码和邮箱验证码。请先调用发送验证码接口获取验证码")
    @ApiResponse(responseCode = "200", description = "密码修改成功")
    @ApiResponse(responseCode = "400", description = "原密码错误、验证码错误或新密码格式不正确")
    @PutMapping("/changePassword")
    public Result<String> changePassword(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "密码修改信息(需包含邮箱验证码)", required = true)
            @RequestBody ChangePasswordRequest changePasswordRequest) {
        try {
            // 参数验证
            if (changePasswordRequest.getUsername() == null || changePasswordRequest.getUsername().trim().isEmpty()) {
                return Result.error("用户名不能为空");
            }
            if (changePasswordRequest.getEmail() == null || changePasswordRequest.getEmail().trim().isEmpty()) {
                return Result.error("邮箱不能为空");
            }
            if (changePasswordRequest.getOldPassword() == null || changePasswordRequest.getOldPassword().trim().isEmpty()) {
                return Result.error("原密码不能为空");
            }
            if (changePasswordRequest.getNewPassword() == null || changePasswordRequest.getNewPassword().trim().isEmpty()) {
                return Result.error("新密码不能为空");
            }
            if (changePasswordRequest.getVerificationCode() == null || changePasswordRequest.getVerificationCode().trim().isEmpty()) {
                return Result.error("验证码不能为空");
            }
            
            // 调用服务层方法
            boolean success = usersService.changePassword(
                changePasswordRequest.getUsername(),
                changePasswordRequest.getEmail(),
                changePasswordRequest.getOldPassword(),
                changePasswordRequest.getNewPassword(),
                changePasswordRequest.getVerificationCode()
            );
            
            if (success) {
                return Result.success("密码修改成功");
            } else {
                return Result.error("密码修改失败");
            }
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("密码修改失败,请稍后重试");
        }
    }
    
    @Operation(summary = "发送验证码", description = "向指定邮箱发送验证码。场景: register-注册(邮箱不能已注册), forgetPassword-找回密码(邮箱必须已注册)。不指定场景则不检查邮箱注册状态")
    @ApiResponse(responseCode = "200", description = "验证码已发送")
    @ApiResponse(responseCode = "400", description = "邮箱状态不符或发送失败")
    @PostMapping("/sendCode")
    public Result<String> sendVerificationCode(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "邮箱信息和使用场景", required = true)
            @RequestBody SendCodeRequest sendCodeRequest) {
        try {
            // 参数验证
            if (sendCodeRequest.getEmail() == null || sendCodeRequest.getEmail().trim().isEmpty()) {
                return Result.error("邮箱不能为空");
            }
            
            boolean success;
            String scenario = sendCodeRequest.getScenario();
            
            // 根据场景调用不同的方法
            if ("register".equals(scenario)) {
                // 注册场景:邮箱不能已注册
                success = usersService.sendVerificationCodeForRegister(sendCodeRequest.getEmail());
            } else if ("forgetPassword".equals(scenario)) {
                // 找回密码场景:邮箱必须已注册
                success = usersService.sendVerificationCodeForForgetPassword(sendCodeRequest.getEmail());
            } else {
                // 通用场景:不检查邮箱注册状态
                success = usersService.sendVerificationCode(sendCodeRequest.getEmail());
            }
            
            if (success) {
                return Result.success("验证码已发送，请查收邮件(有效期1分钟)");
            } else {
                return Result.error("验证码发送失败，请稍后重试");
            }
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("验证码发送失败，请稍后重试");
        }
    }
    
    @Operation(summary = "忘记密码", description = "通过验证码重置密码")
    @ApiResponse(responseCode = "200", description = "密码重置成功")
    @ApiResponse(responseCode = "400", description = "验证码错误或已过期")
    @PostMapping("/forgetPassword")
    public Result<String> forgetPassword(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "重置密码信息", required = true)
            @RequestBody ForgetPasswordRequest forgetPasswordRequest) {
        try {
            // 参数验证
            if (forgetPasswordRequest.getEmail() == null || forgetPasswordRequest.getEmail().trim().isEmpty()) {
                return Result.error("邮箱不能为空");
            }
            if (forgetPasswordRequest.getVerificationCode() == null || forgetPasswordRequest.getVerificationCode().trim().isEmpty()) {
                return Result.error("验证码不能为空");
            }
            if (forgetPasswordRequest.getNewPassword() == null || forgetPasswordRequest.getNewPassword().trim().isEmpty()) {
                return Result.error("新密码不能为空");
            }
            
            // 调用服务层方法重置密码
            boolean success = usersService.forgetPassword(
                forgetPasswordRequest.getEmail(),
                forgetPasswordRequest.getVerificationCode(),
                forgetPasswordRequest.getNewPassword()
            );
            
            if (success) {
                return Result.success("密码重置成功，请使用新密码登录");
            } else {
                return Result.error("密码重置失败");
            }
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("密码重置失败，请稍后重试");
        }
    }

    @Operation(summary = "更新用户头像", description = "更新用户头像URL")
    @ApiResponse(responseCode = "200", description = "头像更新成功")
    @PostMapping("/updateAvatarUrl")
    public Result<String> updateAvatarUrl(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "头像信息", required = true)
            @RequestBody UpdateAvatarUrlRequest updateAvatarUrlRequest) {
        try {
            if (updateAvatarUrlRequest.getUsername() == null || updateAvatarUrlRequest.getUsername().trim().isEmpty()) {
                return Result.error("姓名不能为空");
            }
            if (updateAvatarUrlRequest.getNewAvatarUrl() == null || updateAvatarUrlRequest.getNewAvatarUrl().trim().isEmpty()) {
                return Result.error("头像不能为空");
            }

            // 更新头像
            boolean success = usersService.updateAvatarUrl(
                    updateAvatarUrlRequest.getUsername(),
                    updateAvatarUrlRequest.getNewAvatarUrl()
            );
            if (success) {
                return Result.success("头像重置成功，请稍等或刷新查看");
            } else {
                return Result.error("头像重置失败");
            }
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("头像重置失败，请稍后重试");
        }
    }

    @Operation(summary = "获取用户信息", description = "根据用户ID获取用户详细信息")
    @ApiResponse(responseCode = "200", description = "获取成功")
    @ApiResponse(responseCode = "400", description = "用户不存在")
    @GetMapping("/getUserInfo/{userId}")
    public Result<Users> getUserInfo(
            @Parameter(description = "用户ID", required = true, example = "1")
            @PathVariable Long userId) {
        try {
            if (userId == null) {
                return Result.error("用户ID不能为空");
            }
            
            Users user = usersService.getUserInfo(userId);
            return Result.success("获取用户信息成功", user);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("获取用户信息失败，请稍后重试");
        }
    }

    @Operation(summary = "编辑用户权限", description = "仅管理员可用，修改用户角色和状态")
    @ApiResponse(responseCode = "200", description = "权限更新成功")
    @ApiResponse(responseCode = "403", description = "权限不足，仅管理员可操作")
    @PutMapping("/updatePrivileges")
    public Result<String> updateUserPrivileges(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "权限信息", required = true)
            @RequestBody UpdatePrivilegesRequest request) {
        try {
            if (request.getUsername() == null) {
                return Result.error("用户名不能为空");
            }
            
            boolean success = usersService.updateUserPrivileges(
                request.getUsername(),
                request.getRole(),
                request.getUserStatus()
            );
            
            if (success) {
                return Result.success("用户权限更新成功");
            } else {
                return Result.error("用户权限更新失败");
            }
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("用户权限更新失败，请稍后重试");
        }
    }

    @Operation(summary = "上传头像", description = "用户上传头像文件，支持jpg/png/gif等图片格式，文件大小不超过5MB")
    @ApiResponse(responseCode = "200", description = "上传成功，返回头像 URL")
    @ApiResponse(responseCode = "400", description = "文件类型不支持或文件过大")
    @PostMapping("/uploadAvatar")
    public Result<String> uploadAvatar(
            @Parameter(description = "用户名", required = true)
            @RequestParam("username") String username,
            @Parameter(description = "头像文件", required = true)
            @RequestParam("file") MultipartFile file) {
        try {
            if (username == null || username.trim().isEmpty()) {
                return Result.error("用户名不能为空");
            }
            
            // 上传文件并获取URL
            String avatarUrl = usersService.uploadAvatar(username, file);
            return Result.success("头像上传成功", avatarUrl);
        } catch (RuntimeException e) {
            // 返回具体的业务错误信息
            return Result.error(e.getMessage());
        } catch (IOException e) {
            // 返回IO异常的详细信息
            return Result.error("文件保存失败: " + e.getMessage());
        } catch (Exception e) {
            // 返回其他异常的详细信息
            e.printStackTrace(); // 打印堆栈信息到控制台
            return Result.error("头像上传失败: " + e.getMessage());
        }
    }
}

