package com.example.demo.pojo.request.teacher;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 修改实验请求
 */
@Data
public class UpdateExperimentRequest {

    /**
     * 实验ID
     */
    private Long id;

    /**
     * 实验名称
     */
    private String experimentName;

    /**
     * 分数占比（%）
     */
    private Integer percentage;

    /**
     * 实验结束填写时间
     */
    private LocalDateTime endTime;
}
