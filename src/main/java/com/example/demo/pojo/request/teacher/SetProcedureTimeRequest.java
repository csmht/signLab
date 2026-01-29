package com.example.demo.pojo.request.teacher;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 设置步骤时间请求
 */
@Data
public class SetProcedureTimeRequest {
    /** 班级实验ID */
    private Long classExperimentId;

    /** 实验步骤ID */
    private Long procedureId;

    /** 步骤开始时间 */
    private LocalDateTime startTime;

    /** 步骤结束时间 */
    private LocalDateTime endTime;
}
