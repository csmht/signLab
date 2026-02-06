package com.example.demo.pojo.request;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 批量绑定班级到实验请求DTO
 */
@Data
public class BatchBindClassesToExperimentRequest {

    /**
     * 课程ID
     */
    private String courseId;

    /**
     * 实验ID
     */
    private String experimentId;

    /**
     * 班级编号列表
     */
    private List<String> classCodes;

    /**
     * 上课时间（例如：2025年8月6日 8:00-14:00）
     */
    private String courseTime;

    /**
     * 实验开始时间
     */
    private LocalDateTime startTime;

    /**
     * 实验结束填写时间
     */
    private LocalDateTime endTime;

    /**
     * 实验地点
     */
    private String experimentLocation;

    /**
     * 授课老师用户名
     */
    private String userName;

}