package com.example.demo.interceptor;

import com.example.demo.enums.UserRole;
import com.example.demo.pojo.ao.RoleAO;
import com.example.demo.util.JwtUtil;
import com.example.demo.util.SecurityUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 自定义认证拦截器
 * 替代 SecurityConfig 和 JwtAuthenticationFilter 的功能
 * 负责验证 JWT 令牌并设置用户信息到 ThreadLocal
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    /**
     * 不需要认证的路径列表
     * 支持通配符 * 匹配单级路径,** 匹配多级路径
     */
    private static final List<String> PERMITTED_PATTERNS = Arrays.asList(
            "/api/auth/**",
            "/api/test/**",
            "/api/health/**",
            "/api/student/bind-class",
            "/api/public/**",
            "/api/student/download/word/**",
            "/api/assignment/download/**",
            "/actuator/**",
            "/error"
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        String requestUri = request.getRequestURI();
        String method = request.getMethod();

        log.debug("认证拦截器 - 请求: {} {}", method, requestUri);

        // 处理 OPTIONS 请求
        if ("OPTIONS".equals(method)) {
            response.setStatus(HttpServletResponse.SC_OK);
            return false;
        }

        // 检查路径是否在白名单中
        if (isPermittedPath(requestUri)) {
            log.debug("路径在白名单中,无需认证: {}", requestUri);
            return true;
        }

        // 提取并验证 JWT 令牌
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                String username = jwtUtil.getUsernameFromToken(token);
                String role = jwtUtil.getRoleFromToken(token);

                if (username != null && role != null && !jwtUtil.isTokenExpired(token)) {
                    // 将用户信息设置到 ThreadLocal
                    RoleAO roleAO = new RoleAO(username, UserRole.valueOf(role.toUpperCase()));
                    SecurityUtil.setRoleAO(roleAO);
                    log.debug("JWT认证成功 - 用户: {}, 角色: {}", username, role);

                    return true;
                } else {
                    log.warn("JWT令牌无效或已过期 - URI: {}", requestUri);
                    sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "令牌无效或已过期");
                    return false;
                }
            } catch (Exception e) {
                log.error("JWT令牌解析失败 - URI: {}", requestUri, e);
                sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "令牌解析失败");
                return false;
            }
        } else {
            log.warn("未提供有效的令牌 - URI: {}", requestUri);
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "未提供有效的令牌");
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, @Nullable Exception ex) {
        // 请求处理完成后清理ThreadLocal,防止内存泄漏
        SecurityUtil.clearRoleAO();
    }

    /**
     * 检查请求路径是否在白名单中
     * 支持通配符匹配
     *
     * @param requestUri 请求URI
     * @return 是否允许通过
     */
    private boolean isPermittedPath(String requestUri) {
        // 精确匹配
        if (PERMITTED_PATTERNS.contains(requestUri)) {
            return true;
        }

        // 通配符匹配
        for (String pattern : PERMITTED_PATTERNS) {
            if (matchesPattern(requestUri, pattern)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 路径通配符匹配
     *
     * @param path    请求路径
     * @param pattern 匹配模式
     * @return 是否匹配
     */
    private boolean matchesPattern(String path, String pattern) {
        // 将通配符模式转换为正则表达式
        String regex = pattern
                .replace("**", ".*")
                .replace("*", "[^/]*");

        return path.matches(regex);
    }

    /**
     * 发送错误响应
     *
     * @param response HTTP响应对象
     * @param status   HTTP状态码
     * @param message  错误消息
     */
    private void sendErrorResponse(HttpServletResponse response, HttpStatus status, String message)
            throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json;charset=UTF-8");

        Map<String, Object> error = new HashMap<>();
        error.put("code", status.value());
        error.put("message", message);

        response.getWriter().write(objectMapper.writeValueAsString(error));
    }
}
