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
     * 将 Map<String, String> 转换为 List<DataField>
     *
     * @param map Map<字段名, 字段值>
     * @return 字段列表
     */
    public static List<DataField> fromMap(Map<String, String> map) {
        if (map == null || map.isEmpty()) {
            return new ArrayList<>();
        }
        return map.entrySet().stream()
                .map(entry -> {
                    DataField field = new DataField();
                    field.setFieldName(entry.getKey());
                    field.setValue(entry.getValue());
                    return field;
                })
                .collect(Collectors.toList());
    }
}
