package com.example.demo.pojo.request.teacher;

import lombok.Data;
import java.util.List;

/**
 * 创建限时答题步骤请求
 */
@Data
public class CreateTimedQuizProcedureRequest {

    /** 实验ID */
    private Long experimentId;

    /** 是否可跳过 */
    private Boolean isSkip;

    /** 步骤分数占比 */
    private Integer proportion;

    /** 步骤描述 */
    private String remark;

    // ===== 题目配置 =====

    /** 是否随机抽取 */
    private Boolean isRandom;

    /** 题目数量（仅在随机抽取时有效） */
    private Integer topicNumber;

    /** 标签列表（仅在随机抽取时有效） */
    private List<String> topicTags;

    /** 题目类型列表（仅在随机抽取时有效，如 [1,2,3] 表示单选、多选、判断） */
    private List<Integer> topicTypes;

    /** 老师选定的题目ID列表（仅在非随机模式时有效） */
    private List<Long> teacherSelectedTopicIds;

    // ===== 时间配置 =====

    /** 步骤开始时间偏移量(分钟),默认为0 */
    private Integer offsetMinutes;

    /** 步骤持续时间(分钟) - 用于学生访问窗口控制 */
    private Integer durationMinutes;

    // ===== 限时答题特有配置 =====

    /** 答题时间限制(分钟) - 学生点击"获取题目"后的答题时长 */
    private Integer quizTimeLimit;
}
