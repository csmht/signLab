package com.example.demo.pojo.dto.mapvo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 题型统计项
 * 用于替代 Map<Integer, Long> 结构
 * Key: 题型类型，Value: 题目数量
 */
@Data
public class TypeCountItem {

    /**
     * 题型类型（1-单选，2-多选，3-判断，4-填空，5-简答，6-其他）
     */
    private Integer type;

    /**
     * 该题型下的题目数量
     */
    private Long count;

    /**
     * 将 List<TypeCountItem> 转换为 Map<Integer, Long>
     *
     * @param items 统计项列表
     * @return Map<题型类型, 题目数量>
     */
    public static Map<Integer, Long> toMap(List<TypeCountItem> items) {
        if (items == null || items.isEmpty()) {
            return Map.of();
        }
        return items.stream()
                .collect(Collectors.toMap(TypeCountItem::getType, TypeCountItem::getCount));
    }

    /**
     * 将 Map<Integer, Long> 转换为 List<TypeCountItem>
     *
     * @param map Map<题型类型, 题目数量>
     * @return 统计项列表
     */
    public static List<TypeCountItem> fromMap(Map<Integer, Long> map) {
        if (map == null || map.isEmpty()) {
            return new ArrayList<>();
        }
        return map.entrySet().stream()
                .map(entry -> {
                    TypeCountItem item = new TypeCountItem();
                    item.setType(entry.getKey());
                    item.setCount(entry.getValue());
                    return item;
                })
                .collect(Collectors.toList());
    }
}
