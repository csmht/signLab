package com.example.demo.pojo.dto.mapvo;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 学生签到统计
 * 用于替代 Map<String, Object> 结构（签到统计）
 */
@Data
public class StudentAttendanceStats {

    /**
     * 总签到次数
     */
    private Integer totalAttendance;

    /**
     * 正常签到次数
     */
    private Long normalAttendance;

    /**
     * 迟到次数
     */
    private Long lateAttendance;

    /**
     * 跨班签到次数
     */
    private Long crossClassAttendance;

    /**
     * 补签次数
     */
    private Long makeupAttendance;

    /**
     * 签到率
     */
    private Double attendanceRate;

    /**
     * 将 StudentAttendanceStats 转换为 Map<String, Object>
     *
     * @param stats 签到统计对象
     * @return Map<String, Object>
     */
    public static Map<String, Object> toMap(StudentAttendanceStats stats) {
        if (stats == null) {
            return new HashMap<>();
        }
        Map<String, Object> map = new HashMap<>();
        map.put("totalAttendance", stats.getTotalAttendance());
        map.put("normalAttendance", stats.getNormalAttendance());
        map.put("lateAttendance", stats.getLateAttendance());
        map.put("crossClassAttendance", stats.getCrossClassAttendance());
        map.put("makeupAttendance", stats.getMakeupAttendance());
        map.put("attendanceRate", stats.getAttendanceRate());
        return map;
    }

    /**
     * 从 Map<String, Object> 创建 StudentAttendanceStats
     *
     * @param map Map<String, Object>
     * @return 签到统计对象
     */
    public static StudentAttendanceStats fromMap(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        StudentAttendanceStats stats = new StudentAttendanceStats();
        if (map.get("totalAttendance") != null) {
            stats.setTotalAttendance(((Number) map.get("totalAttendance")).intValue());
        }
        if (map.get("normalAttendance") != null) {
            stats.setNormalAttendance(((Number) map.get("normalAttendance")).longValue());
        }
        if (map.get("lateAttendance") != null) {
            stats.setLateAttendance(((Number) map.get("lateAttendance")).longValue());
        }
        if (map.get("crossClassAttendance") != null) {
            stats.setCrossClassAttendance(((Number) map.get("crossClassAttendance")).longValue());
        }
        if (map.get("makeupAttendance") != null) {
            stats.setMakeupAttendance(((Number) map.get("makeupAttendance")).longValue());
        }
        if (map.get("attendanceRate") != null) {
            stats.setAttendanceRate(((Number) map.get("attendanceRate")).doubleValue());
        }
        return stats;
    }
}
