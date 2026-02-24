package com.example.demo.pojo.response;

import com.example.demo.pojo.dto.mapvo.TagCountItem;
import com.example.demo.pojo.dto.mapvo.TypeCountItem;
import lombok.Data;

import java.util.List;

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
     * 按题型统计列表
     */
    private List<TypeCountItem> typeCount;

    /**
     * 按学科标签统计列表（type=1）
     */
    private List<TagCountItem> subjectTagCount;

    /**
     * 按难度标签统计列表（type=2）
     */
    private List<TagCountItem> difficultyTagCount;

    /**
     * 按自定义标签统计列表（type=4）
     */
    private List<TagCountItem> customTagCount;
}
