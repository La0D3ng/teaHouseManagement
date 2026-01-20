package com.example.demo.entity;
// 实体层，让数据库字段和后端代码绑定，保证类名和数据库中的表名一样，不过首字母要大写，一个属性类对应一张数据库表。

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体类
 * 对应数据库表: users
 */
@Data
@Schema(description = "用户信息")
@TableName(value = "users")
public class Users {
    @Schema(description = "用户ID")
    @TableId(value = "user_id", type = IdType.AUTO)
    @TableField("user_id")
    private Long userId;

    @Schema(description = "用户名")
    @TableField("username")
    private String username;

    @Schema(description = "邮箱")
    @TableField("email")
    private String email;

    @Schema(description = "手机号")
    @TableField("phone")
    private String phone;

    @Schema(description = "密码哈希值", hidden = true)
    @TableField("password_hash")
    @JsonIgnore
    private String passwordHash;

    @Schema(description = "用户角色", example = "customer", allowableValues = {"manager", "staff", "customer"})
    @TableField("role")
    private UserRole role;

    @Schema(description = "头像URL")
    @TableField("avatar_url")
    private String avatarUrl;

    @Schema(description = "用户状态 (0=禁用, 1=启用)")
    @TableField("user_status")
    private Integer userStatus;

    @Schema(description = "创建时间")
    @TableField("created_at")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    // 用户角色枚举（管理员、员工、用户）
    public enum UserRole {
        manager,
        staff,
        customer
    }

    // Lombok 自动为这些字段生成了 getter 和 setter 方法
}
