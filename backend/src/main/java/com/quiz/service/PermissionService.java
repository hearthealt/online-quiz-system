package com.quiz.service;

import java.util.List;

/**
 * 权限管理服务接口
 *
 * @author Quiz System
 * @since 2024-09-29
 */
public interface PermissionService {

    /**
     * 检查用户是否有指定权限
     *
     * @param userId 用户ID
     * @param permission 权限标识
     * @return 是否有权限
     */
    boolean hasPermission(Long userId, String permission);

    /**
     * 检查用户是否为管理员
     *
     * @param userId 用户ID
     * @return 是否为管理员
     */
    boolean isAdmin(Long userId);

    /**
     * 获取用户权限列表
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    List<String> getUserPermissions(Long userId);

    /**
     * 更新用户权限
     *
     * @param userId 用户ID
     * @param permissions 权限列表
     * @return 是否成功
     */
    boolean updateUserPermissions(Long userId, List<String> permissions);

    /**
     * 检查题目管理权限
     *
     * @param userId 用户ID
     * @return 是否有题目管理权限
     */
    boolean hasQuestionManagePermission(Long userId);
}
