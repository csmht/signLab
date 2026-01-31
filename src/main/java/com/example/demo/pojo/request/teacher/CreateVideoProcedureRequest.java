package com.example.demo.pojo.request.teacher;

import lombok.Data;

/**
 * 创建视频观看步骤请求
 */
@Data
public class CreateVideoProcedureRequest {

    /**
     * 实验ID
     */
    private Long experimentId;

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

    /**
     * 步骤开始时间偏移量(分钟),默认为0
     */
    private Integer offsetMinutes;

    /**
     * 步骤持续时间(分钟)
     */
    private Integer durationMinutes;
}
