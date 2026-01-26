package com.example.demo.pojo.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 学生实验步骤详情响应
 */
@Data
public class StudentProcedureDetailResponse {

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
     * 是否可跳过
     */
    private Boolean isSkip;

    /**
     * 步骤分数占比
     */
    private Integer proportion;

    /**
     * 视频ID（类型1时有效）
     */
    private Long videoId;

    /**
     * 数据收集ID（类型2时有效）
     */
    private Long dataCollectionId;

    /**
     * 题库详情ID（类型3时有效）
     */
    private Long procedureTopicId;

    /**
     * 步骤开始时间
     */
    private LocalDateTime startTime;

    /**
     * 步骤结束时间
     */
    private LocalDateTime endTime;

    // ===== 学生完成状态相关 =====

    /**
     * 是否已完成
     */
    private Boolean isCompleted;

    /**
     * 答案内容
     */
    private String answer;

    /**
     * 得分
     */
    private BigDecimal score;

    /**
     * 提交时间
     */
    private LocalDateTime submissionTime;

    // ===== 可访问性相关 =====

    /**
     * 是否可做
     */
    private Boolean isAccessible;

    /**
     * 不可做原因（当 isAccessible 为 false 时有效）
     */
    private String inaccessibleReason;

    /**
     * 前置步骤是否完成
     */
    private Boolean isPreviousCompleted;
}
