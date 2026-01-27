package com.example.demo.pojo.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 学生实验步骤完成情况响应
 */
@Data
public class StudentProcedureCompletionResponse {

    /**
     * 学生用户名
     */
    private String studentUsername;

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
     * 步骤完成列表
     */
    private List<ProcedureCompletion> procedures;

    /**
     * 总进度
     */
    private String progress;

    /**
     * 总得分
     */
    private BigDecimal totalScore;

    /**
     * 步骤完成详情
     */
    @Data
    public static class ProcedureCompletion {
        /**
         * 步骤ID
         */
        private Long id;

        /**
         * 步骤序号
         */
        private Integer number;

        /**
         * 步骤类型（1-观看视频，2-数据收集，3-题库答题）
         */
        private Integer type;

        /**
         * 步骤描述
         */
        private String remark;

        /**
         * 是否已完成
         */
        private Boolean isCompleted;

        /**
         * 提交时间
         */
        private LocalDateTime submissionTime;

        /**
         * 得分
         */
        private BigDecimal score;

        /**
         * 步骤分数占比
         */
        private Integer proportion;
    }
}
