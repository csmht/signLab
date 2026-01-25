package com.example.demo.pojo.dto;

import lombok.Data;

import java.util.List;

/**
 * 标签题目映射请求DTO
 * 用于批量添加或更新标签与题目的映射关系
 */
@Data
public class TopicTagMapRequest {

    /**
     * 题目ID
     */
    private Long topicId;

    /**
     * 标签ID列表
     */
    private List<Long> tagIds;

}