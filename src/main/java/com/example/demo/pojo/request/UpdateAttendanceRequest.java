package com.example.demo.pojo.request;

import lombok.Data;

/**
 * 修改签到状态请求DTO
 */
@Data
public class UpdateAttendanceRequest {

    /**
     * 班级实验ID
     */
    private Long classExperimentId;

    /**
     * 学生用户名（学号）
     */
    private String studentUsername;

    /**
     * 新的签到状态：1-正常，2-补签，3-迟到，4-跨班签到
     */
    private Integer attendanceStatus;
}