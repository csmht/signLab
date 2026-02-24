package com.example.demo.pojo.dto.mapvo;

import com.example.demo.pojo.vo.ExperimentGradeResult;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 实验成绩结果项
 * 用于替代 Map<Long, ExperimentGradeResult> 结构
 * Key: 实验ID，Value: 成绩结果
 */
@Data
public class ExperimentResultItem {

    /**
     * 实验ID
     */
    private Long experimentId;

    /**
     * 成绩结果
     */
    private ExperimentGradeResult result;

    /**
     * 将 List<ExperimentResultItem> 转换为 Map<Long, ExperimentGradeResult>
     *
     * @param items 实验成绩结果项列表
     * @return Map<实验ID, 成绩结果>
     */
    public static Map<Long, ExperimentGradeResult> toMap(List<ExperimentResultItem> items) {
        if (items == null || items.isEmpty()) {
            return Map.of();
        }
        return items.stream()
                .collect(Collectors.toMap(ExperimentResultItem::getExperimentId, ExperimentResultItem::getResult));
    }

    /**
     * 将 Map<Long, ExperimentGradeResult> 转换为 List<ExperimentResultItem>
     *
     * @param map Map<实验ID, 成绩结果>
     * @return 实验成绩结果项列表
     */
    public static List<ExperimentResultItem> fromMap(Map<Long, ExperimentGradeResult> map) {
        if (map == null || map.isEmpty()) {
            return new ArrayList<>();
        }
        return map.entrySet().stream()
                .map(entry -> {
                    ExperimentResultItem item = new ExperimentResultItem();
                    item.setExperimentId(entry.getKey());
                    item.setResult(entry.getValue());
                    return item;
                })
                .collect(Collectors.toList());
    }
}
