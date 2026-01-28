package com.example.demo.pojo.request;

import lombok.Data;

/**
 * 学生查询请求
 * 用于学生的查询、分页查询
 */
@Data
public class StudentQueryRequest {

    /**
     * 当前页码
     */
    private Integer current = 1;

    /**
     * 每页条数
     */
    private Integer size = 10;

    /**
     * 学生用户名（模糊查询）
     */
    private String studentUsername;

    /**
     * 是否分页查询（true-分页，false-列表）
     */
    private Boolean pageable = true;
}
