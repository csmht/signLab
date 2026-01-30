package com.example.demo.pojo.request.teacher;

import lombok.Data;
import java.util.List;

/**
 * 更新限时答题步骤请求
 */
@Data
public class UpdateTimedQuizProcedureRequest {

    /** 步骤ID（必填） */
    private Long id;

    /** 是否可跳过 */
    private Boolean isSkip;

    /** 步骤分数占比 */
    private Integer proportion;

    /** 步骤描述 */
    private String remark;

    // ===== 题目配置 =====

    /** 是否随机抽取 */
    private Boolean isRandom;

    /** 题目数量 */
    private Integer topicNumber;

    /** 标签列表 */
    private List<String> topicTags;

    /** 题目类型列表 */
    private List<Integer> topicTypes;

    /** 老师选定的题目ID列表 */
    private List<Long> teacherSelectedTopicIds;

    // ===== 时间配置 =====

    /** 步骤开始时间偏移量(分钟) */
    private Integer offsetMinutes;

    /** 步骤持续时间(分钟) */
    private Integer durationMinutes;

    // ===== 限时答题特有配置 =====

    /** 答题时间限制(分钟) */
    private Integer quizTimeLimit;
}
