package com.example.demo.pojo.request;

import lombok.Data;

/**
 * 班级学生步骤详情查询请求
 * 用于查询指定班级中所有学生的步骤详情
 */
@Data
public class ClassProcedureDetailRequest {

    /**
     * 班级编号（必填）
     */
    private String classCode;

    /**
     * 课程ID（必填）
     */
    private String courseId;

    /**
     * 实验ID（必填）
     */
    private Long experimentId;

    /**
     * 步骤ID（必填）
     */
    private Long procedureId;

    /**
     * 学生用户名（可选，用于筛选特定学生）
     */
    private String studentUsername;
}
