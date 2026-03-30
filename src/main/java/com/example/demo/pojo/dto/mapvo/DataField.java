package com.example.demo.pojo.dto.mapvo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 数据收集字段
 * 用于替代 Map<String, String> 结构（数据收集字段）
 * Key: 字段名，Value: 字段值
 */
@Data
public class DataField {

    /**
     * 字段名
     */
    private String fieldName;

    /**
     * 字段值
     */
    private String value;

    /**
     * 字段级误差百分比（可选，覆盖步骤级误差，单位：%）
     * 示例：5 表示允许±5%的相对误差
     */
    private Double tolerance;

    /**
     * 将 List<DataField> 转换为 Map<String, String>
     *
     * @param fields 字段列表
     * @return Map<字段名, 字段值>
     */
    public static Map<String, String> toMap(List<DataField> fields) {
        if (fields == null || fields.isEmpty()) {
            return Map.of();
        }
        return fields.stream()
                .collect(Collectors.toMap(DataField::getFieldName, DataField::getValue));
    }

    /**
     * 将 List<DataField> 转换为 Map<String, Double>（字段级误差映射）
     *
     * @param fields 字段列表
     * @return Map<字段名, 误差百分比>
     */
    public static Map<String, Double> toToleranceMap(List<DataField> fields) {
        if (fields == null || fields.isEmpty()) {
            return Map.of();
        }
        return fields.stream()
                .filter(field -> field.getTolerance() != null)
                .collect(Collectors.toMap(DataField::getFieldName, DataField::getTolerance));
    }

    /**
     * 将 Map<String, String> 转换为 List<DataField>
     *
     * @param map Map<字段名, 字段值>
     * @return 字段列表
     */
    public static List<DataField> fromMap(Map<String, String> map) {
        return fromMap(map, null);
    }

    /**
     * 将 Map<String, String> 和误差映射转换为 List<DataField>
     *
     * @param map Map<字段名, 字段值>
     * @param toleranceMap Map<字段名, 误差百分比>
     * @return 字段列表
     */
    public static List<DataField> fromMap(Map<String, String> map, Map<String, Double> toleranceMap) {
        if (map == null || map.isEmpty()) {
            return new ArrayList<>();
        }
        return map.entrySet().stream()
                .map(entry -> {
                    DataField field = new DataField();
                    field.setFieldName(entry.getKey());
                    field.setValue(entry.getValue());
                    if (toleranceMap != null) {
                        field.setTolerance(toleranceMap.get(entry.getKey()));
                    }
                    return field;
                })
                .collect(Collectors.toList());
    }
}
