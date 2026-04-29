package com.example.demo.pojo.request.teacher;

import lombok.Data;

import java.util.List;

/**
 * 创建课堂小测请求（旧版）
 */
@Data
public class CreateClassroomQuizRequestLegacy {

    /**
     * 班级实验ID
     */
    private Long classExperimentId;

    /**
     * 小测标题
     */
    private String quizTitle;

    /**
     * 小测描述
     */
    private String quizDescription;

    /**
     * 答题时间限制（分钟）
     */
    private Integer quizTimeLimit;

    /**
     * 是否随机抽取
     */
    private Boolean isRandom;

    /**
     * 题目数量（仅在随机抽取时有效）
     */
    private Integer topicNumber;

    /**
     * 标签ID列表（仅在随机抽取时有效）
     */
    private List<Long> topicTags;

    /**
     * 题目类型列表（仅在随机抽取时有效）
     */
    private List<Integer> topicTypes;

    /**
     * 老师选定的题目ID列表（仅在非随机模式时有效）
     */
    private List<Long> teacherSelectedTopicIds;
}
