package com.example.demo.controller;

import com.example.demo.dto.Result;
import com.example.demo.entity.UsersPrivileges;
import com.example.demo.service.IUsersPrivilegesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "用户权限管理", description = "用户权限信息查询接口")
@RestController
@RequestMapping("/usersPrivileges")
public class UsersPrivilegesController {
    @Autowired
    private IUsersPrivilegesService usersPrivilegesService;

    @Operation(summary = "获取用户权限信息", description = "仅管理员可用，根据用户ID获取用户权限详情")
    @ApiResponse(responseCode = "200", description = "获取成功")
    @ApiResponse(responseCode = "403", description = "权限不足，仅管理员可操作")
    @GetMapping("/getUserPrivilegesInfo/{userId}")
    public Result<UsersPrivileges> getUserInfo(
            @Parameter(description = "用户ID", required = true, example = "1")
            @PathVariable Long userId) {
        try {
            if (userId == null) {
                return Result.error("用户ID不能为空");
            }

            UsersPrivileges usersPrivileges = usersPrivilegesService.getUserPrivilegesInfo(userId);
            return Result.success("获取用户权限信息成功", usersPrivileges);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("获取用户权限信息失败，请稍后重试");
        }
    }
    
    @Operation(summary = "获取所有用户权限信息", description = "仅管理员可用，获取系统中所有用户的权限信息")
    @ApiResponse(responseCode = "200", description = "获取成功")
    @ApiResponse(responseCode = "403", description = "权限不足，仅管理员可操作")
    @GetMapping("/getAllUsersPrivileges")
    public Result<List<UsersPrivileges>> getAllUsersPrivileges() {
        try {
            List<UsersPrivileges> usersPrivilegesList = usersPrivilegesService.getAllUsersPrivileges();
            return Result.success("获取所有用户权限信息成功", usersPrivilegesList);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("获取所有用户权限信息失败，请稍后重试");
        }
    }
}
