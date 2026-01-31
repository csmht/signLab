package com.example.demo.pojo.request.teacher;

import lombok.Data;

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
     * 题库配置ID(ProcedureTopic.id)
     */
    private Long procedureTopicId;

    /**
     * 答题时间限制（分钟）
     */
    private Integer quizTimeLimit;
}
