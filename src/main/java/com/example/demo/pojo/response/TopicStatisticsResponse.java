package com.example.demo.pojo.response;

import lombok.Data;

import java.util.Map;

/**
 * 题目统计响应
 */
@Data
public class TopicStatisticsResponse {

    /**
     * 总题目数
     */
    private Long totalCount;

    /**
     * 按题型统计
     * key: 题型类型（1-单选，2-多选等）
     * value: 该题型题目数量
     */
    private Map<Integer, Long> typeCount;

    /**
     * 按学科标签统计（type=1）
     * key: 标签名称
     * value: 该标签下的题目数量
     */
    private Map<String, Long> subjectTagCount;

    /**
     * 按难度标签统计（type=2）
     * key: 标签名称
     * value: 该标签下的题目数量
     */
    private Map<String, Long> difficultyTagCount;

    /**
     * 按自定义标签统计（type=4）
     * key: 标签名称
     * value: 该标签下的题目数量
     */
    private Map<String, Long> customTagCount;
}
