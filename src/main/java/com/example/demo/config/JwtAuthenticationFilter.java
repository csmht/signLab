package com.example.demo.config;

import com.example.demo.enums.UserRole;
import com.example.demo.pojo.ao.RoleAO;
import com.example.demo.util.JwtUtil;
import com.example.demo.util.SecurityUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT认证过滤器
 * 从请求头中提取JWT令牌，验证并设置用户信息到ThreadLocal
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    private final String[] url = {
            "/api/auth/login"
    };

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            if (isUrlPermitted(request)) {
                filterChain.doFilter(request, response);
                return;
            }

            String authHeader = request.getHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                try {
                    String username = jwtUtil.getUsernameFromToken(token);
                    String role = jwtUtil.getRoleFromToken(token);

                    if (username != null && role != null && !jwtUtil.isTokenExpired(token)) {
                        // 将用户信息设置到ThreadLocal
                        RoleAO roleAO = new RoleAO(username, UserRole.valueOf(role.toUpperCase()));
                        SecurityUtil.setRoleAO(roleAO);
                        log.debug("JWT认证成功 - 用户: {}, 角色: {}", username, role);
                    } else {
                        log.warn("JWT令牌无效或已过期");
                        SecurityUtil.clearRoleAO();
                    }
                } catch (Exception e) {
                    log.error("JWT令牌解析失败", e);
                    SecurityUtil.clearRoleAO();
                }
            } else {
                if (request.getMethod().equals("OPTIONS")) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    return;
                }
                sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "未提供有效的令牌");
                return;
            }

            filterChain.doFilter(request, response);
        } finally {
            // 请求处理完成后清理ThreadLocal，防止内存泄漏
            SecurityUtil.clearRoleAO();
        }
    }

    private boolean isUrlPermitted(HttpServletRequest request) {
        for (String urlPattern : url) {
            if (request.getRequestURI().matches(urlPattern.replace("**", ".*"))) {
                return true;
            }
        }
        return false;
    }

    private void sendErrorResponse(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json;charset=UTF-8");

        Map<String, Object> error = new HashMap<>();
        error.put("code", status.value());
        error.put("message", message);

        response.getWriter().write(objectMapper.writeValueAsString(error));
    }
}