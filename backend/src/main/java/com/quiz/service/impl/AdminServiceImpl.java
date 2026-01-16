package com.quiz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.quiz.entity.User;
import com.quiz.mapper.UserMapper;
import com.quiz.service.AdminService;
import com.quiz.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.ArrayList;

/**
 * 管理员服务实现类
 *
 * @author Quiz System
 * @since 2024-09-29
 */
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Override
    public IPage<User> getUserList(Page<User> page, String username, String nickname, Integer status) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        
        // 只查询普通用户，排除管理员
        queryWrapper.eq(User::getRole, "user");
        
        // 用户名模糊查询
        if (StringUtils.hasText(username)) {
            queryWrapper.like(User::getUsername, username);
        }
        
        // 昵称模糊查询
        if (StringUtils.hasText(nickname)) {
            queryWrapper.like(User::getNickname, nickname);
        }
        
        // 状态查询
        if (status != null) {
            queryWrapper.eq(User::getStatus, status);
        }
        
        // 按创建时间倒序排列
        queryWrapper.orderByDesc(User::getCreatedAt);
        
        IPage<User> result = userMapper.selectPage(page, queryWrapper);
        
        // 确保权限字段不为null，如果为null则设置为空列表
        result.getRecords().forEach(user -> {
            if (user.getPermissions() == null) {
                user.setPermissions(new ArrayList<>());
            }
        });
        
        return result;
    }

    @Override
    public void updateUserPermissions(Long userId, List<String> permissions) {
        // 检查用户是否存在且为普通用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if ("admin".equals(user.getRole())) {
            throw new RuntimeException("不能修改管理员权限");
        }
        
        // 更新权限
        userMapper.updateUserPermissions(userId, permissions);
    }

    @Override
    public void updateUserStatus(Long userId, Integer status) {
        // 检查用户是否存在且为普通用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if ("admin".equals(user.getRole())) {
            throw new RuntimeException("不能修改管理员状态");
        }
        
        // 更新状态
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getId, userId)
                    .set(User::getStatus, status)
                    .set(User::getUpdatedAt, LocalDateTime.now());
        userMapper.update(null, updateWrapper);
    }

    @Override
    public void resetUserPassword(Long userId) {
        // 检查用户是否存在且为普通用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if ("admin".equals(user.getRole())) {
            throw new RuntimeException("不能重置管理员密码");
        }
        
        // 生成随机密码（8位，包含字母和数字）
        String newPassword = generateRandomPassword();
        String encodedPassword = passwordEncoder.encode(newPassword);
        
        // 更新密码
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getId, userId)
                    .set(User::getPassword, encodedPassword)
                    .set(User::getUpdatedAt, LocalDateTime.now());
        userMapper.update(null, updateWrapper);
        
        // 发送密码重置邮件
        try {
            emailService.sendPasswordResetEmail(user.getEmail(), user.getUsername(), newPassword);
        } catch (Exception e) {
            // 邮件发送失败不影响密码重置，但记录日志
            System.err.println("密码重置邮件发送失败: " + e.getMessage());
        }
    }
    
    /**
     * 生成随机密码
     * @return 8位随机密码
     */
    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder password = new StringBuilder();
        
        for (int i = 0; i < 8; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return password.toString();
    }

}
