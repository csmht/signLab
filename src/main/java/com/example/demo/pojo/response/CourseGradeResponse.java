package com.example.demo.pojo.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 课程成绩响应
 */
@Data
public class CourseGradeResponse {

    /**
     * 成绩ID
     */
    private Long id;

    /**
     * 学生学号
     */
    private String studentUsername;

    /**
     * 学生姓名
     */
    private String studentName;

    /**
     * 课程ID
     */
    private String courseId;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 成绩（字母等级或具体分数）
     */
    private String grade;

    /**
     * 数字成绩
     */
    private BigDecimal gradeNumeric;

    /**
     * 成绩类型
     */
    private String gradeType;

    /**
     * 满分值
     */
    private BigDecimal maxScore;

    /**
     * 打分教师用户名
     */
    private String teacherUsername;

    /**
     * 教师姓名
     */
    private String teacherName;

    /**
     * 教师评语
     */
    private String teacherComment;

    /**
     * 成绩打分时间
     */
    private LocalDateTime gradeTime;

    /**
     * 学期信息
     */
    private String semester;

    /**
     * 是否已审核
     */
    private Boolean isApproved;

    /**
     * 审核人用户名
     */
    private String approvedBy;

    /**
     * 审核时间
     */
    private LocalDateTime approvedTime;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
}
