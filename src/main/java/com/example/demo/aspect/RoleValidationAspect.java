package com.example.demo.aspect;

import com.example.demo.annotation.RequireRole;
import com.example.demo.enums.UserRole;
import com.example.demo.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

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

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(401, "未登录，请先登录");
        }

        Set<String> userRoles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(auth -> auth.replace("ROLE_", ""))
                .collect(Collectors.toSet());

        log.debug("当前用户角色: {}", userRoles);
        log.debug("注解来源: {}", methodAnnotation != null ? "方法级别" : "类级别");

        UserRole[] requiredRoles = effectiveAnnotation.value();
        boolean requireAll = effectiveAnnotation.requireAll();

        boolean hasPermission;
        if (requireAll) {
            hasPermission = Arrays.stream(requiredRoles)
                    .allMatch(role -> userRoles.contains(role.name()));
            log.debug("AND验证 - 需要所有角色: {}, 用户拥有角色: {}, 结果: {}",
                    Arrays.toString(requiredRoles), userRoles, hasPermission);
        } else {
            hasPermission = Arrays.stream(requiredRoles)
                    .anyMatch(role -> userRoles.contains(role.name()));
            log.debug("OR验证 - 需要任一角色: {}, 用户拥有角色: {}, 结果: {}",
                    Arrays.toString(requiredRoles), userRoles, hasPermission);
            if(userRoles.contains(UserRole.ADMIN.name())) {
                hasPermission = true;
            }
        }

        if (!hasPermission) {
            String requiredRoleNames = Arrays.stream(requiredRoles)
                    .map(UserRole::getDescription)
                    .collect(Collectors.joining("或"));
            throw new BusinessException(403, "权限不足，只有" + requiredRoleNames + "可以访问此功能");
        }

        log.debug("角色验证通过");
    }
}