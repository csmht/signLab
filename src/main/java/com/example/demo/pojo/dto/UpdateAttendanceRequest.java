package com.example.demo.pojo.dto;

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
     * 新的签到状态
     */
    private String attendanceStatus;
}