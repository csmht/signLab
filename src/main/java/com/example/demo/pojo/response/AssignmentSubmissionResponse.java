package com.example.demo.pojo.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 作业提交响应
 */
@Data
public class AssignmentSubmissionResponse {

    /**
     * 作业ID
     */
    private Long id;

    /**
     * 课程ID
     */
    private String courseId;

    /**
     * 实验ID
     */
    private String experimentId;

    /**
     * 学生用户名
     */
    private String studentUsername;

    /**
     * 学生姓名
     */
    private String studentName;

    /**
     * 提交类型（实验报告、数据文件等）
     */
    private String submissionType;

    /**
     * 文件名（原始文件名）
     */
    private String fileName;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 提交状态（draft-草稿，submitted-已提交，graded-已批改）
     */
    private String submissionStatus;

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
}
