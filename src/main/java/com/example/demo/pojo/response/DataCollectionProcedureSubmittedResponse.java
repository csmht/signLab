package com.example.demo.pojo.response;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 数据收集步骤详情响应（已提交）
 * 步骤类型：type=2（数据收集）
 */
@Data
public class DataCollectionProcedureSubmittedResponse {

    /**
     * 步骤ID
     */
    private Long id;

    /**
     * 步骤序号
     */
    private Integer number;

    /**
     * 步骤类型（2-数据收集）
     */
    private Integer type;

    /**
     * 步骤描述
     */
    private String remark;

    /**
     * 步骤分数占比
     */
    private Integer proportion;

    /**
     * 是否可修改
     * true-可以修改，false-不可修改
     */
    private Boolean isModifiable;

    /**
     * 不可修改的原因（当isModifiable为false时返回）
     */
    private String notModifiableReason;

    /**
     * 提交时间
     */
    private LocalDateTime submissionTime;

    /**
     * 得分
     */
    private BigDecimal score;

    /**
     * 教师评语
     */
    private String teacherComment;

    /**
     * 评分状态（0-未评分，1-教师人工评分，2-系统自动评分）
     */
    private Integer isGraded;

    /**
     * 是否已过答题时间
     * 如果true，表示当前时间已超过步骤答题时间，可以显示正确答案
     * 如果false，表示还在答题时间内，不显示正确答案
     */
    private Boolean isAfterEndTime;

    /**
     * 数据收集详情
     */
    private DataCollectionDetail dataCollectionDetail;

    /**
     * 数据收集详情
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class DataCollectionDetail extends BaseSubmittedDataCollectionDetailResponse {
    }

    /**
     * 附件信息
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class AttachmentInfo extends BaseSubmittedDataCollectionDetailResponse.AttachmentInfo {
    }
}
