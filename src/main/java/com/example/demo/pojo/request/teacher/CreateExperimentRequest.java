package com.example.demo.pojo.request.teacher;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 创建实验请求
 */
@Data
public class CreateExperimentRequest {

    /**
     * 课程ID
     */
    private String courseId;

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
