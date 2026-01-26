package com.example.demo.pojo.request;

import lombok.Data;

import java.util.List;

/**
 * 批量绑定学生到班级请求DTO
 */
@Data
public class BatchBindStudentsRequest {

    /**
     * 班级编号
     */
    private String classCode;

    /**
     * 学生用户名列表（学号列表）
     */
    private List<String> studentUsernames;

}