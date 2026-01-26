package com.example.demo.pojo.request.teacher;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 创建数据收集步骤请求
 */
@Data
public class CreateDataCollectionProcedureRequest {

    /**
     * 实验ID
     */
    private Long experimentId;

    /**
     * 步骤序号
     */
    private Integer number;

    /**
     * 是否可跳过
     */
    private Boolean isSkip;

    /**
     * 步骤分数占比
     */
    private Integer proportion;

    /**
     * 步骤描述
     */
    private String remark;

    /**
     * 数据类型（1-关键数据，2-表格数据）
     */
    private Integer dataType;

    /**
     * 数据描述
     * 类型为1时，按照：需求数据1$需求数据2$的格式
     * 类型为2时，按照：x&表头1$列表头2#y&横表头1$横表头2$横表头3 格式储存
     */
    private String dataRemark;

    /**
     * 是否需要提交照片
     */
    private Boolean needPhoto;

    /**
     * 是否需要提交文档
     */
    private Boolean needDoc;

    /**
     * 步骤开始时间
     */
    private LocalDateTime startTime;

    /**
     * 步骤结束时间
     */
    private LocalDateTime endTime;
}
