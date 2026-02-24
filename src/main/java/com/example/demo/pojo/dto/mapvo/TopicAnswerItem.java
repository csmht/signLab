package com.example.demo.pojo.dto.mapvo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 题目答案项
 * 用于替代 Map<Long, String> 结构
 * Key: 题目ID，Value: 学生答案
 */
@Data
public class TopicAnswerItem {

    /**
     * 题目ID
     */
    private Long topicId;

    /**
     * 学生答案
     */
    private String answer;

    /**
     * 将 List<TopicAnswerItem> 转换为 Map<Long, String>
     *
     * @param items 答案项列表
     * @return Map<题目ID, 答案>
     */
    public static Map<Long, String> toMap(List<TopicAnswerItem> items) {
        if (items == null || items.isEmpty()) {
            return Map.of();
        }
        return items.stream()
                .collect(Collectors.toMap(TopicAnswerItem::getTopicId, TopicAnswerItem::getAnswer));
    }

    /**
     * 将 Map<Long, String> 转���为 List<TopicAnswerItem>
     *
     * @param map Map<题目ID, 答案>
     * @return 答案项列表
     */
    public static List<TopicAnswerItem> fromMap(Map<Long, String> map) {
        if (map == null || map.isEmpty()) {
            return new ArrayList<>();
        }
        return map.entrySet().stream()
                .map(entry -> {
                    TopicAnswerItem item = new TopicAnswerItem();
                    item.setTopicId(entry.getKey());
                    item.setAnswer(entry.getValue());
                    return item;
                })
                .collect(Collectors.toList());
    }
}
