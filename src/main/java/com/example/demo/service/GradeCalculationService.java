package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demo.pojo.dto.mapvo.ExperimentResultItem;
import com.example.demo.pojo.entity.Experiment;
import com.example.demo.pojo.entity.ExperimentalProcedure;
import com.example.demo.pojo.entity.StudentExperimentalProcedure;
import com.example.demo.pojo.vo.CourseGradeResult;
import com.example.demo.pojo.vo.ExperimentGradeResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 成绩计算服务
 * 提供实验成绩和课程成绩的计算功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GradeCalculationService {

    private final ExperimentService experimentService;
    private final ExperimentalProcedureService experimentalProcedureService;
    private final StudentExperimentalProcedureService studentExperimentalProcedureService;
    private final CourseService courseService;

    /**
     * 计算学生在某实验的成绩
     *
     * @param experimentId     实验ID
     * @param studentUsername  学生用户名
     * @param classCode        班级编号
     * @return 实验成绩结果（包含分数和是否未批改）
     */
    public ExperimentGradeResult calculateExperimentGrade(Long experimentId, String studentUsername, String classCode) {
        // 1. 查询实验信息
        Experiment experiment = experimentService.getById(experimentId);
        if (experiment == null) {
            return ExperimentGradeResult.ungraded(experimentId, "未知实验");
        }

        // 2. 查询实验步骤（包含占比）
        List<ExperimentalProcedure> procedures = experimentalProcedureService.getByExperimentId(experimentId);

        // 3. 查询学生的步骤答案和得分
        List<StudentExperimentalProcedure> studentProcedures = studentExperimentalProcedureService
                .getByStudentAndExperiment(studentUsername, classCode, experimentId);

        // 4. 构建步骤ID到学生答案的映射
        Map<Long, StudentExperimentalProcedure> studentAnswerMap = new HashMap<>();
        for (StudentExperimentalProcedure sp : studentProcedures) {
            studentAnswerMap.put(sp.getExperimentalProcedureId(), sp);
        }

        // 5. 检查是否有未批改的步骤（占比>0且未批改）
        boolean hasUngradedProcedure = false;
        for (ExperimentalProcedure procedure : procedures) {
            if (procedure.getProportion() != null && procedure.getProportion() > 0) {
                StudentExperimentalProcedure studentAnswer = studentAnswerMap.get(procedure.getId());
                // 未提交或未批改（isGraded == 0 或 null）
                if (studentAnswer == null || studentAnswer.getIsGraded() == null || studentAnswer.getIsGraded() == 0) {
                    hasUngradedProcedure = true;
                    break;
                }
            }
        }

        // 6. 如果有未批改的步骤，返回未批改结果
        if (hasUngradedProcedure) {
            return ExperimentGradeResult.ungraded(experimentId, experiment.getExperimentName());
        }

        // 7. 计算实验成绩 = Σ(步骤得分 × 步骤占比 / 100)
        BigDecimal experimentScore = BigDecimal.ZERO;
        for (ExperimentalProcedure procedure : procedures) {
            if (procedure.getProportion() != null && procedure.getProportion() > 0) {
                StudentExperimentalProcedure studentAnswer = studentAnswerMap.get(procedure.getId());
                if (studentAnswer != null && studentAnswer.getScore() != null) {
                    BigDecimal procedureScore = studentAnswer.getScore();
                    BigDecimal proportion = new BigDecimal(procedure.getProportion());
                    // 步骤得分 × 步骤占比 / 100
                    BigDecimal weightedScore = procedureScore.multiply(proportion)
                            .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
                    experimentScore = experimentScore.add(weightedScore);
                }
            }
        }

        // 确保分数不超过100
        if (experimentScore.compareTo(new BigDecimal("100")) > 0) {
            experimentScore = new BigDecimal("100");
        }

        return ExperimentGradeResult.graded(experimentId, experiment.getExperimentName(), experimentScore);
    }

    /**
     * 计算学生在某课程的总成绩
     *
     * @param courseId         课程ID
     * @param studentUsername  学生用户名
     * @param classCode        班级编号
     * @return 课程成绩结果（包含分数和是否未批改）
     */
    public CourseGradeResult calculateCourseGrade(String courseId, String studentUsername, String classCode) {
        // 1. 查询课程信息
        com.example.demo.pojo.entity.Course course = courseService.getById(courseId);
        String courseName = course != null ? course.getCourseName() : "未知课程";

        // 2. 查询课程下所有实验
        LambdaQueryWrapper<Experiment> experimentQuery = new LambdaQueryWrapper<>();
        experimentQuery.eq(Experiment::getCourseId, courseId)
                .eq(Experiment::getIsDeleted, false)
                .orderByAsc(Experiment::getCreatedTime);
        List<Experiment> experiments = experimentService.list(experimentQuery);

        // 3. 计算每个实验的成绩
        List<ExperimentResultItem> experimentResults = new ArrayList<>();
        boolean hasUngradedExperiment = false;

        for (Experiment experiment : experiments) {
            ExperimentGradeResult expResult = calculateExperimentGrade(
                    experiment.getId(), studentUsername, classCode);
            ExperimentResultItem item = new ExperimentResultItem();
            item.setExperimentId(experiment.getId());
            item.setResult(expResult);
            experimentResults.add(item);

            if (expResult.isUngraded()) {
                hasUngradedExperiment = true;
            }
        }

        // 4. 如果有未批改的实验，返回未批改结果
        if (hasUngradedExperiment) {
            return CourseGradeResult.ungraded(courseId, courseName, experimentResults);
        }

        // 5. 计算课程成绩 = Σ(实验成绩 × 实验占比 / 100)
        BigDecimal courseScore = BigDecimal.ZERO;
        for (ExperimentResultItem resultItem : experimentResults) {
            ExperimentGradeResult expResult = resultItem.getResult();
            // 查找对应的实验获取占比
            Experiment experiment = experiments.stream()
                    .filter(e -> e.getId().equals(resultItem.getExperimentId()))
                    .findFirst().orElse(null);
            if (expResult.getScore() != null && experiment != null && experiment.getPercentage() != null && experiment.getPercentage() > 0) {
                BigDecimal expScore = expResult.getScore();
                BigDecimal percentage = new BigDecimal(experiment.getPercentage());
                // 实验成绩 × 实验占比 / 100
                BigDecimal weightedScore = expScore.multiply(percentage)
                        .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
                courseScore = courseScore.add(weightedScore);
            }
        }

        // 确保分数不超过100
        if (courseScore.compareTo(new BigDecimal("100")) > 0) {
            courseScore = new BigDecimal("100");
        }

        return CourseGradeResult.graded(courseId, courseName, courseScore, experimentResults);
    }

    /**
     * 批量计算学生在某课程的总成绩（多个班级的学生）
     *
     * @param courseId     课程ID
     * @param classCodes   班级编号列表
     * @return 学生用户名 -> 课程成绩结果的映射
     */
    public Map<String, CourseGradeResult> calculateCourseGradesForClass(String courseId, List<String> classCodes) {
        Map<String, CourseGradeResult> results = new HashMap<>();

        for (String classCode : classCodes) {
            // 查询班级下的所有学生（通过 StudentClassRelation）
            // 这里需要在调用方处理学生查询
        }

        return results;
    }
}
