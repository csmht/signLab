package com.example.demo.pojo.dto.mapvo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 表格单元格答案
 * 使用 rowIndex + columnIndex 定位单元格（从 0 开始）
 */
@Data
public class TableCellAnswer {

    /** 行索引（从 0 开始，对应 tableRowHeaders 数组下标） */
    private Integer rowIndex;

    /** 列索引（从 0 开始，对应 tableColumnHeaders 数组下标） */
    private Integer columnIndex;

    /** 答案值 */
    private String value;

    /** 单元格级误差百分比（可选，覆盖列级和步骤级误差，单位：%） */
    private Double tolerance;

    /**
     * 获取位置标识 key（格式: "rowIndex-columnIndex"，如 "0-0"）
     */
    public String getPositionKey() {
        return rowIndex + "-" + columnIndex;
    }

    /**
     * 将 List<TableCellAnswer> 转换为 Map<String, String>
     * key 格式: "rowIndex-columnIndex"
     */
    public static Map<String, String> toMap(List<TableCellAnswer> answers) {
        if (answers == null || answers.isEmpty()) {
            return Map.of();
        }
        return answers.stream()
                .collect(Collectors.toMap(TableCellAnswer::getPositionKey, TableCellAnswer::getValue));
    }

    /**
     * 将 List<TableCellAnswer> 转换为 Map<String, Double>（单元格级误差映射）
     * key 格式: "rowIndex-columnIndex"
     */
    public static Map<String, Double> toToleranceMap(List<TableCellAnswer> answers) {
        if (answers == null || answers.isEmpty()) {
            return Map.of();
        }
        return answers.stream()
                .filter(answer -> answer.getTolerance() != null)
                .collect(Collectors.toMap(TableCellAnswer::getPositionKey, TableCellAnswer::getTolerance));
    }

    /**
     * 将 Map<String, String> 转换为 List<TableCellAnswer>
     * key 格式: "rowIndex-columnIndex"
     */
    public static List<TableCellAnswer> fromMap(Map<String, String> map) {
        return fromMap(map, null);
    }

    /**
     * 将 Map<String, String> 和误差映射转换为 List<TableCellAnswer>
     * key 格式: "rowIndex-columnIndex"
     */
    public static List<TableCellAnswer> fromMap(Map<String, String> map, Map<String, Double> toleranceMap) {
        if (map == null || map.isEmpty()) {
            return new ArrayList<>();
        }
        return map.entrySet().stream()
                .map(entry -> {
                    TableCellAnswer answer = new TableCellAnswer();
                    String[] parts = entry.getKey().split("-");
                    answer.setRowIndex(Integer.parseInt(parts[0]));
                    answer.setColumnIndex(Integer.parseInt(parts[1]));
                    answer.setValue(entry.getValue());
                    if (toleranceMap != null) {
                        answer.setTolerance(toleranceMap.get(entry.getKey()));
                    }
                    return answer;
                })
                .collect(Collectors.toList());
    }
}
