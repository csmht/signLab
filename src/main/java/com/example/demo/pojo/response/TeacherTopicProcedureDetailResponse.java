package com.example.demo.pojo.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 教师题库步骤详情响应
 * 步骤类型：type=3（题库答题）或 type=5（限时答题）
 */
@Data
public class TeacherTopicProcedureDetailResponse {

    /**
     * 步骤ID
     */
    private Long id;

    /**
     * 实验ID
     */
    private Long experimentId;

    /**
     * 步骤序号
     */
    private Integer number;

    /**
     * 步骤类型（3-题库答题，5-限时答题）
     */
    private Integer type;

    /**
     * 步骤描述
     */
    private String remark;

    /**
     * 是否可跳过
     */
    private Boolean isSkip;

    /**
     * 步骤分数占比
     */
    private Integer proportion;

    /**
     * 步骤开始时间偏移量(分钟)
     */
    private Integer offsetMinutes;

    /**
     * 步骤持续时间(分钟)
     */
    private Integer durationMinutes;

    // ===== 类型3：题库答题详细信息 =====

    /**
     * 题库详情ID（类型3时有效）
     */
    private Long procedureTopicId;

    /**
     * 是否随机抽取题目（类型3时有效）
     */
    private Boolean topicIsRandom;

    /**
     * 题目数量（类型3时有效）
     */
    private Integer topicNumber;

    /**
     * 标签限制列表（类型3、随机模式时有效）
     */
    private List<TeacherProcedureDetailResponse.TagInfo> topicTags;

    /**
     * 题目ID列表（类型3、非随机模式时有效）
     */
    private List<Long> topicIds;

    /**
     * 随机抽题题型（类型3、随机模式时有效）
     */
    private String topicTypes;

    /**
     * 题目详情列表（类型3、非随机模式时有效）
     */
    private List<TeacherProcedureDetailResponse.TopicDetail> topics;

    // ===== 类型5：限时答题详细信息 =====

    /**
     * 限时答题配置ID（类型5时有效）
     */
    private Long timedQuizId;

    /**
     * 是否随机抽取题目（类型5时有效）
     */
    private Boolean timedQuizIsRandom;

    /**
     * 题目数量（类型5时有效）
     */
    private Integer timedQuizNumber;

    /**
     * 答题时间限制（分钟）（类型5时有效）
     */
    private Integer timedQuizTimeLimit;

    /**
     * 标签限制列表（类型5、随机模式时有效）
     */
    private List<TeacherProcedureDetailResponse.TagInfo> timedQuizTags;

    /**
     * 题目类型限制（类型5、随机模式时有效，逗号分隔）
     */
    private String timedQuizTopicTypes;

    /**
     * 题目详情列表（类型5、非随机模式时有效）
     */
    private List<TeacherProcedureDetailResponse.TimedQuizTopicDetail> timedQuizTopics;
}
