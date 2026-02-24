package com.example.demo.pojo.dto.mapvo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 标签统计项
 * 用于替代 Map<String, Long> 结构（标签统计）
 * Key: 标签名称，Value: 题目数量
 */
@Data
public class TagCountItem {

    /**
     * 标签名称
     */
    private String tagName;

    /**
     * 该标签下的题目数量
     */
    private Long count;

    /**
     * 将 List<TagCountItem> 转换为 Map<String, Long>
     *
     * @param items 统计项列表
     * @return Map<标签名称, 题目数量>
     */
    public static Map<String, Long> toMap(List<TagCountItem> items) {
        if (items == null || items.isEmpty()) {
            return Map.of();
        }
        return items.stream()
                .collect(Collectors.toMap(TagCountItem::getTagName, TagCountItem::getCount));
    }

    /**
     * 将 Map<String, Long> 转换为 List<TagCountItem>
     *
     * @param map Map<标签名称, 题目数量>
     * @return 统计项列表
     */
    public static List<TagCountItem> fromMap(Map<String, Long> map) {
        if (map == null || map.isEmpty()) {
            return new ArrayList<>();
        }
        return map.entrySet().stream()
                .map(entry -> {
                    TagCountItem item = new TagCountItem();
                    item.setTagName(entry.getKey());
                    item.setCount(entry.getValue());
                    return item;
                })
                .collect(Collectors.toList());
    }
}
