package com.example.demo.config;

import com.example.demo.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    private final String[] url = {
            "/api/auth/login",
            "/api/auth/check-user/**",
            "/api/auth/check-user-status/**",
            "/api/auth/wechat-status/**",
            "/api/auth/debug-user/**",
            "/api/auth/test-db",
            "/api/auth/login-by-openid",
            "/api/auth/login-by-code",
            "/api/test/**",
            "/api/health/**",
            "/api/student/bind-class",
            "/api/public/**",
            "/api/student/download/word/**",
            "/api/assignment/download/**",
            "/actuator/**",
    };

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

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
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    username,
                                    null,
                                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                            );

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    SecurityContextHolder.clearContext();
                }
            } catch (Exception e) {
                e.printStackTrace();
                SecurityContextHolder.clearContext();
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