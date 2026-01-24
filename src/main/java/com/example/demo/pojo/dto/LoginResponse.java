package com.example.demo.pojo.dto;

import lombok.Data;

@Data
public class LoginResponse {

    private Long userId;

    private String username;

    private String name;

    private String role;

    private String token;

    private Boolean isFirstLogin;

    private String wxNickname;

    private String wxAvatar;
}