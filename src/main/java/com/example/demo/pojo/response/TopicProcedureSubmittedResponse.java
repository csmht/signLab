package com.example.demo.pojo.response;

import com.example.demo.pojo.dto.mapvo.TopicChoice;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 题库步骤详情响应（已提交）
 * 步骤类型：type=3（题库答题）或 type=5（限时答题）
 */
@Data
public class TopicProcedureSubmittedResponse {

    /**
     * 步骤ID
     */
    private Long id;

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
     * 步骤分数占比
     */
    private Integer proportion;

    /**
     * 是否可修改
     * true-可以修改，false-不可修改
     */
    private Boolean isModifiable;

    /**
     * 不可修改的原因（当isModifiable为false时返回）
     */
    private String notModifiableReason;

    /**
     * 提交时间
     */
    private LocalDateTime submissionTime;

    /**
     * 得分
     */
    private BigDecimal score;

    /**
     * 教师评语
     */
    private String teacherComment;

    /**
     * 评分状态（0-未评分，1-教师人工评分，2-系统自动评分）
     */
    private Integer isGraded;

    /**
     * 是否已过答题时间
     * 如果true，表示当前时间已超过步骤答题时间，可以显示正确答案
     * 如果false，表示还在答题时间内，不显示正确答案
     */
    private Boolean isAfterEndTime;

    /**
     * 题库详情（类型3）
     */
    private TopicDetail topicDetail;

    /**
     * 限时答题详情（类型5）
     */
    private TimedQuizDetail timedQuizDetail;

    /**
     * 题库详情
     */
    @Data
    public static class TopicDetail {
        /**
         * 题库详情ID
         */
        private Long id;

        /**
         * 是否随机抽取题目
         */
        private Boolean isRandom;

        /**
         * 题目数量
         */
        private Integer number;

        /**
         * 标签限制
         */
        private String tags;

        /**
         * 题目列表（带答案）
         * 注意：correctAnswer和isCorrect字段仅在当前时间超过步骤答题时间后才返回
         */
        private List<TopicItem> topics;
    }

    /**
     * 限时答题详情
     */
    @Data
    public static class TimedQuizDetail {
        /**
         * 限时答题配置ID
         */
        private Long id;

        /**
         * 是否随机抽取题目
         */
        private Boolean isRandom;

        /**
         * 题目数量
         */
        private Integer number;

        /**
         * 答题时间限制（分钟）
         */
        private Integer quizTimeLimit;

        /**
         * 是否已锁定
         */
        private Boolean isLocked;

        /**
         * 题目列表（带答案）
         */
        private List<TopicItem> topics;
    }

    /**
     * 题目项（带答案）
     */
    @Data
    public static class TopicItem {
        /**
         * 题目ID
         */
        private Long id;

        /**
         * 题号
         */
        private Integer number;

        /**
         * 题目类型（1-单选题，2-多选题，3-判断题，4-填空题，5-简答题，6-其他）
         */
        private Integer type;

        /**
         * 题目内容
         */
        private String content;

        /**
         * 选项内容列表
         */
        private List<TopicChoice> choices;

        /**
         * 学生答案
         */
        private String studentAnswer;

        /**
         * 正确答案（仅在当前时间超过步骤答题时间后才返回）
         */
        private String correctAnswer;

        /**
         * 是否正确（仅在当前时间超过步骤答题时间后才返回）
         */
        private Boolean isCorrect;

        /**
         * 得分
         */
        private BigDecimal score;

        public void setChoices(List<TopicChoice> choices){
            this.choices = choices;
        }

        /**
         * 从JSON字符串解析选项
         */
        public void setChoices(String choices) {
            if (choices == null || choices.trim().isEmpty()) {
                setChoices(java.util.Collections.emptyList());
                return;
            }
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                java.util.Map<String, String> map = mapper.readValue(choices,
                    new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, String>>() {});
                setChoices(TopicChoice.fromMap(map));
            } catch (Exception e) {
                setChoices(java.util.Collections.emptyList());
            }
        }
    }
}
