package com.example.demo.pojo.request.teacher;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 批量设置步骤时间请求
 */
@Data
public class SetProcedureTimesBatchRequest {
    /** 班级实验ID */
    private Long classExperimentId;

    /** 步骤时间列表 */
    private List<ProcedureTimeItem> procedureTimes;

    /**
     * 步骤时间项
     */
    @Data
    public static class ProcedureTimeItem {
        /** 实验步骤ID */
        private Long procedureId;

        /** 步骤开始时间 */
        private LocalDateTime startTime;

        /** 步骤结束时间 */
        private LocalDateTime endTime;
    }
}
