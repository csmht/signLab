package com.example.demo.pojo.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 签到响应DTO
 * 包含签到结果信息
 */
@Data
public class AttendanceResponse {

    /**
     * 签到是否成功
     */
    private Boolean success;

    /**
     * 签到状态：1-正常，2-补签，3-迟到，4-跨班签到
     */
    private Integer attendanceStatus;

    /**
     * 签到时间
     */
    private LocalDateTime attendanceTime;

    /**
     * 班级代码
     */
    private String classCode;

    /**
     * 实验ID
     */
    private String experimentId;

    /**
     * 课程ID
     */
    private String courseId;

    /**
     * 消息提示
     */
    private String message;
}