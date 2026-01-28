package com.example.demo.pojo.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 班级实验完成统计响应
 */
@Data
public class ClassExperimentStatisticsResponse {

    /**
     * 班级编号
     */
    private String classCode;

    /**
     * 实验ID
     */
    private Long experimentId;

    /**
     * 实验名称
     */
    private String experimentName;

    /**
     * 授课老师用户名
     */
    private String userName;

    /**
     * 班级总人数
     */
    private Integer totalStudents;

    /**
     * 已提交人数
     */
    private Integer submittedCount;

    /**
     * 完成率
     */
    private BigDecimal completionRate;

    /**
     * 平均分
     */
    private BigDecimal averageScore;

    /**
     * 步骤统计列表
     */
    private List<ProcedureStatistics> procedureStatistics;

    /**
     * 学生完成列表
     */
    private List<StudentCompletionInfo> studentCompletions;

    /**
     * 步骤统计
     */
    @Data
    public static class ProcedureStatistics {
        /**
         * 步骤ID
         */
        private Long id;

        /**
         * 步骤序号
         */
        private Integer number;

        /**
         * 步骤类型
         */
        private Integer type;

        /**
         * 步骤描述
         */
        private String remark;

        /**
         * 完成人数
         */
        private Integer completedCount;

        /**
         * 未完成人数
         */
        private Integer notCompletedCount;

        /**
         * 完成率
         */
        private BigDecimal completionRate;

        /**
         * 平均分
         */
        private BigDecimal averageScore;
    }

    /**
     * 学生完成信息
     */
    @Data
    public static class StudentCompletionInfo {
        /**
         * 学生用户名
         */
        private String studentUsername;

        /**
         * 学生姓名（如果有的话）
         */
        private String studentName;

        /**
         * 已完成步骤数
         */
        private Integer completedCount;

        /**
         * 总步骤数
         */
        private Integer totalCount;

        /**
         * 进度
         */
        private String progress;

        /**
         * 总得分
         */
        private BigDecimal totalScore;

        /**
         * 提交时间（最后提交时间）
         */
        private LocalDateTime lastSubmissionTime;
    }
}
