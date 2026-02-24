package com.example.demo.pojo.dto.mapvo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 填空答案项
 * 用于替代 Map<String, String> 结构（填空答案）
 * Key: 字段名，Value: 答案值
 */
@Data
public class FillBlankAnswer {

    /**
     * 字段名
     */
    private String fieldName;

    /**
     * 答案值
     */
    private String value;

    /**
     * 将 List<FillBlankAnswer> 转换为 Map<String, String>
     *
     * @param answers 答案列表
     * @return Map<字段名, 答案值>
     */
    public static Map<String, String> toMap(List<FillBlankAnswer> answers) {
        if (answers == null || answers.isEmpty()) {
            return Map.of();
        }
        return answers.stream()
                .collect(Collectors.toMap(FillBlankAnswer::getFieldName, FillBlankAnswer::getValue));
    }

    /**
     * 将 Map<String, String> 转换为 List<FillBlankAnswer>
     *
     * @param map Map<字段名, 答案值>
     * @return 答案列表
     */
    public static List<FillBlankAnswer> fromMap(Map<String, String> map) {
        if (map == null || map.isEmpty()) {
            return new ArrayList<>();
        }
        return map.entrySet().stream()
                .map(entry -> {
                    FillBlankAnswer answer = new FillBlankAnswer();
                    answer.setFieldName(entry.getKey());
                    answer.setValue(entry.getValue());
                    return answer;
                })
                .collect(Collectors.toList());
    }
}
