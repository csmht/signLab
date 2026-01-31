package com.example.demo.util;

import com.example.demo.pojo.entity.ClassExperiment;
import com.example.demo.pojo.entity.ExperimentalProcedure;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

/**
 * 步骤时间计算工具类
 * 根据班级实验的开始时间和步骤的偏移量、持续时间计算步骤的实际开始和结束时间
 */
@Slf4j
public class ProcedureTimeCalculator {

    /**
     * 计算步骤开始时间
     *
     * @param experimentStartTime 实验开始时间
     * @param offsetMinutes      偏移时间(分钟),可为负数
     * @return 步骤开始时间
     */
    public static LocalDateTime calculateStartTime(LocalDateTime experimentStartTime, Integer offsetMinutes) {
        if (experimentStartTime == null) {
            log.warn("实验开始时间为空,无法计算步骤开始时间");
            return null;
        }
        if (offsetMinutes == null) {
            offsetMinutes = 0; // 默认值为0
        }
        return experimentStartTime.plusMinutes(offsetMinutes);
    }

    /**
     * 计算步骤结束时间
     *
     * @param startTime      步骤开始时间
     * @param durationMinutes 持续时间(分钟),必须为正数
     * @return 步骤结束时间
     */
    public static LocalDateTime calculateEndTime(LocalDateTime startTime, Integer durationMinutes) {
        if (startTime == null) {
            log.warn("步骤开始时间为空,无法计算步骤结束时间");
            return null;
        }
        if (durationMinutes == null || durationMinutes <= 0) {
            log.warn("持续时间无效: {}, 使用默认值60分钟", durationMinutes);
            durationMinutes = 60; // 默认值为60分钟
        }
        return startTime.plusMinutes(durationMinutes);
    }

    /**
     * 计算步骤完整时间范围
     *
     * @param classExperiment 班级实验
     * @param procedure       实验步骤
     * @return 时间范围对象
     */
    public static ProcedureTimeRange calculateTimeRange(
            ClassExperiment classExperiment,
            ExperimentalProcedure procedure) {

        if (classExperiment == null || procedure == null) {
            log.warn("班级实验或步骤为空,无法计算时间范围");
            return new ProcedureTimeRange(null, null);
        }

        LocalDateTime startTime = calculateStartTime(
                classExperiment.getStartTime(),
                procedure.getOffsetMinutes()
        );

        LocalDateTime endTime = calculateEndTime(
                startTime,
                procedure.getDurationMinutes()
        );

        return new ProcedureTimeRange(startTime, endTime);
    }

    /**
     * 时间范围对象
     */
    public static class ProcedureTimeRange {
        private final LocalDateTime startTime;
        private final LocalDateTime endTime;

        public ProcedureTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
            this.startTime = startTime;
            this.endTime = endTime;
        }

        public LocalDateTime getStartTime() {
            return startTime;
        }

        public LocalDateTime getEndTime() {
            return endTime;
        }

        public boolean isValid() {
            return startTime != null && endTime != null;
        }
    }
}
