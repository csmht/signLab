package com.example.demo.pojo.request.teacher;

import lombok.Data;

/**
 * 插入视频观看步骤请求
 */
@Data
public class InsertVideoProcedureRequest {

    /**
     * 实验ID
     */
    private Long experimentId;

    /**
     * 插入位置（在该步骤序号后插入）
     */
    private Integer afterNumber;

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
     * 视频ID
     */
    private Long videoId;
}
