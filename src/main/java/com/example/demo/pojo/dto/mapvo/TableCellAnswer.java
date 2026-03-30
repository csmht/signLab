package com.example.demo.pojo.dto.mapvo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 表格单元格答案
 * 用于替代 Map<String, String> 结构（表格答案）
 * Key: 单元格位置，Value: 答案值
 */
@Data
public class TableCellAnswer {

    /**
     * 单元格位置（如 A1, B2）
     */
    private String cellPosition;

    /**
     * 答案值
     */
    private String value;

    /**
     * 单元格级误差百分比（可选，覆盖步骤级误差，单位：%）
     * 示例：5 表示允许±5%的相对误差
     */
    private Double tolerance;

    /**
     * 将 List<TableCellAnswer> 转换为 Map<String, String>
     *
     * @param answers 答案列表
     * @return Map<单元格位置, 答案值>
     */
    public static Map<String, String> toMap(List<TableCellAnswer> answers) {
        if (answers == null || answers.isEmpty()) {
            return Map.of();
        }
        return answers.stream()
                .collect(Collectors.toMap(TableCellAnswer::getCellPosition, TableCellAnswer::getValue));
    }

    /**
     * 将 List<TableCellAnswer> 转换为 Map<String, Double>（单元格级误差映射）
     *
     * @param answers 答案列表
     * @return Map<单元格位置, 误差百分比>
     */
    public static Map<String, Double> toToleranceMap(List<TableCellAnswer> answers) {
        if (answers == null || answers.isEmpty()) {
            return Map.of();
        }
        return answers.stream()
                .filter(answer -> answer.getTolerance() != null)
                .collect(Collectors.toMap(TableCellAnswer::getCellPosition, TableCellAnswer::getTolerance));
    }

    /**
     * 将 Map<String, String> 转换为 List<TableCellAnswer>
     *
     * @param map Map<单元格位置, 答案值>
     * @return 答案列表
     */
    public static List<TableCellAnswer> fromMap(Map<String, String> map) {
        return fromMap(map, null);
    }

    /**
     * 将 Map<String, String> 和误差映射转换为 List<TableCellAnswer>
     *
     * @param map Map<单元格位置, 答案值>
     * @param toleranceMap Map<单元格位置, 误差百分比>
     * @return 答案列表
     */
    public static List<TableCellAnswer> fromMap(Map<String, String> map, Map<String, Double> toleranceMap) {
        if (map == null || map.isEmpty()) {
            return new ArrayList<>();
        }
        return map.entrySet().stream()
                .map(entry -> {
                    TableCellAnswer answer = new TableCellAnswer();
                    answer.setCellPosition(entry.getKey());
                    answer.setValue(entry.getValue());
                    if (toleranceMap != null) {
                        answer.setTolerance(toleranceMap.get(entry.getKey()));
                    }
                    return answer;
                })
                .collect(Collectors.toList());
    }
}
