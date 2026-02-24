package com.example.demo.pojo.dto.mapvo;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 登录请求
 * 用于替代 Map<String, String> 结构（登录请求体）
 */
@Data
public class LoginRequest {

    /**
     * 登录码
     */
    private String code;

    /**
     * OpenID
     */
    private String openId;

    /**
     * 将 LoginRequest 转换为 Map<String, String>
     *
     * @param request 登录请求对象
     * @return Map<String, String>
     */
    public static Map<String, String> toMap(LoginRequest request) {
        if (request == null) {
            return new HashMap<>();
        }
        Map<String, String> map = new HashMap<>();
        map.put("code", request.getCode());
        map.put("openId", request.getOpenId());
        return map;
    }

    /**
     * 从 Map<String, String> 创建 LoginRequest
     *
     * @param map Map<String, String>
     * @return 登录请求对象
     */
    public static LoginRequest fromMap(Map<String, String> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        LoginRequest request = new LoginRequest();
        request.setCode(map.get("code"));
        request.setOpenId(map.get("openId"));
        return request;
    }
}
