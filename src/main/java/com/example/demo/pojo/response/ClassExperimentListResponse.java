package com.example.demo.pojo.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 教师端/管理员端班级实验列表响应
 * 不返回兼容单班级场景的 classCode、className 字段
 */
@Data
public class ClassExperimentListResponse {

    /**
     * 班级实验ID
     */
    private Long classExperimentId;

    /**
     * 班级编号列表（合班上课时返回多个）
     */
    private List<String> classCodes;

    /**
     * 班级名称列表（合班上课时返回多个）
     */
    private List<String> classNames;

    /**
     * 是否为合班上课
     */
    private Boolean isMergedClass;

    /**
     * 课程ID
     */
    private String courseId;

    /**
     * 课程名称
     */
    private String courseName;

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

    /**
     * 授课老师姓名
     */
    private String teacherName;
}
