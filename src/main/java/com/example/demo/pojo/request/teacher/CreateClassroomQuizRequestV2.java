package com.example.demo.pojo.request.teacher;

import lombok.Data;

import java.util.List;

/**
 * 创建课堂小测请求（新版）
 */
@Data
public class CreateClassroomQuizRequestV2 {

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
     * 标签匹配方式
     * false: 命中任一标签
     * true: 必须命中全部标签
     */
    private Boolean tagMatchAll;

    /**
     * 题目类型列表（仅在随机抽取时有效）
     */
    private List<Integer> topicTypes;

    /**
     * 老师选定的题目ID列表（仅在非随机模式时有效）
     */
    private List<Long> teacherSelectedTopicIds;

    public static CreateClassroomQuizRequestV2 fromLegacy(CreateClassroomQuizRequest request) {
        if (request == null) {
            return null;
        }
        CreateClassroomQuizRequestV2 target = new CreateClassroomQuizRequestV2();
        target.setClassExperimentId(request.getClassExperimentId());
        target.setQuizTitle(request.getQuizTitle());
        target.setQuizDescription(request.getQuizDescription());
        target.setQuizTimeLimit(request.getQuizTimeLimit());
        target.setIsRandom(request.getIsRandom());
        target.setTopicNumber(request.getTopicNumber());
        target.setTopicTags(request.getTopicTags());
        target.setTagMatchAll(Boolean.FALSE);
        target.setTopicTypes(request.getTopicTypes());
        target.setTeacherSelectedTopicIds(request.getTeacherSelectedTopicIds());
        return target;
    }

    public static CreateClassroomQuizRequest toLegacy(CreateClassroomQuizRequestV2 request) {
        if (request == null) {
            return null;
        }
        CreateClassroomQuizRequest target = new CreateClassroomQuizRequest();
        target.setClassExperimentId(request.getClassExperimentId());
        target.setQuizTitle(request.getQuizTitle());
        target.setQuizDescription(request.getQuizDescription());
        target.setQuizTimeLimit(request.getQuizTimeLimit());
        target.setIsRandom(request.getIsRandom());
        target.setTopicNumber(request.getTopicNumber());
        target.setTopicTags(request.getTopicTags());
        target.setTopicTypes(request.getTopicTypes());
        target.setTeacherSelectedTopicIds(request.getTeacherSelectedTopicIds());
        return target;
    }
}
