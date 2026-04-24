package com.example.demo.pojo.request;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 更新课次请求
 */
@Data
public class UpdateClassExperimentRequest {

    /**
     * 上课时间
     */
    private String courseTime;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;
}
