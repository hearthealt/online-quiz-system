package com.quiz.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.quiz.entity.User;

import java.util.List;

/**
 * 管理员服务接口
 *
 * @author Quiz System
 * @since 2024-09-29
 */
public interface AdminService {

    /**
     * 获取用户列表（分页，只查询普通用户）
     *
     * @param page 分页参数
     * @param username 用户名（模糊查询）
     * @param nickname 昵称（模糊查询）
     * @param status 状态
     * @return 用户分页列表
     */
    IPage<User> getUserList(Page<User> page, String username, String nickname, Integer status);

    /**
     * 更新用户权限
     *
     * @param userId 用户ID
     * @param permissions 权限列表
     */
    void updateUserPermissions(Long userId, List<String> permissions);

    /**
     * 更新用户状态
     *
     * @param userId 用户ID
     * @param status 状态（0-禁用，1-正常）
     */
    void updateUserStatus(Long userId, Integer status);

    /**
     * 重置用户密码
     *
     * @param userId 用户ID
     */
    void resetUserPassword(Long userId);

}