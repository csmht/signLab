package com.example.demo.pojo.request;

import lombok.Data;

/**
 * 用户登录请求
 */
@Data
public class LoginRequest {

    /** 用户名（学号/工号） */
    private String username;

    /** 密码 */
    private String password;

    /** 是否记住账号 */
    private Boolean rememberMe;

    /** 微信授权code（可选，用于绑定微信） */
    private String wxCode;
}