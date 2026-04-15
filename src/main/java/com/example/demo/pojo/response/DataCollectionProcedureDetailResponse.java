package com.example.demo.pojo.response;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 数据收集步骤详情响应（未提交）
 * 步骤类型：type=2（数据收集）
 */
@Data
public class DataCollectionProcedureDetailResponse {

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
     * 数据收集详情
     */
    private DataCollectionDetail dataCollectionDetail;

    /**
     * 数据收集详情
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class DataCollectionDetail extends BaseDataCollectionDetailResponse {
    }
}
