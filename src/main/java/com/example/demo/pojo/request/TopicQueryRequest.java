package com.example.demo.pojo.request;

import lombok.Data;

import java.util.List;

/**
 * 题目查询请求
 */
@Data
public class TopicQueryRequest {

    /**
     * 当前页码
     */
    private Long current = 1L;

    /**
     * 每页条数
     */
    private Long size = 10L;

    /**
     * 题目类型筛选：1-单选题，2-多选题，3-判断题，4-填空题，6-其他
     */
    private Integer type;

    /**
     * 关键词搜索（题目内容）
     */
    private String keyword;

    /**
     * 标签筛选（通用标签）
     */
    private List<Long> tagIds;

    /**
     * 难度标签筛选（type=2的标签ID列表）
     */
    private List<Long> difficultyTagIds;

    /**
     * 学科标签筛选（type=1的标签ID列表）
     */
    private List<Long> subjectTagIds;

    /**
     * 是否需要包含所有标签
     * true-必须包含所有标签，false-包含任一标签即可
     */
    private Boolean requireAllTags = false;

    /**
     * 创建者筛选
     */
    private String createdBy;
}
