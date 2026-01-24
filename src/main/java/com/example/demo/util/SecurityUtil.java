package com.example.demo.util;

import com.example.demo.enums.UserRole;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class SecurityUtil {

    public static Optional<Authentication> getAuthentication() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication());
    }

    public static Optional<String> getCurrentUsername() {
        return getAuthentication()
                .map(Authentication::getName)
                .filter(name -> !name.isEmpty());
    }

    public static Set<UserRole> getCurrentUserRoles() {
        return getAuthentication()
                .map(Authentication::getAuthorities)
                .map(authorities -> authorities.stream()
                        .map(GrantedAuthority::getAuthority)
                        .map(auth -> auth.replace("ROLE_", ""))
                        .map(roleName -> {
                            try {
                                return UserRole.valueOf(roleName);
                            } catch (IllegalArgumentException e) {
                                return null;
                            }
                        })
                        .filter(role -> role != null)
                        .collect(Collectors.toSet()))
                .orElse(Set.of());
    }

    public static boolean hasRole(UserRole role) {
        return getCurrentUserRoles().contains(role);
    }

    public static boolean hasAnyRole(UserRole... roles) {
        Set<UserRole> userRoles = getCurrentUserRoles();
        for (UserRole role : roles) {
            if (userRoles.contains(role)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasAllRoles(UserRole... roles) {
        Set<UserRole> userRoles = getCurrentUserRoles();
        for (UserRole role : roles) {
            if (!userRoles.contains(role)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isAuthenticated() {
        return getAuthentication()
                .map(Authentication::isAuthenticated)
                .orElse(false);
    }
}