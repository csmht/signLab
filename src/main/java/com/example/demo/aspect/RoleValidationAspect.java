package com.example.demo.aspect;

import com.example.demo.annotation.RequireRole;
import com.example.demo.enums.UserRole;
import com.example.demo.exception.BusinessException;
import com.example.demo.util.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 角色验证切面
 * 验证用户是否具有访问接口所需的权限
 * 支持层级权限：管理员 > 教师 > 学生
 */
@Aspect
@Component
@Slf4j
public class RoleValidationAspect {

    @Before("@within(com.example.demo.annotation.RequireRole) || @annotation(com.example.demo.annotation.RequireRole)")
    public void validateRole(JoinPoint joinPoint) {
        log.debug("开始验证用户角色...");

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Class<?> targetClass = joinPoint.getTarget().getClass();

        RequireRole methodAnnotation = method.getAnnotation(RequireRole.class);
        RequireRole classAnnotation = targetClass.getAnnotation(RequireRole.class);

        RequireRole effectiveAnnotation = methodAnnotation != null ? methodAnnotation : classAnnotation;

        if (effectiveAnnotation == null) {
            log.debug("未找到 @RequireRole 注解，跳过验证");
            return;
        }

        // 检查用户是否已登录
        if (!SecurityUtil.isAuthenticated()) {
            throw new BusinessException(401, "未登录，请先登录");
        }

        // 获取用户的角色
        UserRole userRole = SecurityUtil.getCurrentRole()
                .orElseThrow(() -> new BusinessException(403, "用户角色无效"));

        log.debug("当前用户角色: {} (级别: {})", userRole.getDescription(), userRole.getLevel());
        log.debug("注解来源: {}", methodAnnotation != null ? "方法级别" : "类级别");

        UserRole[] requiredRoles = effectiveAnnotation.value();

        // 使用层级权限检查：用户角色的级别 >= 任一要求角色的级别，则满足权限
        int minRequiredLevel = Arrays.stream(requiredRoles)
                .mapToInt(UserRole::getLevel)
                .min()
                .orElse(Integer.MAX_VALUE);

        boolean hasPermission = userRole.getLevel() >= minRequiredLevel;

        log.debug("层级权限验证 - 用户级别: {}, 最低要求级别: {}, 结果: {}",
                userRole.getLevel(), minRequiredLevel, hasPermission);

        if (!hasPermission) {
            String requiredRoleNames = Arrays.stream(requiredRoles)
                    .map(UserRole::getDescription)
                    .collect(Collectors.joining("或"));
            throw new BusinessException(403, "权限不足，只有" + requiredRoleNames + "可以访问此功能");
        }

        log.debug("角色验证通过");
    }
}