package com.quiz.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.quiz.dto.Result;
import com.quiz.dto.UpdatePermissionsRequest;
import com.quiz.dto.UpdateUserStatusRequest;
import com.quiz.dto.ResetPasswordRequest;
import com.quiz.entity.User;
import com.quiz.service.AdminService;
import com.quiz.service.QuizSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 管理员控制器
 *
 * @author Quiz System
 * @since 2024-09-29
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Tag(name = "管理员功能", description = "管理员专用功能接口")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final QuizSessionService quizSessionService;

    /**
     * 获取用户列表（分页）
     */
    @Operation(summary = "获取用户列表", description = "分页获取普通用户列表，支持搜索和筛选")
    @GetMapping("/users")
    public Result<IPage<User>> getUserList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "用户名") @RequestParam(required = false) String username,
            @Parameter(description = "昵称") @RequestParam(required = false) String nickname,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        try {
            Page<User> pageParam = new Page<>(page, size);
            IPage<User> result = adminService.getUserList(pageParam, username, nickname, status);
            return Result.success("获取成功", result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新用户权限
     */
    @Operation(summary = "更新用户权限", description = "更新指定用户的权限")
    @PutMapping("/users/permissions")
    public Result<Void> updateUserPermissions(
            @Parameter(description = "权限更新请求") @RequestBody UpdatePermissionsRequest request) {
        try {
            adminService.updateUserPermissions(request.getUserId(), request.getPermissions());
            return Result.success("权限更新成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新用户状态
     */
    @Operation(summary = "更新用户状态", description = "启用或禁用用户")
    @PutMapping("/users/status")
    public Result<Void> updateUserStatus(
            @Parameter(description = "状态更新请求") @RequestBody UpdateUserStatusRequest request) {
        try {
            adminService.updateUserStatus(request.getUserId(), request.getStatus());
            return Result.success("状态更新成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 重置用户密码
     */
    @Operation(summary = "重置用户密码", description = "管理员强制重置用户密码为默认值")
    @PutMapping("/users/reset-password")
    public Result<Void> resetUserPassword(
            @Parameter(description = "密码重置请求") @RequestBody ResetPasswordRequest request) {
        try {
            adminService.resetUserPassword(request.getUserId());
            return Result.success("密码重置成功，新密码已发送到用户邮箱");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取答题会话列表（管理员）
     */
    @Operation(summary = "获取答题会话列表", description = "分页获取所有答题会话，包括已删除的")
    @GetMapping("/sessions")
    public Result<Map<String, Object>> getSessionList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "题库ID") @RequestParam(required = false) Long bankId,
            @Parameter(description = "模式(practice/exam)") @RequestParam(required = false) String mode,
            @Parameter(description = "状态(ongoing/completed)") @RequestParam(required = false) String status,
            @Parameter(description = "是否删除(0-未删除,1-已删除,不传查全部)") @RequestParam(required = false) Integer deleted) {
        try {
            Map<String, Object> result = quizSessionService.getAdminSessionList(current, size, userId, bankId, mode, status, deleted);
            return Result.success("获取成功", result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

}
