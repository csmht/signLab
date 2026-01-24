package com.example.demo.pojo.dto;

import lombok.Data;

@Data
public class UserStatusDto {

    private Boolean exists;

    private Boolean passwordSet;

    private String role;

    private String name;
}