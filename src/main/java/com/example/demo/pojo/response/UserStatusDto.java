package com.example.demo.pojo.response;

import lombok.Data;

/**
 * 用户状态DTO
 */
@Data
public class UserStatusDto {

    /** 用户是否存在 */
    private Boolean exists;

    /** 是否已设置密码 */
    private Boolean passwordSet;

    /** 用户角色 */
    private String role;

    /** 用户姓名 */
    private String name;
}