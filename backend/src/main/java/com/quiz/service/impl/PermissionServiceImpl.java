package com.quiz.service.impl;

import com.quiz.entity.User;
import com.quiz.service.PermissionService;
import com.quiz.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 权限管理服务实现类
 *
 * @author Quiz System
 * @since 2024-09-29
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final UserService userService;

    @Override
    public boolean hasPermission(Long userId, String permission) {
        User user = userService.getById(userId);
        if (user == null) {
            return false;
        }
        return user.hasPermission(permission);
    }

    @Override
    public boolean isAdmin(Long userId) {
        User user = userService.getById(userId);
        if (user == null) {
            return false;
        }
        return user.isAdmin();
    }

    @Override
    public List<String> getUserPermissions(Long userId) {
        User user = userService.getById(userId);
        if (user == null) {
            return null;
        }
        return user.getPermissions();
    }

    @Override
    public boolean updateUserPermissions(Long userId, List<String> permissions) {
        try {
            User user = userService.getById(userId);
            if (user == null) {
                return false;
            }
            
            // 只有管理员可以更新权限
            if (!user.isAdmin()) {
                log.warn("非管理员用户尝试更新权限: userId={}", userId);
                return false;
            }
            
            user.setPermissions(permissions);
            return userService.updateById(user);
        } catch (Exception e) {
            log.error("更新用户权限失败: userId={}, permissions={}", userId, permissions, e);
            return false;
        }
    }

    @Override
    public boolean hasQuestionManagePermission(Long userId) {
        return hasPermission(userId, "question:create") ||
               hasPermission(userId, "question:edit") ||
               hasPermission(userId, "question:delete") ||
               hasPermission(userId, "question:upload") ||
               hasPermission(userId, "question:batch");
    }
}
