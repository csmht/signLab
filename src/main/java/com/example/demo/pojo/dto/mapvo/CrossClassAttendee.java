package com.example.demo.pojo.dto.mapvo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 跨班签到学生
 * 用于替代 Map<String, Object> 结构（跨班签到学生信息）
 */
@Data
public class CrossClassAttendee {

    /**
     * 学号
     */
    private String studentCode;

    /**
     * 学生姓名
     */
    private String studentName;

    /**
     * 学生实际班级编号
     */
    private String studentActualClassCode;

    /**
     * 班级名称
     */
    private String className;

    /**
     * 签到时间
     */
    private LocalDateTime attendanceTime;

    /**
     * 最后签到时间
     */
    private LocalDateTime lastAttendanceTime;

    /**
     * 学生类型
     */
    private String studentType;

    /**
     * 将 CrossClassAttendee 转换为 Map<String, Object>
     *
     * @param attendee 跨班签到学生对象
     * @return Map<String, Object>
     */
    public static Map<String, Object> toMap(CrossClassAttendee attendee) {
        if (attendee == null) {
            return new HashMap<>();
        }
        Map<String, Object> map = new HashMap<>();
        map.put("studentCode", attendee.getStudentCode());
        map.put("studentName", attendee.getStudentName());
        map.put("studentActualClassCode", attendee.getStudentActualClassCode());
        map.put("className", attendee.getClassName());
        map.put("attendanceTime", attendee.getAttendanceTime());
        map.put("lastAttendanceTime", attendee.getLastAttendanceTime());
        map.put("studentType", attendee.getStudentType());
        return map;
    }

    /**
     * 将 List<CrossClassAttendee> 转换为 List<Map<String, Object>>
     *
     * @param attendees 跨班签到学生列表
     * @return List<Map<String, Object>>
     */
    public static List<Map<String, Object>> toMapList(List<CrossClassAttendee> attendees) {
        if (attendees == null || attendees.isEmpty()) {
            return new ArrayList<>();
        }
        return attendees.stream()
                .map(CrossClassAttendee::toMap)
                .collect(Collectors.toList());
    }

    /**
     * 从 Map<String, Object> 创建 CrossClassAttendee
     *
     * @param map Map<String, Object>
     * @return 跨班签到学生对象
     */
    public static CrossClassAttendee fromMap(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        CrossClassAttendee attendee = new CrossClassAttendee();
        attendee.setStudentCode((String) map.get("studentCode"));
        attendee.setStudentName((String) map.get("studentName"));
        attendee.setStudentActualClassCode((String) map.get("studentActualClassCode"));
        attendee.setClassName((String) map.get("className"));
        attendee.setAttendanceTime((LocalDateTime) map.get("attendanceTime"));
        attendee.setLastAttendanceTime((LocalDateTime) map.get("lastAttendanceTime"));
        attendee.setStudentType((String) map.get("studentType"));
        return attendee;
    }

    /**
     * 从 List<Map<String, Object>> 创建 List<CrossClassAttendee>
     *
     * @param mapList List<Map<String, Object>>
     * @return 跨班签到学生列表
     */
    public static List<CrossClassAttendee> fromMapList(List<Map<String, Object>> mapList) {
        if (mapList == null || mapList.isEmpty()) {
            return new ArrayList<>();
        }
        return mapList.stream()
                .map(CrossClassAttendee::fromMap)
                .collect(Collectors.toList());
    }
}
