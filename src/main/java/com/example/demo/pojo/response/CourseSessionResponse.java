package com.example.demo.pojo.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课次信息响应
 */
@Data
public class CourseSessionResponse {

    /**
     * 班级实验ID
     */
    private Long classExperimentId;

    /**
     * 班级编号
     */
    private String classCode;

    /**
     * 班级名称
     */
    private String className;

    /**
     * 课程ID
     */
    private String courseId;

    /**
     * 实验ID
     */
    private Long experimentId;

    /**
     * 实验名称
     */
    private String experimentName;

    /**
     * 上课时间（例如：8:00-14:00）
     */
    private String courseTime;

    /**
     * 实验开始时间
     */
    private LocalDateTime startTime;

    /**
     * 实验结束时间
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
