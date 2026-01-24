package com.example.demo.pojo.dto;

import lombok.Data;

@Data
public class SetPasswordRequest {

    private String username;

    private String password;

    private String confirmPassword;
}