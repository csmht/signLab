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

/**
 * 用户认证控制器
 * 提供用户登录、密码管理、微信绑定等认证相关的API接口
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录接口
     * 支持用户名密码登录，首次登录需设置密码
     * 登录时可选择绑定微信账号
     *
     * @param request 登录请求参数（用户名、密码、微信授权code）
     * @return 登录响应（包含用户信息和JWT token）
     */
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

    /**
     * 管理员重置用户密码
     * 仅管理员可调用，密码重置为"syjx@学号后四位"
     *
     * @param usernames 需要重置密码的用户名列表
     * @return 操作结果
     */
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

    /**
     * 用户设置密码
     * 用户首次登录或修改密码时使用，密码必须为6位数字
     *
     * @param request 密码设置请求（用户名、新密码、确认密码）
     * @return 操作结果
     */
    @PostMapping("/set-password")
    public ApiResponse<Void> setPassword(@RequestBody SetPasswordRequest request) {
        try {
            authService.setPassword(request);
            return ApiResponse.success(null, "密码设置成功");
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }

    /**
     * 检查用户是否存在
     * 用于前端验证用户名是否可用
     *
     * @param username 用户名（学号/工号）
     * @return 用户是否存在
     */
    @GetMapping("/check-user/{username}")
    public ApiResponse<Boolean> checkUser(@PathVariable String username) {
        try {
            boolean exists = authService.checkUserExists(username);
            return ApiResponse.success(exists);
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }

    /**
     * 检查用户状态
     * 返回用户是否存在、是否已设置密码、用户角色等信息
     *
     * @param username 用户名（学号/工号）
     * @return 用户状态信息
     */
    @GetMapping("/check-user-status/{username}")
    public ApiResponse<UserStatusDto> checkUserStatus(@PathVariable String username) {
        try {
            UserStatusDto status = authService.checkUserStatus(username);
            return ApiResponse.success(status);
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }

    /**
     * 检查用户微信绑定状态
     * 返回用户是否已绑定微信、微信昵称、头像等信息
     *
     * @param username 用户名（学号/工号）
     * @return 微信绑定状态信息
     */
    @GetMapping("/wechat-status/{username}")
    public ApiResponse<WeChatStatusDto> checkWeChatStatus(@PathVariable String username) {
        try {
            WeChatStatusDto status = authService.checkWeChatStatus(username);
            return ApiResponse.success(status);
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }

    /**
     * 学生解绑微信
     * 学生可解除与微信的绑定关系
     * 需要学生角色权限
     *
     * @return 操作结果
     */
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
    /**
     * 调试接口：查询用户详细信息
     * 用于开发调试，返回用户的完整信息
     *
     * @param username 用户名（学号/工号）
     * @return 用户详细信息字符串
     */
    @GetMapping("/debug-user/{username}")
    public ApiResponse<String> debugUser(@PathVariable String username) {
        try {
            return ApiResponse.success(authService.debugUserInfo(username), "用户信息查询成功");
        } catch (Exception e) {
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 数据库连接测试接口
     * 用于测试数据库连接是否正常
     *
     * @return 数据库连接状态和用户总数
     */
    @GetMapping("/test-db")
    public ApiResponse<String> testDatabase() {
        try {
            return ApiResponse.success(authService.testDatabaseConnection(), "数据库连接测试成功");
        } catch (Exception e) {
            return ApiResponse.error(500, "数据库连接失败: " + e.getMessage());
        }
    }
    /**
     * 通过微信openid直接登录
     * 适用于已绑定微信的用户快速登录
     *
     * @param request 请求参数（包含openid）
     * @return 登录响应（包含用户信息和JWT token）
     */
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
    /**
     * 通过微信授权码登录
     * 推荐方式：通过微信授权code获取openid后登录
     *
     * @param request 请求参数（包含微信授权code）
     * @return 登录响应（包含用户信息和JWT token）
     */
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