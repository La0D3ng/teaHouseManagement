package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("userprivileges") // 映射数据库视图
@Schema(description = "用户权限信息（视图）")
public class UsersPrivileges {
    @Schema(description = "用户ID", example = "1")
    @TableId(value = "user_id", type = IdType.INPUT)
    @TableField("user_id")
    private Long userId;

    @Schema(description = "用户名", example = "张三", requiredMode = Schema.RequiredMode.REQUIRED)
    @TableField("username")
    private String username;

    @Schema(description = "用户角色", example = "customer", allowableValues = {"manager", "staff", "customer"}, requiredMode = Schema.RequiredMode.REQUIRED)
    @TableField("role")
    private UserRole role;

    @Schema(description = "用户状态（0-禁用，1-启用）", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @TableField("user_status")
    private int userStatus; // 0 和 1

    @Schema(description = "最后更新时间", example = "2024-01-01T10:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    // 用户角色枚举
    public enum UserRole {
        manager,   // 管理员
        staff,     // 员工
        customer   // 客户
    }
}