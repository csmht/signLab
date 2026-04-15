package com.example.demo.pojo.response;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 学生数据收集步骤详情响应
 * 用于教师查询学生的数据收集步骤详情
 */
@Data
public class StudentDataCollectionProcedureDetailResponse {

    /**
     * 步骤ID
     */
    private Long id;

    /**
     * 步骤序号
     */
    private Integer number;

    /**
     * 步骤描述
     */
    private String remark;

    /**
     * 步骤分数占比
     */
    private Integer proportion;

    /**
     * 数据收集详情
     */
    private DataCollectionDetail dataCollectionDetail;

    /**
     * 提交时间
     */
    private LocalDateTime submissionTime;

    /**
     * 得分
     */
    private java.math.BigDecimal score;

    /**
     * 教师评语
     */
    private String teacherComment;

    /**
     * 评分状态（0-未评分，1-教师人工评分，2-系统自动评分）
     */
    private Integer isGraded;

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
