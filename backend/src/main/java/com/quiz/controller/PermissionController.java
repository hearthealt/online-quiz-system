package com.quiz.controller;

import com.quiz.annotation.RequirePermission;
import com.quiz.dto.Result;
import com.quiz.service.PermissionService;
import com.quiz.service.UserService;
import com.quiz.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 权限管理控制器
 *
 * @author Quiz System
 * @since 2024-09-29
 */
@RestController
@RequestMapping("/permissions")
@RequiredArgsConstructor
@Tag(name = "权限管理", description = "用户权限管理相关接口")
public class PermissionController {

    private final PermissionService permissionService;
    private final UserService userService;

    /**
     * 获取用户权限
     */
    @Operation(summary = "获取用户权限", description = "获取指定用户的权限列表")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功", 
                    content = @Content(schema = @Schema(implementation = Result.class))),
            @ApiResponse(responseCode = "403", description = "权限不足", 
                    content = @Content(schema = @Schema(implementation = Result.class)))
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/user/{userId}")
    @RequirePermission("admin")
    public Result<List<String>> getUserPermissions(@PathVariable Long userId) {
        try {
            List<String> permissions = permissionService.getUserPermissions(userId);
            return Result.success("获取成功", permissions);
        } catch (Exception e) {
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 更新用户权限
     */
    @Operation(summary = "更新用户权限", description = "更新指定用户的权限列表")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "更新成功", 
                    content = @Content(schema = @Schema(implementation = Result.class))),
            @ApiResponse(responseCode = "403", description = "权限不足", 
                    content = @Content(schema = @Schema(implementation = Result.class)))
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PutMapping("/user/{userId}")
    @RequirePermission("admin")
    public Result<Void> updateUserPermissions(
            @PathVariable Long userId,
            @RequestBody Map<String, List<String>> request) {
        try {
            List<String> permissions = request.get("permissions");
            if (permissions == null) {
                return Result.error("权限列表不能为空");
            }

            boolean success = permissionService.updateUserPermissions(userId, permissions);
            if (success) {
                return Result.success("权限更新成功");
            } else {
                return Result.error("权限更新失败");
            }
        } catch (Exception e) {
            return Result.error("更新失败: " + e.getMessage());
        }
    }

    /**
     * 检查用户权限
     */
    @Operation(summary = "检查用户权限", description = "检查指定用户是否有特定权限")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "检查成功", 
                    content = @Content(schema = @Schema(implementation = Result.class))),
            @ApiResponse(responseCode = "403", description = "权限不足", 
                    content = @Content(schema = @Schema(implementation = Result.class)))
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/user/{userId}/check")
    @RequirePermission("admin")
    public Result<Boolean> checkUserPermission(
            @PathVariable Long userId,
            @Parameter(description = "权限标识") @RequestParam String permission) {
        try {
            boolean hasPermission = permissionService.hasPermission(userId, permission);
            return Result.success("检查成功", hasPermission);
        } catch (Exception e) {
            return Result.error("检查失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有用户列表（用于权限管理）
     */
    @Operation(summary = "获取用户列表", description = "获取所有用户列表用于权限管理")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功", 
                    content = @Content(schema = @Schema(implementation = Result.class))),
            @ApiResponse(responseCode = "403", description = "权限不足", 
                    content = @Content(schema = @Schema(implementation = Result.class)))
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/users")
    @RequirePermission("admin")
    public Result<List<User>> getAllUsers() {
        try {
            List<User> users = userService.list();
            // 为每个用户加载权限信息
            for (User user : users) {
                user.setPermissions(permissionService.getUserPermissions(user.getId()));
            }
            return Result.success("获取成功", users);
        } catch (Exception e) {
            return Result.error("获取失败: " + e.getMessage());
        }
    }
}
