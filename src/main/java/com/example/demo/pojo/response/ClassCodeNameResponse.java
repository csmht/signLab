package com.example.demo.pojo.response;

import lombok.Data;

/**
 * 班级编号与名称响应
 */
@Data
public class ClassCodeNameResponse {

    /**
     * 班级编号
     */
    private String classCode;

    /**
     * 班级名称
     */
    private String className;
}
