package com.example.demo.pojo.request;

import lombok.Data;

import java.util.List;

/**
 * 题目标签ID列表请求DTO
 * 用于创建题目时同时指定标签
 */
@Data
public class TopicWithTagIdsRequest {

    /**
     * 题号
     */
    private Integer number;

    /**
     * 题目类型：1-单选题，2-多选题，3-判断题，4-填空题，5-简答题，6-其他
     */
    private Integer type;

    /**
     * 题目内容
     */
    private String content;

    /**
     * 选项内容
     */
    private String choices;

    /**
     * 正确答案
     */
    private String correctAnswer;

    /**
     * 标签ID列表
     */
    private List<Long> tagIds;

}