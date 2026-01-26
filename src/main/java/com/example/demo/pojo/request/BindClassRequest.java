package com.example.demo.pojo.request;

import lombok.Data;

/**
 * 学生绑定班级请求
 */
@Data
public class BindClassRequest {

    /**
     * 班级验证码
     */
    private String verificationCode;
}
