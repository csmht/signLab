package com.example.demo.pojo.dto;

import lombok.Data;

/**
 * 设置密码请求DTO
 */
@Data
public class SetPasswordRequest {

    /** 用户名（学号/工号） */
    private String username;

    /** 新密码（必须为6位数字） */
    private String password;

    /** 确认密码 */
    private String confirmPassword;
}