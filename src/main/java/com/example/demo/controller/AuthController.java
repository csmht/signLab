package com.example.demo.controller;

import com.example.demo.annotation.RequireRole;
import com.example.demo.enums.UserRole;
import com.example.demo.exception.BusinessException;
import com.example.demo.pojo.dto.*;
import com.example.demo.service.AuthService;
import com.example.demo.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return ApiResponse.success(response, "登录成功");
        } catch (com.example.demo.exception.BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error(500, "登录失败: " + e.getMessage());
        }
    }

    @RequireRole(value = UserRole.ADMIN)
    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@RequestBody List<String> usernames) {
        try {
            authService.resetPassword(usernames);
            return ApiResponse.success(null, "密码重置成功");
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }

    @PostMapping("/set-password")
    public ApiResponse<Void> setPassword(@RequestBody SetPasswordRequest request) {
        try {
            authService.setPassword(request);
            return ApiResponse.success(null, "密码设置成功");
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }

    @GetMapping("/check-user/{username}")
    public ApiResponse<Boolean> checkUser(@PathVariable String username) {
        try {
            boolean exists = authService.checkUserExists(username);
            return ApiResponse.success(exists);
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }

    @GetMapping("/check-user-status/{username}")
    public ApiResponse<UserStatusDto> checkUserStatus(@PathVariable String username) {
        try {
            UserStatusDto status = authService.checkUserStatus(username);
            return ApiResponse.success(status);
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }

    @GetMapping("/wechat-status/{username}")
    public ApiResponse<WeChatStatusDto> checkWeChatStatus(@PathVariable String username) {
        try {
            WeChatStatusDto status = authService.checkWeChatStatus(username);
            return ApiResponse.success(status);
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }

    @RequireRole(value = UserRole.STUDENT)
    @PostMapping("/unbind-wechat")
    public ApiResponse<Void> unbindWeChat() {
        try {

            Optional<String> currentUsername = SecurityUtil.getCurrentUsername();
            if(currentUsername.isEmpty()) {
                throw new BusinessException("尚未登录");
            }

            String username = currentUsername.get();

            authService.unbindWeChat(username);
            return ApiResponse.success(null, "微信解绑成功");
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }

    @GetMapping("/debug-user/{username}")
    public ApiResponse<String> debugUser(@PathVariable String username) {
        try {
            return ApiResponse.success(authService.debugUserInfo(username), "用户信息查询成功");
        } catch (Exception e) {
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/test-db")
    public ApiResponse<String> testDatabase() {
        try {
            return ApiResponse.success(authService.testDatabaseConnection(), "数据库连接测试成功");
        } catch (Exception e) {
            return ApiResponse.error(500, "数据库连接失败: " + e.getMessage());
        }
    }

    @PostMapping("/login-by-openid")
    public ApiResponse<LoginResponse> loginByOpenId(@RequestBody Map<String, String> request) {
        try {
            String openid = request.get("openid");
            if (openid == null || openid.trim().isEmpty()) {
                return ApiResponse.error(400, "openid不能为空");
            }

            LoginResponse response = authService.loginByOpenId(openid);
            return ApiResponse.success(response, "登录成功");
        } catch (com.example.demo.exception.BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error(500, "登录失败: " + e.getMessage());
        }
    }

    @PostMapping("/login-by-code")
    public ApiResponse<LoginResponse> loginByCode(@RequestBody Map<String, String> request) {
        try {
            String code = request.get("code");
            if (code == null || code.trim().isEmpty()) {
                return ApiResponse.error(400, "code不能为空");
            }

            LoginResponse response = authService.loginByCode(code);
            return ApiResponse.success(response, "登录成功");
        } catch (com.example.demo.exception.BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error(500, "登录失败: " + e.getMessage());
        }
    }

}