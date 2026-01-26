package com.example.demo.pojo.request;

import lombok.Data;

/**
 * 批量添加用户请求DTO
 * 用于Excel导入用户数据
 * 按列序号映射：id(0), username(1), name(2), password(3), role(4)
 */
@Data
public class BatchAddUserRequest {

    /**
     * 用户名（学号/工号）- 对应Excel第2列（索引1）
     */
    private String username;

    /**
     * 姓名 - 对应Excel第3列（索引2）
     */
    private String name;

    /**
     * 角色（student/teacher/admin）- 对应Excel第5列（索引4）
     */
    private String role;

    /**
     * 院系 - 对应Excel第10列（索引9）
     */
    private String department;

    /**
     * 专业 - 对应Excel第11列（索引10）
     */
    private String major;
}