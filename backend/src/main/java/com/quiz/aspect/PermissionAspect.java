package com.quiz.aspect;

import com.quiz.annotation.RequirePermission;
import com.quiz.service.PermissionService;
import com.quiz.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * 权限检查切面
 *
 * @author Quiz System
 * @since 2024-09-29
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class PermissionAspect {

    private final PermissionService permissionService;

    @Around("@annotation(requirePermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint, RequirePermission requirePermission) throws Throwable {
        try {
            // 获取当前用户ID
            Long userId = SecurityUtils.getCurrentUserId();
            if (userId == null) {
                log.warn("未找到当前用户ID");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("未授权访问");
            }

            // 检查权限
            String permission = requirePermission.value();
            if (!permissionService.hasPermission(userId, permission)) {
                log.warn("用户 {} 没有权限 {} 访问 {}", userId, permission, joinPoint.getSignature().getName());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("权限不足");
            }

            // 权限检查通过，继续执行
            return joinPoint.proceed();
        } catch (Exception e) {
            log.error("权限检查异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("权限检查失败");
        }
    }
}
