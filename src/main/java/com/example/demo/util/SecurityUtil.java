package com.example.demo.util;

import com.example.demo.enums.UserRole;
import com.example.demo.pojo.ao.RoleAO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 安全工具类
 * 用于从ThreadLocal中获取当前用户信息
 */
@Slf4j
@Component
public class SecurityUtil {

    /**
     * 线程本地变量，用于存储当前线程的角色信息
     */
    public static ThreadLocal<RoleAO> threadLocal = new ThreadLocal<>();

    /**
     * 设置当前线程的角色信息
     *
     * @param roleAO 角色信息对象
     */
    public static void setRoleAO(RoleAO roleAO) {
        threadLocal.set(roleAO);
        log.debug("设置线程角色信息: username={}, role={}", roleAO.getUsername(), roleAO.getRole());
    }

    /**
     * 获取当前线程的角色信息
     *
     * @return 角色信息Optional，未设置时返回Optional.empty()
     */
    public static Optional<RoleAO> getRoleAO() {
        return Optional.ofNullable(threadLocal.get());
    }

    /**
     * 清除当前线程的角色信息
     */
    public static void clearRoleAO() {
        threadLocal.remove();
        log.debug("清除线程角色信息");
    }

    /**
     * 获取当前登录的用户名
     *
     * @return 用户名Optional，未登录时返回Optional.empty()
     */
    public static Optional<String> getCurrentUsername() {
        return getRoleAO().map(RoleAO::getUsername);
    }

    /**
     * 获取当前登录用户的角色
     *
     * @return 角色Optional，未登录时返回Optional.empty()
     */
    public static Optional<UserRole> getCurrentRole() {
        return getRoleAO().map(RoleAO::getRole);
    }

    /**
     * 获取当前登录用户的ID
     *
     * @return 用户ID Optional，未登录时返回Optional.empty()
     */
    public static Optional<Long> getCurrentUserId() {
        return getRoleAO().map(RoleAO::getUserId);
    }

    /**
     * 检查当前用户是否已登录
     *
     * @return 是否已登录
     */
    public static boolean isAuthenticated() {
        return threadLocal.get() != null;
    }

    /**
     * 检查当前用户是否具有指定角色（支持层级权限）
     *
     * @param requiredRole 要求的角色
     * @return 是否具有权限
     */
    public static boolean hasRole(UserRole requiredRole) {
        Optional<UserRole> currentRole = getCurrentRole();
        if (currentRole.isEmpty()) {
            return false;
        }
        return currentRole.get().hasPermission(requiredRole);
    }

    /**
     * 检查当前用户是否为管理员
     *
     * @return 是否为管理员
     */
    public static boolean isAdmin() {
        return hasRole(UserRole.ADMIN);
    }

    /**
     * 检查当前用户是否为教师或管理员
     *
     * @return 是否为教师或管理员
     */
    public static boolean isTeacherOrAdmin() {
        return hasRole(UserRole.TEACHER);
    }
}