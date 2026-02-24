package com.example.demo.pojo.dto.mapvo;

import com.example.demo.pojo.response.CourseExperimentsDetail;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 课程实验映射项
 * 用于替代 Map<String, CourseExperimentsDetail> 结构
 * Key: 课程代码，Value: 实验详情
 */
@Data
public class CourseExperimentItem {

    /**
     * 课程���码
     */
    private String courseCode;

    /**
     * 实验详情
     */
    private CourseExperimentsDetail detail;

    /**
     * 将 List<CourseExperimentItem> 转换为 Map<String, CourseExperimentsDetail>
     *
     * @param items 课程实验映射项列表
     * @return Map<课程代码, 实验详情>
     */
    public static Map<String, CourseExperimentsDetail> toMap(List<CourseExperimentItem> items) {
        if (items == null || items.isEmpty()) {
            return Map.of();
        }
        return items.stream()
                .collect(Collectors.toMap(CourseExperimentItem::getCourseCode, CourseExperimentItem::getDetail));
    }

    /**
     * 将 Map<String, CourseExperimentsDetail> 转换为 List<CourseExperimentItem>
     *
     * @param map Map<课程代码, 实验详情>
     * @return 课程实验映射项列表
     */
    public static List<CourseExperimentItem> fromMap(Map<String, CourseExperimentsDetail> map) {
        if (map == null || map.isEmpty()) {
            return new ArrayList<>();
        }
        return map.entrySet().stream()
                .map(entry -> {
                    CourseExperimentItem item = new CourseExperimentItem();
                    item.setCourseCode(entry.getKey());
                    item.setDetail(entry.getValue());
                    return item;
                })
                .collect(Collectors.toList());
    }
}
