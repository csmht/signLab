package com.example.demo.pojo.request.teacher;

import lombok.Data;

import java.util.List;

/**
 * 创建课堂小测请求
 */
@Data
public class CreateClassroomQuizRequest {

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

    // ==================== 以下为题库配置字段（仅当 procedureTopicId 为空时生效） ====================

    /**
     * 是否随机抽取
     * true: 随机抽取模式，根据标签和类型随机抽题
     * false: 教师选定模式，使用 teacherSelectedTopicIds 指定题目
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
     * 1-单选，2-多选，3-判断，4-填空，6-其他
     */
    private List<Integer> topicTypes;

    /**
     * 老师选定的题目ID列表（仅在非随机模式时有效）
     */
    private List<Long> teacherSelectedTopicIds;
}
