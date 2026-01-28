package com.example.demo.pojo.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 学生实验详情响应
 */
@Data
public class StudentExperimentDetailResponse {

    /**
     * 实验ID
     */
    private Long experimentId;

    /**
     * 实验名称
     */
    private String experimentName;

    /**
     * 课程ID
     */
    private String courseId;

    /**
     * 班级编号
     */
    private String classCode;

    /**
     * 上课时间
     */
    private String courseTime;

    /**
     * 实验地点
     */
    private String experimentLocation;

    /**
     * 实验开始时间
     */
    private LocalDateTime startTime;

    /**
     * 实验结束时间
     */
    private LocalDateTime endTime;

    /**
     * 授课老师用户名
     */
    private String userName;

    /**
     * 步骤列表
     */
    private List<StudentProcedureDetailResponse> procedures;

    /**
     * 实验总进度（已完步骤数 / 总步骤数）
     */
    private String progress;
}
