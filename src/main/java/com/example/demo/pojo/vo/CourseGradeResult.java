package com.example.demo.pojo.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * 课程成绩结果 VO
 * 用于表示学生在某课程的总成绩情况
 */
@Data
public class CourseGradeResult {

    /**
     * 课程ID
     */
    private String courseId;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 课程分数（满分100）
     * 如果存在未批改的实验则为 null
     */
    private BigDecimal score;

    /**
     * 是否未批改
     * 存在任一未批改的实验时为 true
     */
    private boolean isUngraded;

    /**
     * 各实验成绩结果
     * key: 实验ID
     * value: 实验成绩结果
     */
    private Map<Long, ExperimentGradeResult> experimentResults;

    /**
     * 显示文本
     * "未批改" 或 具体分数（保留2位小数）
     */
    private String displayText;

    /**
     * 创建已批改的课程成绩结果
     */
    public static CourseGradeResult graded(String courseId, String courseName, BigDecimal score,
                                           Map<Long, ExperimentGradeResult> experimentResults) {
        CourseGradeResult result = new CourseGradeResult();
        result.setCourseId(courseId);
        result.setCourseName(courseName);
        result.setScore(score);
        result.setUngraded(false);
        result.setExperimentResults(experimentResults);
        result.setDisplayText(score != null ? score.setScale(2, RoundingMode.HALF_UP).toString() : "0.00");
        return result;
    }

    /**
     * 创建未批改的课程成绩结果
     */
    public static CourseGradeResult ungraded(String courseId, String courseName,
                                             Map<Long, ExperimentGradeResult> experimentResults) {
        CourseGradeResult result = new CourseGradeResult();
        result.setCourseId(courseId);
        result.setCourseName(courseName);
        result.setScore(null);
        result.setUngraded(true);
        result.setExperimentResults(experimentResults);
        result.setDisplayText("未批改");
        return result;
    }
}
