package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.StudentExperimentalProcedureMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.pojo.entity.StudentExperimentalProcedure;
import com.example.demo.pojo.entity.User;
import com.example.demo.pojo.response.StudentProcedureSubmissionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
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
}
