package com.example.demo.pojo.request.teacher;

import lombok.Data;

import java.util.List;

/**
 * 创建题库练习步骤请求
 */
@Data
public class CreateTopicProcedureRequest {

    /**
     * 实验ID
     */
    private Long experimentId;

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
     * 题目类型列表（仅在随机抽取时有效），后端自动拼接成"1,2,3"格式
     * 1-单选，2-多选，3-判断，4-填空，6-其他
     */
    private List<Integer> topicTypes;

    /**
     * 老师选定的题目ID列表（仅在非随机模式时有效）
     */
    private List<Long> teacherSelectedTopicIds;

    /**
     * 步骤开始时间偏移量(分钟),默认为0
     */
    private Integer offsetMinutes;

    /**
     * 步骤持续时间(分钟)
     */
    private Integer durationMinutes;
}
