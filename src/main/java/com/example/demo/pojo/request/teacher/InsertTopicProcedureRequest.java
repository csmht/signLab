package com.example.demo.pojo.request.teacher;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 插入题库练习步骤请求
 */
@Data
public class InsertTopicProcedureRequest {

    /**
     * 实验ID
     */
    private Long experimentId;

    /**
     * 插入位置（在该步骤序号后插入）
     */
    private Integer afterNumber;

    /**
     * 是否可跳过
     */
    private Boolean isSkip;

    /**
     * 步骤分数占比
     */
    private Integer proportion;

    /**
     * 步骤描述
     */
    private String remark;

    /**
     * 是否随机抽取
     */
    private Boolean isRandom;

    /**
     * 题目数量（仅在随机抽取时有效）
     */
    private Integer topicNumber;

    /**
     * 标签ID列表（仅在随机抽取时有效），后端自动拼接成"id1,id2"格式
     */
    private List<String> topicTags;

    /**
     * 老师选定的题目ID列表（仅在非随机模式时有效）
     */
    private List<Long> teacherSelectedTopicIds;

    /**
     * 步骤开始时间
     */
    private LocalDateTime startTime;

    /**
     * 步骤结束时间
     */
    private LocalDateTime endTime;
}
