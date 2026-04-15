package com.example.demo.pojo.dto.mapvo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 表格列级误差
 * 用于替代 Map<String, Double> 结构（列级误差映射）
 * Key: 列名，Value: 误差百分比
 */
@Data
public class ColumnTolerance {

    /**
     * 列名（如 "A"、"B"）
     */
    private String columnName;

    /**
     * 列级误差百分比（单位：%）
     * 示例：5 表示允许±5%的相对误差
     */
    private Double tolerance;

    /**
     * 将 List<ColumnTolerance> 转换为 Map<String, Double>
     *
     * @param list 列级误差列表
     * @return Map<列名, 误差百分比>
     */
    public static Map<String, Double> toMap(List<ColumnTolerance> list) {
        if (list == null || list.isEmpty()) {
            return Map.of();
        }
        return list.stream()
                .filter(ct -> ct.getTolerance() != null)
                .collect(Collectors.toMap(ColumnTolerance::getColumnName, ColumnTolerance::getTolerance));
    }

    /**
     * 将 Map<String, Double> 转换为 List<ColumnTolerance>
     *
     * @param map Map<列名, 误差百分比>
     * @return 列级误差列表
     */
    public static List<ColumnTolerance> fromMap(Map<String, Double> map) {
        if (map == null || map.isEmpty()) {
            return new ArrayList<>();
        }
        return map.entrySet().stream()
                .map(entry -> {
                    ColumnTolerance ct = new ColumnTolerance();
                    ct.setColumnName(entry.getKey());
                    ct.setTolerance(entry.getValue());
                    return ct;
                })
                .collect(Collectors.toList());
    }
}
