package com.example.demo.pojo.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 签到列表响应DTO
 * 包含三个列表：非跨班签到、跨班签到、未签到
 */
@Data
public class AttendanceListResponse {

    /**
     * 非跨班签到学生列表
     */
    private List<StudentAttendanceInfo> normalAttendanceList;

    /**
     * 跨班签到学生列表
     */
    private List<StudentAttendanceInfo> crossClassAttendanceList;

    /**
     * 未签到学生列表
     */
    private List<StudentAttendanceInfo> notAttendanceList;

    /**
     * 学生签到信息
     */
    @Data
    public static class StudentAttendanceInfo {

        /**
         * 签到记录ID（未签到学生为null）
         */
        private Long attendanceId;

        /**
         * 学生用户名（学号）
         */
        private String studentUsername;

        /**
         * 学生姓名
         */
        private String studentName;

        /**
         * 学生所在班级名称
         */
        private String className;

        /**
         * 签到状态
         */
        private String attendanceStatus;

        /**
         * 签到时间
         */
        private LocalDateTime attendanceTime;
    }
}