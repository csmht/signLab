package com.example.demo.pojo.response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 学生题库答题步骤详情响应
 * 用于教师查询学生的题库答题步骤详情
 */
@Data
public class StudentTopicProcedureDetailResponse {

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
     * 题库详情
     */
    private TopicDetail topicDetail;

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
         * 选项内容（key: 选项字母如A、B、C、D，value: 选项内容）
         */
        private Map<String, String> choices;

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

        // 将答案从String变为Map
        public void setChoices(String choices) {
            if (choices == null || choices.trim().isEmpty()) {
                this.choices = null;
                return;
            }
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                this.choices = mapper.readValue(choices,
                    new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, String>>() {});
            } catch (Exception e) {
                // 解析失败时设置为null
                this.choices = null;
            }
        }
    }
}
