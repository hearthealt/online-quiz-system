package com.quiz.utils;

import com.quiz.exception.ForbiddenException;
import com.quiz.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 用户验证工具类
 * 用于消除Controller中重复的用户验证代码
 *
 * @author Quiz System
 * @since 2024-10-09
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserValidationHelper {

    /**
     * 获取当前用户ID，如果未登录则抛出异常
     *
     * @return 当前用户ID
     * @throws UnauthorizedException 用户未登录时抛出
     */
    public Long getCurrentUserIdOrThrow() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            log.warn("用户未登录，尝试访问需要登录的资源");
            throw new UnauthorizedException("用户未登录，请先登录");
        }
        return userId;
    }

    /**
     * 验证资源所有权
     * 检查当前用户是否拥有指定资源的权限
     *
     * @param resourceOwnerId 资源所有者ID
     * @param currentUserId   当前用户ID
     * @param resourceName    资源名称（用于日志）
     * @throws ForbiddenException 权限不足时抛出
     */
    public void validateOwnership(Long resourceOwnerId, Long currentUserId, String resourceName) {
        if (resourceOwnerId == null) {
            log.error("资源所有者ID为空: {}", resourceName);
            throw new ForbiddenException("资源所有者信息异常");
        }

        if (!resourceOwnerId.equals(currentUserId)) {
            log.warn("用户 {} 尝试访问不属于自己的资源: {} (资源所有者: {})",
                    currentUserId, resourceName, resourceOwnerId);
            throw new ForbiddenException("无权限操作此" + resourceName);
        }
    }

    /**
     * 验证资源所有权（重载方法，使用默认资源名称）
     *
     * @param resourceOwnerId 资源所有者ID
     * @param currentUserId   当前用户ID
     * @throws ForbiddenException 权限不足时抛出
     */
    public void validateOwnership(Long resourceOwnerId, Long currentUserId) {
        validateOwnership(resourceOwnerId, currentUserId, "资源");
    }

    /**
     * 获取当前用户ID并验证资源所有权
     * 这是最常用的组合操作
     *
     * @param resourceOwnerId 资源所有者ID
     * @param resourceName    资源名称
     * @return 当前用户ID
     * @throws UnauthorizedException 用户未登录时抛出
     * @throws ForbiddenException    权限不足时抛出
     */
    public Long getCurrentUserAndValidateOwnership(Long resourceOwnerId, String resourceName) {
        Long currentUserId = getCurrentUserIdOrThrow();
        validateOwnership(resourceOwnerId, currentUserId, resourceName);
        return currentUserId;
    }

    /**
     * 获取当前用户ID并验证资源所有权（重载方法）
     *
     * @param resourceOwnerId 资源所有者ID
     * @return 当前用户ID
     * @throws UnauthorizedException 用户未登录时抛出
     * @throws ForbiddenException    权限不足时抛出
     */
    public Long getCurrentUserAndValidateOwnership(Long resourceOwnerId) {
        return getCurrentUserAndValidateOwnership(resourceOwnerId, "资源");
    }

    /**
     * 验证用户是否为管理员
     *
     * @param userId 用户ID
     * @throws ForbiddenException 不是管理员时抛出
     */
    public void validateAdmin(Long userId) {
        // 这里可以根据实际业务逻辑来判断是否为管理员
        // 例如查询用户角色表或者检查特定的用户ID

        // 示例：假设ID为1的用户是管理员
        if (!Long.valueOf(1L).equals(userId)) {
            log.warn("用户 {} 尝试执行管理员操作，但不是管理员", userId);
            throw new ForbiddenException("需要管理员权限");
        }
    }

    /**
     * 获取当前用户ID并验证管理员权限
     *
     * @return 当前用户ID
     * @throws UnauthorizedException 用户未登录时抛出
     * @throws ForbiddenException    不是管理员时抛出
     */
    public Long getCurrentUserAndValidateAdmin() {
        Long currentUserId = getCurrentUserIdOrThrow();
        validateAdmin(currentUserId);
        return currentUserId;
    }
}