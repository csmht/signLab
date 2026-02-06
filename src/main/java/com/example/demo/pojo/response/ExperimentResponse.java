package com.example.demo.pojo.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 实验响应
 */
@Data
public class ExperimentResponse {

    /**
     * 实验ID
     */
    private Long id;

    /**
     * 课程ID
     */
    private String courseId;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 实验名称
     */
    private String experimentName;

    /**
     * 分数占比（%）
     */
    private Integer percentage;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 实验结束填写时间
     */
    private LocalDateTime endTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
}
