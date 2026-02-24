package com.example.demo.pojo.dto.mapvo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 题目选项
 * 用于替代 Map<String, String> 结构（选项）
 * Key: 选项字母（A, B, C, D），Value: 选项内容
 */
@Data
public class TopicChoice {

    /**
     * 选项字母（A, B, C, D）
     */
    private String optionKey;

    /**
     * 选项内容
     */
    private String optionContent;

    /**
     * 将 List<TopicChoice> 转换为 Map<String, String>
     *
     * @param choices 选项列表
     * @return Map<选项字母, 选项内容>
     */
    public static Map<String, String> toMap(List<TopicChoice> choices) {
        if (choices == null || choices.isEmpty()) {
            return Map.of();
        }
        return choices.stream()
                .collect(Collectors.toMap(TopicChoice::getOptionKey, TopicChoice::getOptionContent));
    }

    /**
     * 将 Map<String, String> 转换为 List<TopicChoice>
     *
     * @param map Map<选项字母, 选项内容>
     * @return 选项列表
     */
    public static List<TopicChoice> fromMap(Map<String, String> map) {
        if (map == null || map.isEmpty()) {
            return new ArrayList<>();
        }
        return map.entrySet().stream()
                .map(entry -> {
                    TopicChoice choice = new TopicChoice();
                    choice.setOptionKey(entry.getKey());
                    choice.setOptionContent(entry.getValue());
                    return choice;
                })
                .collect(Collectors.toList());
    }
}
