package com.example.demo.pojo.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 课堂小测统计响应
 * 用于教师查看小测的统计数据
 */
@Data
public class ClassroomQuizStatisticsResponse {

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
     * 状态:0-未开始,1-进行中,2-已结束
     */
    private Integer status;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 总参与人数
     */
    private Integer totalParticipants;

    /**
     * 已提交人数
     */
    private Integer submittedCount;

    /**
     * 完成率
     */
    private BigDecimal completionRate;

    /**
     * 平均分
     */
    private BigDecimal averageScore;

    /**
     * 正确率(全部正确的比例)
     */
    private BigDecimal correctRate;

    /**
     * 题目统计列表
     */
    private List<TopicStatistics> topicStatistics;

    /**
     * 学生答题列表
     */
    private List<StudentAnswerInfo> studentAnswers;

    /**
     * 题目统计
     */
    @Data
    public static class TopicStatistics {
        /**
         * 题目ID
         */
        private Long topicId;

        /**
         * 题号
         */
        private Integer number;

        /**
         * 题目类型
         */
        private Integer type;

        /**
         * 题目内容
         */
        private String content;

        /**
         * 正确答案
         */
        private String correctAnswer;

        /**
         * 答对人数
         */
        private Integer correctCount;

        /**
         * 答错人数
         */
        private Integer incorrectCount;

        /**
         * 正确率
         */
        private BigDecimal correctRate;
    }

    /**
     * 学生答题信息
     */
    @Data
    public static class StudentAnswerInfo {
        /**
         * 学生用户名
         */
        private String studentUsername;

        /**
         * 班级编号
         */
        private String classCode;

        /**
         * 得分
         */
        private BigDecimal score;

        /**
         * 是否全部正确
         */
        private Boolean isCorrect;

        /**
         * 提交时间
         */
        private LocalDateTime submissionTime;
    }
}
