package com.example.demo.pojo.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 步骤时间响应
 */
@Data
public class ProcedureTimeResponse {
    /** 实验步骤ID */
    private Long procedureId;

    /** 步骤序号 */
    private Integer procedureNumber;

    /** 步骤描述 */
    private String procedureRemark;

    /** 步骤开始时间 */
    private LocalDateTime startTime;

    /** 步骤结束时间 */
    private LocalDateTime endTime;
}
