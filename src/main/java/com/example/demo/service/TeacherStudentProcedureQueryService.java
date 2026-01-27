package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.mapper.*;
import com.example.demo.pojo.entity.*;
import com.example.demo.pojo.response.ClassExperimentStatisticsResponse;
import com.example.demo.pojo.response.StudentProcedureCompletionResponse;
import com.example.demo.pojo.response.StudentProcedureDetailCompletionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 教师查询学生步骤完成情况服务
 * 提供教师查询学生实验步骤完成情况的服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TeacherStudentProcedureQueryService {

    private final ExperimentMapper experimentMapper;
    private final ExperimentalProcedureService experimentalProcedureService;
    private final StudentExperimentalProcedureService studentExperimentalProcedureService;
    private final StudentProcedureAttachmentMapper studentProcedureAttachmentMapper;
    private final ProcedureTopicMapMapper procedureTopicMapMapper;
    private final TopicMapper topicMapper;
    private final DataCollectionMapper dataCollectionMapper;
    private final VideoFileMapper videoFileMapper;

    /**
     * 查询学生在指定班级实验中的步骤完成情况
     *
     * @param studentUsername 学生用户名
     * @param classCode       班级编号
     * @param experimentId    实验ID
     * @return 步骤完成情况
     */
    public StudentProcedureCompletionResponse getStudentProcedureCompletion(
            String studentUsername, String classCode, Long experimentId) {

        log.info("查询学生实验步骤完成情况，学生: {}, 班级: {}, 实验: {}",
                studentUsername, classCode, experimentId);

        // 1. 查询实验基本信息
        Experiment experiment = experimentMapper.selectById(experimentId);
        if (experiment == null) {
            throw new com.example.demo.exception.BusinessException(404, "实验不存在");
        }

        // 2. 查询实验的所有步骤
        List<ExperimentalProcedure> procedures = experimentalProcedureService.getByExperimentId(experimentId);

        // 3. 查询学生的步骤完成记录
        List<StudentExperimentalProcedure> studentProcedures =
                studentExperimentalProcedureService.getByStudentAndExperiment(
                        studentUsername, classCode, experimentId);

        // 4. 构建步骤完成列表
        List<StudentProcedureCompletionResponse.ProcedureCompletion> completionList = new ArrayList<>();
        BigDecimal totalScore = BigDecimal.ZERO;

        for (ExperimentalProcedure procedure : procedures) {
            StudentProcedureCompletionResponse.ProcedureCompletion completion =
                    new StudentProcedureCompletionResponse.ProcedureCompletion();

            completion.setId(procedure.getId());
            completion.setNumber(procedure.getNumber());
            completion.setType(procedure.getType());
            completion.setRemark(procedure.getRemark());
            completion.setProportion(procedure.getProportion());

            // 查找学生的完成记录
            StudentExperimentalProcedure studentProcedure = studentProcedures.stream()
                    .filter(sp -> sp.getExperimentalProcedureId().equals(procedure.getId()))
                    .findFirst()
                    .orElse(null);

            if (studentProcedure != null && studentProcedure.getAnswer() != null
                    && !studentProcedure.getAnswer().trim().isEmpty()) {
                completion.setIsCompleted(true);
                completion.setSubmissionTime(studentProcedure.getCreatedTime());
                completion.setScore(studentProcedure.getScore());
                if (studentProcedure.getScore() != null) {
                    totalScore = totalScore.add(studentProcedure.getScore());
                }
            } else {
                completion.setIsCompleted(false);
            }

            completionList.add(completion);
        }

        // 5. 计算总进度
        long completedCount = completionList.stream()
                .filter(StudentProcedureCompletionResponse.ProcedureCompletion::getIsCompleted)
                .count();
        String progress = completedCount + "/" + completionList.size();

        // 6. 构建响应
        StudentProcedureCompletionResponse response = new StudentProcedureCompletionResponse();
        response.setStudentUsername(studentUsername);
        response.setClassCode(classCode);
        response.setExperimentId(experimentId);
        response.setExperimentName(experiment.getExperimentName());
        response.setProcedures(completionList);
        response.setProgress(progress);
        response.setTotalScore(totalScore);

        return response;
    }

    /**
     * 查询学生在指定步骤的完成详情
     *
     * @param studentUsername 学生用户名
     * @param classCode       班级编号
     * @param procedureId     步骤ID
     * @return 步骤完成详情
     */
    public StudentProcedureDetailCompletionResponse getStudentProcedureDetailCompletion(
            String studentUsername, String classCode, Long procedureId) {

        log.info("查询学生步骤完成详情，学生: {}, 班级: {}, 步骤: {}",
                studentUsername, classCode, procedureId);

        // 1. 查询步骤基本信息
        ExperimentalProcedure procedure = experimentalProcedureService.getById(procedureId);
        if (procedure == null) {
            throw new com.example.demo.exception.BusinessException(404, "步骤不存在");
        }

        // 2. 查询学生的步骤完成记录
        QueryWrapper<StudentExperimentalProcedure> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("student_username", studentUsername)
                .eq("class_code", classCode)
                .eq("experimental_procedure_id", procedureId);
        StudentExperimentalProcedure studentProcedure = studentExperimentalProcedureService.getOne(queryWrapper);

        // 3. 构建响应
        StudentProcedureDetailCompletionResponse response = new StudentProcedureDetailCompletionResponse();
        response.setStudentUsername(studentUsername);
        response.setClassCode(classCode);
        response.setProcedureId(procedureId);
        response.setNumber(procedure.getNumber());
        response.setType(procedure.getType());
        response.setRemark(procedure.getRemark());

        if (studentProcedure != null && studentProcedure.getAnswer() != null
                && !studentProcedure.getAnswer().trim().isEmpty()) {
            response.setIsCompleted(true);
            response.setSubmissionTime(studentProcedure.getCreatedTime());
            response.setScore(studentProcedure.getScore());
            response.setTeacherComment(studentProcedure.getTeacherComment());
            response.setAnswer(studentProcedure.getAnswer());

            // 根据步骤类型查询详细信息
            fillProcedureCompletionDetail(response, procedure, studentProcedure);
        } else {
            response.setIsCompleted(false);
        }

        return response;
    }

    /**
     * 填充步骤完成详情
     */
    private void fillProcedureCompletionDetail(
            StudentProcedureDetailCompletionResponse response,
            ExperimentalProcedure procedure,
            StudentExperimentalProcedure studentProcedure) {

        Integer type = procedure.getType();
        if (type == null) {
            return;
        }

        switch (type) {
            case 2:
                // 类型2：数据收集 - 查询附件
                fillDataCollectionAttachments(response, procedure);
                break;
            case 3:
                // 类型3：题库答题 - 解析答案并查询题目
                fillTopicAnswers(response, procedure, studentProcedure);
                break;
            default:
                break;
        }
    }

    /**
     * 填充数据收集附件信息
     */
    private void fillDataCollectionAttachments(
            StudentProcedureDetailCompletionResponse response,
            ExperimentalProcedure procedure) {

        QueryWrapper<StudentProcedureAttachment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("procedure_id", procedure.getId())
                .eq("student_username", response.getStudentUsername())
                .eq("class_code", response.getClassCode())
                .orderByDesc("created_time");

        List<StudentProcedureAttachment> attachments = studentProcedureAttachmentMapper.selectList(queryWrapper);
        if (attachments == null || attachments.isEmpty()) {
            return;
        }

        List<StudentProcedureDetailCompletionResponse.AttachmentInfo> photos = new ArrayList<>();
        List<StudentProcedureDetailCompletionResponse.AttachmentInfo> documents = new ArrayList<>();

        for (StudentProcedureAttachment attachment : attachments) {
            StudentProcedureDetailCompletionResponse.AttachmentInfo info =
                    new StudentProcedureDetailCompletionResponse.AttachmentInfo();
            info.setId(attachment.getId());
            info.setFileFormat(attachment.getFileFormat());
            info.setOriginalFileName(attachment.getOriginalFileName());
            info.setStoredFileName(attachment.getStoredFileName());
            info.setFilePath(attachment.getFilePath());
            info.setFileSize(attachment.getFileSize());
            info.setUploadTime(attachment.getCreateTime());

            if (attachment.getFileType() == 1) {
                photos.add(info);
            } else if (attachment.getFileType() == 2) {
                documents.add(info);
            }
        }

        response.setPhotos(photos);
        response.setDocuments(documents);
    }

    /**
     * 填充题库答案详情
     */
    private void fillTopicAnswers(
            StudentProcedureDetailCompletionResponse response,
            ExperimentalProcedure procedure,
            StudentExperimentalProcedure studentProcedure) {

        // 1. 解析学生答案字符串
        String answerString = studentProcedure.getAnswer();
        if (answerString == null || answerString.trim().isEmpty()) {
            return;
        }

        // 格式: topicId:answer;topicId:answer;
        List<StudentProcedureDetailCompletionResponse.TopicAnswer> topicAnswers = new ArrayList<>();
        String[] answerPairs = answerString.split(";");

        for (String pair : answerPairs) {
            if (pair.trim().isEmpty()) {
                continue;
            }
            String[] parts = pair.split(":");
            if (parts.length != 2) {
                continue;
            }

            try {
                Long topicId = Long.parseLong(parts[0]);
                String studentAnswer = parts[1];

                // 2. 查询题目信息
                Topic topic = topicMapper.selectById(topicId);
                if (topic == null) {
                    continue;
                }

                StudentProcedureDetailCompletionResponse.TopicAnswer topicAnswer =
                        new StudentProcedureDetailCompletionResponse.TopicAnswer();
                topicAnswer.setTopicId(topic.getId());
                topicAnswer.setNumber(topic.getNumber());
                topicAnswer.setType(topic.getType());
                topicAnswer.setContent(topic.getContent());
                topicAnswer.setChoices(topic.getChoices());
                topicAnswer.setStudentAnswer(studentAnswer);
                topicAnswer.setCorrectAnswer(topic.getCorrectAnswer());

                // 3. 判断答案是否正确
                boolean isCorrect = studentAnswer.equals(topic.getCorrectAnswer());
                topicAnswer.setIsCorrect(isCorrect);

                topicAnswers.add(topicAnswer);
            } catch (NumberFormatException e) {
                log.warn("解析题目ID失败: {}", parts[0]);
            }
        }

        response.setTopicAnswers(topicAnswers);
    }

    /**
     * 查询班级实验完成统计
     *
     * @param classCode    班级编号
     * @param experimentId 实验ID
     * @return 班级实验完成统计
     */
    public ClassExperimentStatisticsResponse getClassExperimentStatistics(
            String classCode, Long experimentId) {

        log.info("查询班级实验完成统计，班级: {}, 实验: {}", classCode, experimentId);

        // 1. 查询实验基本信息
        Experiment experiment = experimentMapper.selectById(experimentId);
        if (experiment == null) {
            throw new com.example.demo.exception.BusinessException(404, "实验不存在");
        }

        // 2. 查询实验的所有步骤
        List<ExperimentalProcedure> procedures = experimentalProcedureService.getByExperimentId(experimentId);

        // 3. 查询班级中所有学生提交记录
        QueryWrapper<StudentExperimentalProcedure> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("class_code", classCode);
        List<StudentExperimentalProcedure> allStudentProcedures = studentExperimentalProcedureService.list(queryWrapper);

        // 4. 获取唯一学生列表
        List<String> studentUsernames = allStudentProcedures.stream()
                .map(StudentExperimentalProcedure::getStudentUsername)
                .distinct()
                .collect(Collectors.toList());

        int totalStudents = studentUsernames.size();

        // 5. 统计每个步骤的完成情况
        List<ClassExperimentStatisticsResponse.ProcedureStatistics> procedureStatisticsList = new ArrayList<>();
        BigDecimal totalScoreSum = BigDecimal.ZERO;
        int submittedStudents = 0;

        for (ExperimentalProcedure procedure : procedures) {
            ClassExperimentStatisticsResponse.ProcedureStatistics procStat =
                    new ClassExperimentStatisticsResponse.ProcedureStatistics();
            procStat.setId(procedure.getId());
            procStat.setNumber(procedure.getNumber());
            procStat.setType(procedure.getType());
            procStat.setRemark(procedure.getRemark());

            // 统计该步骤的完成情况
            List<StudentExperimentalProcedure> procedureSubmissions = allStudentProcedures.stream()
                    .filter(sp -> sp.getExperimentalProcedureId().equals(procedure.getId()))
                    .filter(sp -> sp.getAnswer() != null && !sp.getAnswer().trim().isEmpty())
                    .collect(Collectors.toList());

            int completedCount = procedureSubmissions.size();
            int notCompletedCount = totalStudents - completedCount;

            procStat.setCompletedCount(completedCount);
            procStat.setNotCompletedCount(notCompletedCount);

            // 计算完成率
            BigDecimal completionRate = totalStudents > 0
                    ? new BigDecimal(completedCount)
                            .multiply(new BigDecimal(100))
                            .divide(new BigDecimal(totalStudents), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            procStat.setCompletionRate(completionRate);

            // 计算平均分
            BigDecimal avgScore = procedureSubmissions.stream()
                    .filter(sp -> sp.getScore() != null)
                    .map(StudentExperimentalProcedure::getScore)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            avgScore = completedCount > 0
                    ? avgScore.divide(new BigDecimal(completedCount), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            procStat.setAverageScore(avgScore);

            procedureStatisticsList.add(procStat);
        }

        // 6. 统计每个学生的完成情况
        List<ClassExperimentStatisticsResponse.StudentCompletionInfo> studentCompletions = new ArrayList<>();

        for (String studentUsername : studentUsernames) {
            // 获取该学生的所有提交记录
            List<StudentExperimentalProcedure> studentSubmissions = allStudentProcedures.stream()
                    .filter(sp -> sp.getStudentUsername().equals(studentUsername))
                    .filter(sp -> sp.getAnswer() != null && !sp.getAnswer().trim().isEmpty())
                    .collect(Collectors.toList());

            if (studentSubmissions.isEmpty()) {
                continue;
            }

            // 统计完成进度
            int completedCount = (int) studentSubmissions.stream()
                    .map(StudentExperimentalProcedure::getExperimentalProcedureId)
                    .distinct()
                    .count();

            // 计算总分
            BigDecimal totalScore = studentSubmissions.stream()
                    .filter(sp -> sp.getScore() != null)
                    .map(StudentExperimentalProcedure::getScore)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 获取最后提交时间
            LocalDateTime lastSubmissionTime = studentSubmissions.stream()
                    .map(StudentExperimentalProcedure::getCreatedTime)
                    .max(LocalDateTime::compareTo)
                    .orElse(null);

            // 累加到总分
            totalScoreSum = totalScoreSum.add(totalScore);

            ClassExperimentStatisticsResponse.StudentCompletionInfo studentInfo =
                    new ClassExperimentStatisticsResponse.StudentCompletionInfo();
            studentInfo.setStudentUsername(studentUsername);
            studentInfo.setCompletedCount(completedCount);
            studentInfo.setTotalCount(procedures.size());
            studentInfo.setProgress(completedCount + "/" + procedures.size());
            studentInfo.setTotalScore(totalScore);
            studentInfo.setLastSubmissionTime(lastSubmissionTime);

            studentCompletions.add(studentInfo);
            submittedStudents++;
        }

        // 7. 计算总体统计
        BigDecimal overallCompletionRate = totalStudents > 0
                ? new BigDecimal(submittedStudents)
                        .multiply(new BigDecimal(100))
                        .divide(new BigDecimal(totalStudents), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal overallAverageScore = submittedStudents > 0
                ? totalScoreSum.divide(new BigDecimal(submittedStudents), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // 8. 构建响应
        ClassExperimentStatisticsResponse response = new ClassExperimentStatisticsResponse();
        response.setClassCode(classCode);
        response.setExperimentId(experimentId);
        response.setExperimentName(experiment.getExperimentName());
        response.setTotalStudents(totalStudents);
        response.setSubmittedCount(submittedStudents);
        response.setCompletionRate(overallCompletionRate);
        response.setAverageScore(overallAverageScore);
        response.setProcedureStatistics(procedureStatisticsList);
        response.setStudentCompletions(studentCompletions);

        return response;
    }
}
