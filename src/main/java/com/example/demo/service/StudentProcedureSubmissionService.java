package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.ClassExperimentMapper;
import com.example.demo.mapper.StudentExperimentalProcedureMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.pojo.entity.ClassExperiment;
import com.example.demo.pojo.entity.StudentExperimentalProcedure;
import com.example.demo.pojo.entity.User;
import com.example.demo.pojo.response.StudentProcedureSubmissionResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 学生步骤提交服务
 * 基于 StudentExperimentalProcedure 实体提供查询和批改功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StudentProcedureSubmissionService {

    private final StudentExperimentalProcedureMapper studentExperimentalProcedureMapper;
    private final UserMapper userMapper;
    private final ClassExperimentMapper classExperimentMapper;
    private final ClassExperimentClassRelationService classExperimentClassRelationService;
    private final StudentProcedureCompletionService studentProcedureCompletionService;

    /** 提交状态常量 */
    public static final Integer STATUS_NOT_GRADED = 0;      // 未评分
    public static final Integer STATUS_TEACHER_GRADED = 1;  // 教师人工评分
    public static final Integer STATUS_AUTO_GRADED = 2;     // 系统自动评分

    /**
     * 查询学生的步骤提交列表
     *
     * @param studentUsername 学生用户名
     * @param experimentId    实验ID（可选）
     * @return 步骤列表
     */
    public List<StudentProcedureSubmissionResponse> getStudentSubmissions(
            String studentUsername, Long experimentId) {
        LambdaQueryWrapper<StudentExperimentalProcedure> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StudentExperimentalProcedure::getStudentUsername, studentUsername);

        if (experimentId != null) {
            queryWrapper.eq(StudentExperimentalProcedure::getExperimentId, experimentId);
        }

        queryWrapper.orderByDesc(StudentExperimentalProcedure::getCreatedTime);

        List<StudentExperimentalProcedure> submissions = studentExperimentalProcedureMapper.selectList(queryWrapper);

        return submissions.stream().map(this::buildResponse).collect(Collectors.toList());
    }

    /**
     * 查询课程的步骤提交列表（教师端）
     *
     * @param classCode        班级编号
     * @param experimentId     实验ID（可选）
     * @param submissionStatus 提交状态（可选）
     * @return 步骤列表
     */
    public List<StudentProcedureSubmissionResponse> getCourseSubmissions(
            String classCode, Long experimentId, Integer submissionStatus) {
        LambdaQueryWrapper<StudentExperimentalProcedure> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StudentExperimentalProcedure::getClassCode, classCode);

        if (experimentId != null) {
            queryWrapper.eq(StudentExperimentalProcedure::getExperimentId, experimentId);
        }

        if (submissionStatus != null) {
            queryWrapper.eq(StudentExperimentalProcedure::getIsGraded, submissionStatus);
        }

        queryWrapper.orderByDesc(StudentExperimentalProcedure::getCreatedTime);

        List<StudentExperimentalProcedure> submissions = studentExperimentalProcedureMapper.selectList(queryWrapper);

        return submissions.stream().map(this::buildResponse).collect(Collectors.toList());
    }

    /**
     * 根据ID查询步骤提交详情
     *
     * @param submissionId 步骤提交ID
     * @return 步骤详情
     */
    public StudentProcedureSubmissionResponse getSubmissionById(Long submissionId) {
        StudentExperimentalProcedure submission = studentExperimentalProcedureMapper.selectById(submissionId);
        if (submission == null) {
            throw new BusinessException(404, "步骤提交记录不存在");
        }

        return buildResponse(submission);
    }

    /**
     * 批改步骤提交
     *
     * @param submissionId    步骤提交ID
     * @param teacherComment  教师评语
     * @param score           评分
     */
    @Transactional(rollbackFor = Exception.class)
    public void gradeProcedure(Long submissionId, String teacherComment, BigDecimal score) {
        StudentExperimentalProcedure submission = studentExperimentalProcedureMapper.selectById(submissionId);
        if (submission == null) {
            throw new BusinessException(404, "步骤提交记录不存在");
        }

        submission.setIsGraded(STATUS_TEACHER_GRADED);
        submission.setTeacherComment(teacherComment);
        submission.setScore(score);
        submission.setUpdatedTime(LocalDateTime.now());

        int updated = studentExperimentalProcedureMapper.updateById(submission);
        if (updated <= 0) {
            throw new BusinessException(500, "批改步骤失败");
        }

        log.info("批改实验步骤成功，ID：{}，评分：{}", submissionId, score);
    }

    /**
     * 按课次重新执行机器自动批改
     *
     * @param classExperimentId 班级实验ID
     * @return 重批结果统计
     */
    @Transactional(rollbackFor = Exception.class)
    public ReAutoGradeSummary reAutoGradeByClassExperimentId(Long classExperimentId) {
        ClassExperiment classExperiment = classExperimentMapper.selectById(classExperimentId);
        if (classExperiment == null) {
            throw new BusinessException(404, "班级实验不存在");
        }

        List<StudentExperimentalProcedure> submissions = listSubmissionsByClassExperimentId(classExperimentId, classExperiment);
        ReAutoGradeSummary summary = new ReAutoGradeSummary();
        summary.setScannedCount(submissions.size());

        for (StudentExperimentalProcedure submission : submissions) {
            Integer gradeStatus = submission.getIsGraded();
            if (STATUS_TEACHER_GRADED.equals(gradeStatus)) {
                summary.setSkippedTeacherGradedCount(summary.getSkippedTeacherGradedCount() + 1);
                continue;
            }

            StudentProcedureCompletionService.AutoGradeExecutionResult result =
                    studentProcedureCompletionService.autoGradeExistingDataCollectionSubmission(submission.getId());

            if (result.isSuccess()) {
                summary.setReAutoGradedCount(summary.getReAutoGradedCount() + 1);
            } else if (result.isSkipped()) {
                summary.setSkippedUnsupportedCount(summary.getSkippedUnsupportedCount() + 1);
            } else {
                summary.setFailedCount(summary.getFailedCount() + 1);
            }
        }

        log.info("课次 {} 重新机器批改完成，扫描：{}，成功：{}，跳过人工：{}，跳过其他：{}，失败：{}",
                classExperimentId,
                summary.getScannedCount(),
                summary.getReAutoGradedCount(),
                summary.getSkippedTeacherGradedCount(),
                summary.getSkippedUnsupportedCount(),
                summary.getFailedCount());
        return summary;
    }

    /**
     * 构建响应对象
     */
    private StudentProcedureSubmissionResponse buildResponse(StudentExperimentalProcedure submission) {
        StudentProcedureSubmissionResponse response = new StudentProcedureSubmissionResponse();
        response.setId(submission.getId());
        response.setExperimentId(submission.getExperimentId());
        response.setProcedureId(submission.getExperimentalProcedureId());
        response.setNumber(submission.getNumber());
        response.setStudentUsername(submission.getStudentUsername());
        response.setClassCode(submission.getClassCode());
        response.setIsGraded(submission.getIsGraded());
        response.setTeacherComment(submission.getTeacherComment());
        response.setScore(submission.getScore());
        response.setCreateTime(submission.getCreatedTime());
        response.setUpdateTime(submission.getUpdatedTime());

        // 提交时间使用创建时间
        response.setSubmissionTime(submission.getCreatedTime());

        // 查询学生姓名
        User student = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, submission.getStudentUsername())
        );
        response.setStudentName(student != null ? student.getName() : submission.getStudentUsername());

        return response;
    }

    /**
     * 查询课程的步骤提交列表（教师端，使用 classExperimentId）
     *
     * @param classExperimentId 班级实验ID
     * @param submissionStatus  提交状态（可选）
     * @return 步骤列表
     */
    public List<StudentProcedureSubmissionResponse> getCourseSubmissionsByClassExperimentId(
            Long classExperimentId, Integer submissionStatus) {

        // 1. 查询班级实验信息
        ClassExperiment classExperiment = classExperimentMapper.selectById(classExperimentId);
        if (classExperiment == null) {
            throw new BusinessException(404, "班级实验不存在");
        }

        // 2. 按课次精确查询提交，兼容历史数据回退
        List<StudentExperimentalProcedure> submissions = listSubmissionsByClassExperimentId(classExperimentId, classExperiment);

        // 3. 按提交状态过滤
        if (submissionStatus != null) {
            submissions = submissions.stream()
                    .filter(item -> submissionStatus.equals(item.getIsGraded()))
                    .toList();
        }

        return submissions.stream().map(this::buildResponse).collect(Collectors.toList());
    }

    private List<StudentExperimentalProcedure> listSubmissionsByClassExperimentId(Long classExperimentId,
                                                                                   ClassExperiment classExperiment) {
        LinkedHashMap<Long, StudentExperimentalProcedure> submissionMap = new LinkedHashMap<>();

        LambdaQueryWrapper<StudentExperimentalProcedure> exactQuery = new LambdaQueryWrapper<>();
        exactQuery.eq(StudentExperimentalProcedure::getClassExperimentId, classExperimentId)
                .orderByDesc(StudentExperimentalProcedure::getCreatedTime);
        List<StudentExperimentalProcedure> exactMatches = studentExperimentalProcedureMapper.selectList(exactQuery);
        exactMatches.forEach(item -> submissionMap.put(item.getId(), item));

        if (!submissionMap.isEmpty()) {
            return new ArrayList<>(submissionMap.values());
        }

        List<String> classCodes = classExperimentClassRelationService.getClassCodesByExperimentId(classExperimentId);
        if (classCodes == null || classCodes.isEmpty()) {
            throw new BusinessException(404, "班级实验未关联任何班级");
        }

        // 仅在当前课次没有精确落库数据时，才回退到历史兼容查询
        Long experimentId = Long.parseLong(classExperiment.getExperimentId());
        LambdaQueryWrapper<StudentExperimentalProcedure> fallbackQuery = new LambdaQueryWrapper<>();
        fallbackQuery.eq(StudentExperimentalProcedure::getExperimentId, experimentId)
                .in(StudentExperimentalProcedure::getClassCode, classCodes)
                .isNull(StudentExperimentalProcedure::getClassExperimentId)
                .orderByDesc(StudentExperimentalProcedure::getCreatedTime);
        List<StudentExperimentalProcedure> fallbackMatches = studentExperimentalProcedureMapper.selectList(fallbackQuery);
        fallbackMatches.forEach(item -> submissionMap.put(item.getId(), item));
        return new ArrayList<>(submissionMap.values());
    }


    @Data
    @AllArgsConstructor
    public static class ReAutoGradeSummary {
        private int scannedCount;
        private int reAutoGradedCount;
        private int skippedTeacherGradedCount;
        private int skippedUnsupportedCount;
        private int failedCount;

        public ReAutoGradeSummary() {
        }
    }
}
