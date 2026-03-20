package com.example.demo.pojo.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 教师数据收集步骤详情响应
 * 步骤类型：type=2（数据收集）
 */
@Data
public class TeacherDataCollectionProcedureDetailResponse {

    /**
     * 步骤ID
     */
    private Long id;

    /**
     * 实验ID
     */
    private Long experimentId;

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
     * 是否可跳过
     */
    private Boolean isSkip;

    /**
     * 步骤分数占比
     */
    private Integer proportion;

    /**
     * 步骤开始时间偏移量(分钟)
     */
    private Integer offsetMinutes;

    /**
     * 步骤持续时间(分钟)
     */
    private Integer durationMinutes;

    /**
     * 数据收集ID
     */
    private Long dataCollectionId;

    /**
     * 数据收集类型（1-关键数据，2-表格数据）
     */
    private Long dataCollectionType;

    /**
     * 数据描述
     */
    private String dataRemark;

    /**
     * 是否需要提交照片
     */
    private Boolean dataNeedPhoto;

    /**
     * 是否需要提交文档
     */
    private Boolean dataNeedDoc;
}
