package com.example.demo.pojo.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 班级实验查询请求（按班级）
 * 用于教师根据班级代码查询该班级关联的所有实验详情
 */
@Data
public class ClassExperimentQueryByClassRequest {

    /**
     * 班级代码
     */
    @NotBlank(message = "班级代码不能为空")
    private String classCode;
}
