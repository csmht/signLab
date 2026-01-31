package com.example.demo.util;

import com.example.demo.pojo.entity.Topic;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

/**
 * 课堂小测自动评分工具
 * 用于客观题的自动评分逻辑
 */
@Slf4j
@Component
public class ClassroomQuizScorer {

    /**
     * 自动评分
     *
     * @param studentAnswers 学生答案 {topicId: answer}
     * @param topics 题目列表
     * @return 得分(0-100)
     */
    public BigDecimal calculateScore(Map<Long, String> studentAnswers, List<Topic> topics) {
        if (topics == null || topics.isEmpty()) {
            return BigDecimal.ZERO;
        }

        int correctCount = 0;
        int totalCount = topics.size();

        for (Topic topic : topics) {
            String studentAnswer = studentAnswers.get(topic.getId());
            String correctAnswer = topic.getCorrectAnswer();

            if (studentAnswer != null && studentAnswer.equals(correctAnswer)) {
                correctCount++;
            }
        }

        return new BigDecimal(correctCount)
                .multiply(new BigDecimal(100))
                .divide(new BigDecimal(totalCount), 2, RoundingMode.HALF_UP);
    }

    /**
     * 判断是否全部正确
     *
     * @param studentAnswers 学生答案
     * @param topics 题目列表
     * @return 是否全部正确
     */
    public Boolean isAllCorrect(Map<Long, String> studentAnswers, List<Topic> topics) {
        if (topics == null || topics.isEmpty()) {
            return false;
        }

        for (Topic topic : topics) {
            String studentAnswer = studentAnswers.get(topic.getId());
            String correctAnswer = topic.getCorrectAnswer();

            if (studentAnswer == null || !studentAnswer.equals(correctAnswer)) {
                return false;
            }
        }

        return true;
    }
}
