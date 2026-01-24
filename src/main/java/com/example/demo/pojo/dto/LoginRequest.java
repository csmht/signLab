package com.example.demo.pojo.dto;

import lombok.Data;

@Data
public class LoginRequest {

    private String username;

    private String password;

    private Boolean rememberMe;

    private String wxCode;
}