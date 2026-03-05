package com.example.demo.util;

import com.example.demo.pojo.entity.ExperimentalProcedure;
import com.example.demo.pojo.entity.StudentExperimentalProcedure;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 成绩计算工具类
 * 统一处理实验总分和课程总分的计算逻辑
 */
@Slf4j
public class ScoreCalculationUtil {

    private ScoreCalculationUtil() {
        // 工具类���允许实例化
    }

    /**
     * 计算学生实验总分
     * 规则：按步骤占比加权计算，总分 = Σ(步骤得分 × 步骤占比 / 100)
     * 如果任意一个占比不为零的步骤未完成或未批改，则总分为0
     *
     * @param procedures 实验步骤列表（包含占比信息）
     * @param studentProcedures 学生的步骤提交记录
     * @return 学生实验总分
     */
    public static BigDecimal calculateExperimentScore(
            List<ExperimentalProcedure> procedures,
            List<StudentExperimentalProcedure> studentProcedures) {

        if (procedures == null || procedures.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // 筛选占比不为零的步骤
        List<ExperimentalProcedure> requiredProcedures = procedures.stream()
            .filter(p -> p.getProportion() != null && p.getProportion() > 0)
            .collect(Collectors.toList());

        if (requiredProcedures.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // 构建步骤ID到学生提交记录的映射
        Map<Long, StudentExperimentalProcedure> studentAnswerMap;
        if (studentProcedures == null || studentProcedures.isEmpty()) {
            studentAnswerMap = Collections.emptyMap();
        } else {
            studentAnswerMap = studentProcedures.stream()
                .collect(Collectors.toMap(
                    StudentExperimentalProcedure::getExperimentalProcedureId,
                    sp -> sp,
                    (a, b) -> a
                ));
        }

        // 检查是否所有必须步骤都已完成并批改
        for (ExperimentalProcedure procedure : requiredProcedures) {
            StudentExperimentalProcedure sp = studentAnswerMap.get(procedure.getId());
            // 未完成：没有记录或answer为空
            if (sp == null || sp.getAnswer() == null || sp.getAnswer().trim().isEmpty()) {
                return BigDecimal.ZERO;
            }
            // 未批改：score为null或isGraded为0/null
            if (sp.getScore() == null || sp.getIsGraded() == null || sp.getIsGraded() == 0) {
                return BigDecimal.ZERO;
            }
        }

        // 按权重计算总分：Σ(步骤得分 × 步骤占比 / 100)
        BigDecimal totalScore = BigDecimal.ZERO;
        for (ExperimentalProcedure procedure : requiredProcedures) {
            StudentExperimentalProcedure sp = studentAnswerMap.get(procedure.getId());
            if (sp != null && sp.getScore() != null && procedure.getProportion() != null) {
                BigDecimal weightedScore = sp.getScore()
                    .multiply(new BigDecimal(procedure.getProportion()))
                    .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
                totalScore = totalScore.add(weightedScore);
            }
        }

        return totalScore;
    }

    /**
     * 检查实验是否所有必须步骤都已批改
     *
     * @param procedures 实验步骤列表
     * @param studentProcedures 学生的步骤提交记录
     * @return true-已全部批改，false-存在未批改的步骤
     */
    public static boolean isAllProceduresGraded(
            List<ExperimentalProcedure> procedures,
            List<StudentExperimentalProcedure> studentProcedures) {

        if (procedures == null || procedures.isEmpty()) {
            return true;
        }

        Map<Long, StudentExperimentalProcedure> studentAnswerMap;
        if (studentProcedures == null || studentProcedures.isEmpty()) {
            studentAnswerMap = Collections.emptyMap();
        } else {
            studentAnswerMap = studentProcedures.stream()
                .collect(Collectors.toMap(
                    StudentExperimentalProcedure::getExperimentalProcedureId,
                    sp -> sp,
                    (a, b) -> a
                ));
        }

        for (ExperimentalProcedure procedure : procedures) {
            if (procedure.getProportion() != null && procedure.getProportion() > 0) {
                StudentExperimentalProcedure sp = studentAnswerMap.get(procedure.getId());
                if (sp == null || sp.getIsGraded() == null || sp.getIsGraded() == 0) {
                    return false;
                }
            }
        }

        return true;
    }
}
