package com.example.demo.pojo.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 实验成绩结果 VO
 * 用于表示学生在某个实验的成绩情况
 */
@Data
public class ExperimentGradeResult {

    /**
     * 实验ID
     */
    private Long experimentId;

    /**
     * 实验名称
     */
    private String experimentName;

    /**
     * 实验分数（满分100）
     * 如果未批改则为 null
     */
    private BigDecimal score;

    /**
     * 是否未批改
     * 存在占比>0且未批改的步骤时为 true
     */
    private boolean isUngraded;

    /**
     * 显示文本
     * "未批改" 或 具体分数（保留2位小数）
     */
    private String displayText;

    /**
     * 创建��批改的实验成绩结果
     */
    public static ExperimentGradeResult graded(Long experimentId, String experimentName, BigDecimal score) {
        ExperimentGradeResult result = new ExperimentGradeResult();
        result.setExperimentId(experimentId);
        result.setExperimentName(experimentName);
        result.setScore(score);
        result.setUngraded(false);
        result.setDisplayText(score != null ? score.setScale(2, RoundingMode.HALF_UP).toString() : "0.00");
        return result;
    }

    /**
     * 创建未批改的实验成绩结果
     */
    public static ExperimentGradeResult ungraded(Long experimentId, String experimentName) {
        ExperimentGradeResult result = new ExperimentGradeResult();
        result.setExperimentId(experimentId);
        result.setExperimentName(experimentName);
        result.setScore(null);
        result.setUngraded(true);
        result.setDisplayText("未批改");
        return result;
    }
}
