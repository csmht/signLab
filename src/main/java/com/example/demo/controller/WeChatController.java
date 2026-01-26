package com.example.demo.controller;

import com.example.demo.annotation.RequireRole;
import com.example.demo.enums.UserRole;
import com.example.demo.pojo.response.ApiResponse;
import com.example.demo.pojo.response.WeChatBindResponse;
import com.example.demo.service.WeChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 微信绑定控制器
 * 提供微信账号绑定和解绑的接口
 */
@RequestMapping("/api/wechat")
@RestController
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class WeChatController {

    private final WeChatService weChatService;
    private final com.example.demo.util.JwtUtil jwtUtil;

    /**
     * 获取微信绑定状态
     *
     * @return 微信绑定状态
     */
    @GetMapping("/bind-status")
    public ApiResponse<WeChatBindResponse> getBindStatus() {
        try {
            String username = com.example.demo.util.SecurityUtil.getCurrentUsername()
                    .orElseThrow(() -> new com.example.demo.exception.BusinessException(401, "未登录"));

            WeChatBindResponse response = weChatService.getWeChatBindStatus(username);

            return ApiResponse.success(response);
        } catch (com.example.demo.exception.BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("获取微信绑定状态失败", e);
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 绑定微信账号
     *
     * @param code 微信授权code
     * @return 绑定结果
     */
    @PostMapping("/bind")
    public ApiResponse<WeChatBindResponse> bindWeChat(@RequestParam("code") String code) {
        try {
            String username = com.example.demo.util.SecurityUtil.getCurrentUsername()
                    .orElseThrow(() -> new com.example.demo.exception.BusinessException(401, "未登录"));

            // 1. 通过code获取access_token和openid
            com.example.demo.pojo.response.WeChatAccessTokenDto tokenDto = weChatService.getAccessToken(code);

            // 2. 获取微信用户信息
            com.example.demo.pojo.response.WeChatUserInfoDto userInfo = weChatService.getUserInfo(
                    tokenDto.getAccessToken(),
                    tokenDto.getOpenid()
            );

            // 3. 绑定微信账号
            WeChatBindResponse response = weChatService.bindWeChat(
                    username,
                    tokenDto.getOpenid(),
                    tokenDto.getUnionid(),
                    userInfo.getNickname(),
                    userInfo.getHeadimgurl()
            );

            return ApiResponse.success(response, "绑定成功");
        } catch (com.example.demo.exception.BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("绑定微信失败", e);
            return ApiResponse.error(500, "绑定失败: " + e.getMessage());
        }
    }

    /**
     * 解绑微信账号
     *
     * @return 是否解绑成功
     */
    @PostMapping("/unbind")
    public ApiResponse<Void> unbindWeChat() {
        try {
            String username = com.example.demo.util.SecurityUtil.getCurrentUsername()
                    .orElseThrow(() -> new com.example.demo.exception.BusinessException(401, "未登录"));

            weChatService.unbindWeChat(username);

            return ApiResponse.success(null, "解绑成功");
        } catch (com.example.demo.exception.BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("解绑微信失败", e);
            return ApiResponse.error(500, "解绑失败: " + e.getMessage());
        }
    }

    /**
     * 通过微信code登录
     *
     * @param code 微信授权code
     * @return 登录结果（包含JWT token）
     */
    @PostMapping("/login")
    public ApiResponse<com.example.demo.pojo.response.LoginResponse> loginByWeChat(@RequestParam("code") String code) {
        try {
            // 1. 通过code获取openid
            String openid = weChatService.getOpenIdByCode(code);
            if (openid == null) {
                return ApiResponse.error(400, "获取微信信息失败");
            }

            // 2. 通过openid查找用户
            com.example.demo.pojo.entity.User user = weChatService.getUserByWxOpenid(openid);
            if (user == null) {
                return ApiResponse.error(404, "未绑定微信账号，请先绑定");
            }

            // 3. 生成JWT token
            String token = jwtUtil.generateToken(user.getUsername(), user.getRole());

            // 4. 构建登录响应
            com.example.demo.pojo.response.LoginResponse response = new com.example.demo.pojo.response.LoginResponse();
            response.setToken(token);
            response.setUsername(user.getUsername());
            response.setName(user.getName());
            response.setRole(user.getRole());

            return ApiResponse.success(response, "登录成功");
        } catch (Exception e) {
            log.error("微信登录失败", e);
            return ApiResponse.error(500, "登录失败: " + e.getMessage());
        }
    }
}
