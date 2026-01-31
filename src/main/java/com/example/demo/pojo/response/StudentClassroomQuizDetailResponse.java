package com.example.demo.pojo.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 学生课堂小测详情响应
 * 用于学生查看小测详情和答题情况
 */
@Data
public class StudentClassroomQuizDetailResponse {

    /**
     * 小测ID
     */
    private Long quizId;

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
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 状态:0-未开始,1-进行中,2-已结束
     */
    private Integer status;

    /**
     * 是否已提交
     */
    private Boolean isSubmitted;

    /**
     * 提交时间
     */
    private LocalDateTime submissionTime;

    /**
     * 得分
     */
    private java.math.BigDecimal score;

    /**
     * 题目详情列表
     */
    private List<TopicDetail> topics;

    /**
     * 题目详情
     */
    @Data
    public static class TopicDetail {
        /**
         * 题目ID
         */
        private Long topicId;

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
         * 正确答案（仅已结束的小测返回）
         */
        private String correctAnswer;

        /**
         * 是否正确（仅已结束的小测返回）
         */
        private Boolean isCorrect;

        // 将答案从String变为Map
        public void setChoices(String choices) {
            if (choices == null || choices.trim().isEmpty()) {
                this.choices = null;
                return;
            }
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                this.choices = mapper.readValue(choices,
                    new com.fasterxml.jackson.core.type.TypeReference<Map<String, String>>() {});
            } catch (Exception e) {
                // 解析失败时设置为null
                this.choices = null;
            }
        }
    }
}
