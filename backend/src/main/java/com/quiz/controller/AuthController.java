package com.quiz.controller;

import com.quiz.dto.LoginRequest;
import com.quiz.dto.RegisterRequest;
import com.quiz.dto.RegisterWithVerificationRequest;
import com.quiz.dto.SendVerificationCodeRequest;
import com.quiz.dto.UpdatePasswordRequest;
import com.quiz.dto.Result;
import com.quiz.entity.User;
import com.quiz.service.UserService;
import com.quiz.service.VerificationCodeService;
import com.quiz.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 *
 * @author Quiz System
 * @since 2024-09-26
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理", description = "用户注册、登录、登出等认证相关接口")
public class AuthController {

    private final UserService userService;
    private final VerificationCodeService verificationCodeService;

    /**
     * 用户注册
     */
    @Operation(summary = "用户注册", description = "新用户注册接口")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "注册成功", 
                    content = @Content(schema = @Schema(implementation = Result.class))),
            @ApiResponse(responseCode = "400", description = "注册失败", 
                    content = @Content(schema = @Schema(implementation = Result.class)))
    })
    @PostMapping("/register")
    public Result<User> register(@RequestBody @Validated RegisterRequest request) {
        try {
            User user = userService.register(request);
            return Result.success("注册成功", user);
        } catch (Exception e) {
            return Result.badRequest(e.getMessage());
        }
    }

    /**
     * 发送邮箱验证码
     */
    @Operation(summary = "发送邮箱验证码", description = "发送邮箱验证码用于注册验证")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "发送成功", 
                    content = @Content(schema = @Schema(implementation = Result.class))),
            @ApiResponse(responseCode = "400", description = "发送失败", 
                    content = @Content(schema = @Schema(implementation = Result.class)))
    })
    @PostMapping("/send-verification-code")
    public Result<Void> sendVerificationCode(@RequestBody @Validated SendVerificationCodeRequest request) {
        try {
            boolean success = verificationCodeService.sendVerificationCode(request.getEmail());
            if (success) {
                return Result.success("验证码发送成功");
            } else {
                return Result.badRequest("验证码发送失败，请稍后重试");
            }
        } catch (Exception e) {
            return Result.badRequest("验证码发送失败：" + e.getMessage());
        }
    }

    /**
     * 带验证码的用户注册
     */
    @Operation(summary = "带验证码的用户注册", description = "需要邮箱验证码的用户注册接口")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "注册成功", 
                    content = @Content(schema = @Schema(implementation = Result.class))),
            @ApiResponse(responseCode = "400", description = "注册失败", 
                    content = @Content(schema = @Schema(implementation = Result.class)))
    })
    @PostMapping("/register-with-verification")
    public Result<User> registerWithVerification(@RequestBody @Validated RegisterWithVerificationRequest request) {
        try {
            // 验证邮箱验证码
            if (!verificationCodeService.verifyCode(request.getEmail(), request.getVerificationCode())) {
                return Result.badRequest("验证码错误或已过期");
            }

            // 检查邮箱是否已注册
            if (userService.existsByEmail(request.getEmail())) {
                return Result.badRequest("该邮箱已被注册");
            }

            // 创建注册请求
            RegisterRequest registerRequest = new RegisterRequest();
            registerRequest.setUsername(request.getUsername());
            registerRequest.setPassword(request.getPassword());
            registerRequest.setConfirmPassword(request.getPassword()); // 验证码注册时密码已在前端确认
            registerRequest.setNickname(request.getNickname());
            registerRequest.setEmail(request.getEmail());

            // 注册用户
            User user = userService.register(registerRequest);
            return Result.success("注册成功", user);
        } catch (Exception e) {
            return Result.badRequest(e.getMessage());
        }
    }

    /**
     * 用户登录
     */
    @Operation(summary = "用户登录", description = "用户登录接口，返回JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "登录成功", 
                    content = @Content(schema = @Schema(implementation = Result.class))),
            @ApiResponse(responseCode = "400", description = "登录失败", 
                    content = @Content(schema = @Schema(implementation = Result.class)))
    })
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody @Validated LoginRequest request) {
        try {
            String token = userService.login(request);
            User user = userService.getUserByUsername(request.getUsername());
            
            // 获取用户权限信息
            if (user != null) {
                user.setPermissions(userService.getUserPermissions(user.getId()));
            }
            
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("user", user);
            
            return Result.success("登录成功", data);
        } catch (Exception e) {
            return Result.badRequest(e.getMessage());
        }
    }

    /**
     * 检查用户名是否存在
     */
    @Operation(summary = "检查用户名", description = "检查用户名是否已存在")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "检查成功", 
                    content = @Content(schema = @Schema(implementation = Result.class)))
    })
    @GetMapping("/check-username")
    public Result<Boolean> checkUsername(@RequestParam String username) {
        boolean exists = userService.checkUsernameExists(username);
        return Result.success(exists);
    }

    /**
     * 检查邮箱是否已注册
     */
    @Operation(summary = "检查邮箱是否已注册", description = "注册时检查邮箱是否已被占用")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "检查成功", 
                    content = @Content(schema = @Schema(implementation = Result.class)))
    })
    @GetMapping("/check-email")
    public Result<Boolean> checkEmail(@RequestParam String email) {
        boolean exists = userService.existsByEmail(email);
        return Result.success(exists);
    }

    /**
     * 获取当前用户信息
     */
    @Operation(summary = "获取用户信息", description = "获取当前登录用户的详细信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功", 
                    content = @Content(schema = @Schema(implementation = Result.class))),
            @ApiResponse(responseCode = "401", description = "未登录", 
                    content = @Content(schema = @Schema(implementation = Result.class)))
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/profile")
    public Result<User> getProfile() {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            User user = userService.getById(userId);
            if (user == null) {
                return Result.error("用户不存在");
            }
            
            // 获取用户权限信息
            user.setPermissions(userService.getUserPermissions(userId));
            
            return Result.success("获取成功", user);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新用户信息
     */
    @Operation(summary = "更新用户信息", description = "更新当前登录用户的信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "更新成功",
                    content = @Content(schema = @Schema(implementation = Result.class))),
            @ApiResponse(responseCode = "401", description = "未登录",
                    content = @Content(schema = @Schema(implementation = Result.class)))
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PutMapping("/profile")
    public Result<User> updateProfile(@RequestBody User user) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            if (userId == null) {
                return Result.error("用户未登录");
            }

            // 使用 LambdaUpdateWrapper 只更新特定字段，避免 role 等敏感字段被意外修改
            com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<User> updateWrapper =
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<>();
            updateWrapper.eq(User::getId, userId)
                        .set(User::getNickname, user.getNickname())
                        .set(User::getEmail, user.getEmail())
                        .set(User::getAvatar, user.getAvatar());

            boolean success = userService.update(updateWrapper);
            if (success) {
                User updatedUser = userService.getById(userId);
                // 获取用户权限信息
                updatedUser.setPermissions(userService.getUserPermissions(userId));
                return Result.success("更新成功", updatedUser);
            } else {
                return Result.error("更新失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 修改密码
     */
    @Operation(summary = "修改密码", description = "修改当前登录用户的密码")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "修改成功",
                    content = @Content(schema = @Schema(implementation = Result.class))),
            @ApiResponse(responseCode = "401", description = "未登录",
                    content = @Content(schema = @Schema(implementation = Result.class))),
            @ApiResponse(responseCode = "400", description = "修改失败",
                    content = @Content(schema = @Schema(implementation = Result.class)))
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PutMapping("/password")
    public Result<Void> updatePassword(@RequestBody @Validated UpdatePasswordRequest request) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            if (userId == null) {
                return Result.error("用户未登录");
            }

            // 验证两次输入的新密码是否一致
            if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                return Result.badRequest("两次输入的新密码不一致");
            }

            boolean success = userService.updatePassword(userId, request.getOldPassword(), request.getNewPassword());
            if (success) {
                return Result.success("密码修改成功");
            } else {
                return Result.error("密码修改失败");
            }
        } catch (Exception e) {
            return Result.badRequest(e.getMessage());
        }
    }

    /**
     * 用户登出
     */
    @Operation(summary = "用户登出", description = "用户登出接口")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "登出成功", 
                    content = @Content(schema = @Schema(implementation = Result.class)))
    })
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader("Authorization") String token) {
        // JWT是无状态的，客户端删除token即可
        return Result.success("登出成功");
    }
}
