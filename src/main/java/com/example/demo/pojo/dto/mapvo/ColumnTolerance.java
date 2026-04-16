package com.example.demo.pojo.dto.mapvo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 表格列级误差
 * 使用 columnIndex 定位列（从 0 开始，对应 tableColumnHeaders 数组下标）
 */
@Data
public class ColumnTolerance {

    /** 列索引（从 0 开始，对应 tableColumnHeaders 数组下标） */
    private Integer columnIndex;

    /** 列级误差百分比（单位：%），示例：5 表示允许±5%的相对误差 */
    private Double tolerance;

    /**
     * 将 List<ColumnTolerance> 转换为 Map<String, Double>
     * key 格式: 列索引字符串（如 "0", "1"）
     */
    public static Map<String, Double> toMap(List<ColumnTolerance> list) {
        if (list == null || list.isEmpty()) {
            return Map.of();
        }
        return list.stream()
                .filter(ct -> ct.getTolerance() != null)
                .collect(Collectors.toMap(ct -> String.valueOf(ct.getColumnIndex()), ColumnTolerance::getTolerance));
    }

    /**
     * 将 Map<String, Double> 转换为 List<ColumnTolerance>
     * key 格式: 列索引字符串（如 "0", "1"）
     */
    public static List<ColumnTolerance> fromMap(Map<String, Double> map) {
        if (map == null || map.isEmpty()) {
            return new ArrayList<>();
        }
        return map.entrySet().stream()
                .map(entry -> {
                    ColumnTolerance ct = new ColumnTolerance();
                    ct.setColumnIndex(Integer.parseInt(entry.getKey()));
                    ct.setTolerance(entry.getValue());
                    return ct;
                })
                .collect(Collectors.toList());
    }
}
