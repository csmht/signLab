package com.example.demo.pojo.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 班级学生步骤详情响应
 * 用于返回指定班级中所有学生的步骤详情列表
 * @param <T> 具体步骤详情类型
 */
@Data
public class ClassStudentProcedureDetailResponse<T> {

    /**
     * 步骤ID
     */
    private Long procedureId;

    /**
     * 步骤序号
     */
    private Integer procedureNumber;

    /**
     * 步骤类型（1-观看视频，2-数据收集，3-题库答题，5-限时答题）
     */
    private Integer procedureType;

    /**
     * 步骤描述
     */
    private String procedureRemark;

    /**
     * 步骤分数占比
     */
    private Integer proportion;

    /**
     * 是否已过答题时间
     */
    private Boolean isAfterEndTime;

    /**
     * 学生详情列表
     */
    private List<StudentProcedureItem<T>> students;

    /**
     * 学生步骤详情项
     * @param <T> 具体步骤详情类型
     */
    @Data
    public static class StudentProcedureItem<T> {

        /**
         * 学生用户名（学号）
         */
        private String studentUsername;

        /**
         * 学生姓名
         */
        private String studentName;

        /**
         * 提交时间（已提交时有值）
         */
        private LocalDateTime submissionTime;

        /**
         * 得分（已提交时有值）
         */
        private BigDecimal score;

        /**
         * 教师评语
         */
        private String teacherComment;

        /**
         * 评分状态（0-未评分，1-教师人工评分，2-系统自动评分）
         * 已提交时有值
         */
        private Integer isGraded;

        /**
         * 具体步骤详情
         */
        private T detail;
    }
}
