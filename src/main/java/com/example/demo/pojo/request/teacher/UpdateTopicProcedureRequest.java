package com.example.demo.pojo.request.teacher;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 修改题库练习步骤请求
 */
@Data
public class UpdateTopicProcedureRequest {

    /**
     * 步骤ID
     */
    private Long id;

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
     * 标签限制（仅在随机抽取时有效，格式 id1,id2）
     */
    private String topicTags;

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
