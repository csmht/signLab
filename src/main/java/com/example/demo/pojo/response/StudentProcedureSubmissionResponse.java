package com.example.demo.pojo.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 学生步骤提交响应
 * 基于 StudentExperimentalProcedure 实体
 */
@Data
public class StudentProcedureSubmissionResponse {

    /**
     * 提交记录ID
     */
    private Long id;

    /**
     * 实验ID
     */
    private Long experimentId;

    /**
     * 实验步骤ID
     */
    private Long procedureId;

    /**
     * 步骤序号
     */
    private Integer number;

    /**
     * 学生用户名
     */
    private String studentUsername;

    /**
     * 学生姓名
     */
    private String studentName;

    /**
     * 班级编号
     */
    private String classCode;

    /**
     * 提交状态（0-未评分，1-教师人工评分，2-系统自动评分）
     */
    private Integer submissionStatus;

    /**
     * 教师评语
     */
    private String teacherComment;

    /**
     * 评分
     */
    private BigDecimal score;

    /**
     * 提交时间
     */
    private LocalDateTime submissionTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
