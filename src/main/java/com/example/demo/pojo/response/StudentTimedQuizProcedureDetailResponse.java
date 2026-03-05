package com.example.demo.pojo.response;

import com.example.demo.pojo.dto.mapvo.TopicChoice;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 学生限时答题步骤详情响应
 * 用于教师查询学生的限时答题步骤详情
 */
@Data
public class StudentTimedQuizProcedureDetailResponse {

    /**
     * 步骤ID
     */
    private Long id;

    /**
     * 步骤序号
     */
    private Integer number;

    /**
     * 步骤描述
     */
    private String remark;

    /**
     * 步骤分数占比
     */
    private Integer proportion;

    /**
     * 限时答题详情
     */
    private TimedQuizDetail timedQuizDetail;

    /**
     * 提交时间
     */
    private LocalDateTime submissionTime;

    /**
     * 得分
     */
    private java.math.BigDecimal score;

    /**
     * 教师评语
     */
    private String teacherComment;

    /**
     * 评分状态（0-未评分，1-教师人工评分，2-系统自动评分）
     */
    private Integer isGraded;

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
         * 正确答案
         */
        private String correctAnswer;

        /**
         * 是否正确
         */
        private Boolean isCorrect;

        /**
         * 得分
         */
        private java.math.BigDecimal score;

        /**
         * 从JSON字符串解析选项
         */
        public void setChoices(String choices) {
            if (choices == null || choices.trim().isEmpty()) {
                this.choices = null;
                return;
            }
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                java.util.Map<String, String> map = mapper.readValue(choices,
                    new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, String>>() {});
                this.choices = TopicChoice.fromMap(map);
            } catch (Exception e) {
                this.choices = null;
            }
        }
    }
}
