package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.exception.BusinessException;
import com.example.demo.pojo.dto.mapvo.FillBlankAnswer;
import com.example.demo.pojo.dto.mapvo.TableCellAnswer;
import com.example.demo.mapper.*;
import com.example.demo.pojo.entity.*;
import com.example.demo.pojo.response.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.lang.Class;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import com.example.demo.util.ScoreCalculationUtil;

import java.util.*;
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
    private final ClassExperimentMapper classExperimentMapper;
    private final ExperimentalProcedureService experimentalProcedureService;
    private final StudentExperimentalProcedureService studentExperimentalProcedureService;
    private final StudentProcedureAttachmentMapper studentProcedureAttachmentMapper;
    private final ProcedureTopicMapMapper procedureTopicMapMapper;
    private final TopicMapper topicMapper;
    private final TopicTagMapMapper topicTagMapMapper;
    private final ProcedureTopicMapper procedureTopicMapper;
    private final DataCollectionMapper dataCollectionMapper;
    private final VideoFileMapper videoFileMapper;
    private final TimedQuizProcedureMapper timedQuizProcedureMapper;
    private final ClassExperimentClassRelationService classExperimentClassRelationService;
    private final DownloadService downloadService;
    private final StudentClassRelationMapper studentClassRelationMapper;
    private final UserMapper userMapper;
    private final CourseMapper courseMapper;
    private final StudentClassRelationService studentClassRelationService;

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
                completion.setIsGraded(studentProcedure.getIsGraded());
                if (studentProcedure.getScore() != null) {
                    totalScore = totalScore.add(studentProcedure.getScore());
                }
                completion.setSubmissionId(studentProcedure.getId());
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
        LambdaQueryWrapper<StudentExperimentalProcedure> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StudentExperimentalProcedure::getStudentUsername, studentUsername)
                .eq(StudentExperimentalProcedure::getExperimentalProcedureId, procedureId);
        List<StudentExperimentalProcedure> studentProcedures = studentExperimentalProcedureService.list(queryWrapper);

        // 从多条记录中找到匹配班级的记录
        StudentExperimentalProcedure studentProcedure = null;
        if (studentProcedures != null && !studentProcedures.isEmpty()) {
            // 通过关联表查询班级实验ID列表
            List<Long> experimentIds = classExperimentClassRelationService.getExperimentIdsByClassCode(classCode);
            for (StudentExperimentalProcedure sp : studentProcedures) {
                if (experimentIds.contains(sp.getId())) {
                    studentProcedure = sp;
                    break;
                }
            }
        }

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
            response.setIsGraded(studentProcedure.getIsGraded());

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
                fillDataCollectionAttachments(response, procedure, studentProcedure);
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
            ExperimentalProcedure procedure,
            StudentExperimentalProcedure studentProcedure) {

        // 1. 解析学生答案 JSON，设置 fillBlankAnswers 和 tableCellAnswers
        String answerString = studentProcedure.getAnswer();
        if (answerString != null && !answerString.trim().isEmpty()) {
            // 使用工具类解析 data 字段（支持嵌套对象）
            java.util.Map<String, Object> dataMap = com.example.demo.util.AnswerMapJSONUntil.parseDataAsObject(answerString);

            @SuppressWarnings("unchecked")
            java.util.Map<String, String> fillBlankAnswers = (java.util.Map<String, String>) dataMap.get("fillBlankAnswers");

            @SuppressWarnings("unchecked")
            java.util.Map<String, String> tableCellAnswers = (java.util.Map<String, String>) dataMap.get("tableCellAnswers");

            response.setFillBlankAnswers(FillBlankAnswer.fromMap(fillBlankAnswers));
            response.setTableCellAnswers(TableCellAnswer.fromMap(tableCellAnswers));
        }

        // 2. 查询附件信息
        LambdaQueryWrapper<StudentProcedureAttachment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StudentProcedureAttachment::getProcedureId, procedure.getId())
                .eq(StudentProcedureAttachment::getStudentUsername, response.getStudentUsername())
                .eq(StudentProcedureAttachment::getClassExperimentId, studentProcedure.getClassExperimentId())
                .orderByDesc(StudentProcedureAttachment::getCreateTime);

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

            // 生成文件下载密钥
            String currentUsername = com.example.demo.util.SecurityUtil.getCurrentUsername().orElse(null);
            if (currentUsername != null) {
                String downloadKey = downloadService.generateFileKey(
                    DownloadService.TYPE_ATTACHMENT, attachment.getId(), currentUsername);
                info.setDownloadKey(downloadKey);
            }

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

        // 使用工具类解析答案（新格式：{"type":"TOPIC","data":{"题目ID":"答案"}}）
        Map<Long, String> studentAnswers = parseTopicAnswers(answerString);

        List<StudentProcedureDetailCompletionResponse.TopicAnswer> topicAnswers = new ArrayList<>();

        for (Map.Entry<Long, String> entry : studentAnswers.entrySet()) {
            Long topicId = entry.getKey();
            String studentAnswer = entry.getValue();

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
            boolean isCorrect = studentAnswer != null && studentAnswer.equals(topic.getCorrectAnswer());
            topicAnswer.setIsCorrect(isCorrect);

            topicAnswers.add(topicAnswer);
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

        ClassExperimentStatisticsResponse response = new ClassExperimentStatisticsResponse();

        response.setClassCode(classCode);
        response.setExperimentId(experimentId);

        //获取实验信息
        Experiment experiment = experimentMapper.selectById(experimentId);
        if (experiment == null) {
            throw new BusinessException("实验不存在");
        }
        response.setExperimentName(experiment.getExperimentName());

        //获取授课老师信息
        Course course = courseMapper.selectOne(new LambdaQueryWrapper<Course>().eq(Course::getCourseId,experiment.getCourseId()));
        if(course == null){
            return new ClassExperimentStatisticsResponse();
        }
        response.setUserName(userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername,course.getTeacherUsername())).getName());

        //获取班级人员
        List<StudentClassRelation> studentClassRelations = studentClassRelationMapper.selectList(new LambdaQueryWrapper<StudentClassRelation>().eq(StudentClassRelation::getClassCode,classCode));
        response.setTotalStudents(studentClassRelations.size());

        List<String> studentUserName = studentClassRelations.stream().map(StudentClassRelation::getStudentUsername).toList();
        if(studentUserName.isEmpty()){
            return new ClassExperimentStatisticsResponse();
        }

        // 查询实验的所有步骤
        List<ExperimentalProcedure> procedures = experimentalProcedureService.list(
            new LambdaQueryWrapper<ExperimentalProcedure>()
                .eq(ExperimentalProcedure::getExperimentId, experimentId)
                .eq(ExperimentalProcedure::getIsDeleted, false)
                .orderByAsc(ExperimentalProcedure::getNumber)
        );
        int totalProcedures = procedures.size();

        // 筛选占比不为零的步骤（必须完成的步骤）
        List<Long> requiredProcedureIds = procedures.stream()
            .filter(p -> p.getProportion() != null && p.getProportion() > 0)
            .map(ExperimentalProcedure::getId)
            .collect(Collectors.toList());

        // 查询班级学生在该实验的所有步骤提交记录
        List<StudentExperimentalProcedure> studentProcedures;
        studentProcedures = studentExperimentalProcedureService.list(
            new LambdaQueryWrapper<StudentExperimentalProcedure>()
                    .eq(StudentExperimentalProcedure::getExperimentId, experimentId)
                    .in(StudentExperimentalProcedure::getStudentUsername, studentUserName)
        );

        // 按学生分组
        Map<String, List<StudentExperimentalProcedure>> studentProcedureMap = studentProcedures.isEmpty()
            ? Collections.emptyMap()
            : studentProcedures.stream()
                .collect(Collectors.groupingBy(StudentExperimentalProcedure::getStudentUsername));

        // 已提交人数（至少提交一个步骤的学生）
        int submittedCount = studentProcedureMap.size();
        if(submittedCount == 0){
            return new ClassExperimentStatisticsResponse();
        }

        // 完成率
        BigDecimal completionRate = response.getTotalStudents() > 0
            ? BigDecimal.valueOf(submittedCount).divide(BigDecimal.valueOf(response.getTotalStudents()), 4, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;

        // 计算平均分（只计算非 0 分学生的平均分）
        BigDecimal totalScoreSum = BigDecimal.ZERO;
        int validStudentCount = 0;  // 非 0 分学生数量
        for (String username : studentUserName) {
            List<StudentExperimentalProcedure> studentProcList = studentProcedureMap.getOrDefault(username, Collections.emptyList());
            BigDecimal studentScore = ScoreCalculationUtil.calculateExperimentScore(procedures, studentProcList);
            // 只统计非 0 分的学生
            if (studentScore.compareTo(BigDecimal.ZERO) > 0) {
                totalScoreSum = totalScoreSum.add(studentScore);
                validStudentCount++;
            }
        }

        BigDecimal averageScore = validStudentCount > 0
            ? totalScoreSum.divide(BigDecimal.valueOf(validStudentCount), 2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;

        // 构建步骤统计列表
        List<ClassExperimentStatisticsResponse.ProcedureStatistics> procedureStats = procedures.stream().map(procedure -> {
            ClassExperimentStatisticsResponse.ProcedureStatistics stat = new ClassExperimentStatisticsResponse.ProcedureStatistics();
            stat.setId(procedure.getId());
            stat.setNumber(procedure.getNumber());
            stat.setType(procedure.getType());
            stat.setRemark(procedure.getRemark());

            // 统计该步骤的完成人数（有记录且 answer 不为空）
            int completedCount = (int) studentProcedures.stream()
                .filter(sp -> sp.getExperimentalProcedureId().equals(procedure.getId()))
                .filter(sp -> sp.getAnswer() != null && !sp.getAnswer().trim().isEmpty())
                .count();

            stat.setCompletedCount(completedCount);
            stat.setNotCompletedCount(response.getTotalStudents() - completedCount);
            stat.setCompletionRate(response.getTotalStudents() > 0
                ? BigDecimal.valueOf(completedCount).divide(BigDecimal.valueOf(response.getTotalStudents()), 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO);

            // 计算该步骤的平均分（仅计算已批改的）
            List<BigDecimal> scores = studentProcedures.stream()
                .filter(sp -> sp.getExperimentalProcedureId().equals(procedure.getId()))
                .filter(sp -> sp.getScore() != null && sp.getIsGraded() != null && sp.getIsGraded() > 0)
                .map(StudentExperimentalProcedure::getScore)
                .toList();

            BigDecimal avgScore = scores.isEmpty() ? BigDecimal.ZERO
                : scores.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(scores.size()), 2, RoundingMode.HALF_UP);
            stat.setAverageScore(avgScore);

            return stat;
        }).collect(Collectors.toList());

        // 构建学生完成列表
        List<ClassExperimentStatisticsResponse.StudentCompletionInfo> studentCompletions = studentUserName.stream().map(username -> {
            ClassExperimentStatisticsResponse.StudentCompletionInfo info = new ClassExperimentStatisticsResponse.StudentCompletionInfo();
            info.setStudentUsername(username);

            // 获取学生姓名
            User student = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));
            if (student != null) {
                info.setStudentName(student.getName());
            }

            List<StudentExperimentalProcedure> studentProcList = studentProcedureMap.getOrDefault(username, Collections.emptyList());

            // 已完成步骤数（有记录且 answer 不为空）
            int completedCount = (int) studentProcList.stream()
                .filter(sp -> sp.getAnswer() != null && !sp.getAnswer().trim().isEmpty())
                .count();

            info.setCompletedCount(completedCount);
            info.setTotalCount(totalProcedures);
            info.setProgress(completedCount + "/" + totalProcedures);

            // 使用业务规则计算总得分
            BigDecimal totalScore = ScoreCalculationUtil.calculateExperimentScore(procedures, studentProcList);
            info.setTotalScore(totalScore);

            // 最后提交时间
            studentProcList.stream()
                .map(StudentExperimentalProcedure::getUpdatedTime)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .ifPresent(info::setLastSubmissionTime);

            return info;
        }).collect(Collectors.toList());

        // 设置响应
        response.setSubmittedCount(submittedCount);
        response.setCompletionRate(completionRate);
        response.setAverageScore(averageScore);
        response.setProcedureStatistics(procedureStats);
        response.setStudentCompletions(studentCompletions);

        return response;
    }

    /**
     * 查询指定学生已提交的步骤详情（带答案）
     * 教师可以随时查看正确答案
     *
     * @param studentUsername 学生用户名
     * @param courseId   课程ID
     * @param experimentId 实验ID
     * @param procedureId 步骤ID
     * @return 步骤详情（带答案）
     */
    public StudentProcedureDetailWithAnswerResponse getStudentCompletedProcedureDetail(
            String studentUsername, String courseId, Long experimentId, Long procedureId) {

        log.info("教师查询学生已提交步骤详情，学生: {}, 课程: {}, 实验: {}, 步骤: {}",
                studentUsername, courseId, experimentId, procedureId);

        // 查询步骤基本信息
        ExperimentalProcedure procedure = experimentalProcedureService.getById(procedureId);
        if (procedure == null) {
            throw new com.example.demo.exception.BusinessException(404, "步骤不存在");
        }

        if (!procedure.getExperimentId().equals(experimentId)) {
            throw new com.example.demo.exception.BusinessException(400, "步骤与实验不匹配");
        }

        // 查询学生提交记录
        LambdaQueryWrapper<StudentExperimentalProcedure> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudentExperimentalProcedure::getExperimentalProcedureId, procedureId);
        wrapper.eq(StudentExperimentalProcedure::getStudentUsername, studentUsername);
        StudentExperimentalProcedure studentProcedure = studentExperimentalProcedureService.getOne(wrapper);

        if (studentProcedure == null) {
            throw new com.example.demo.exception.BusinessException(404, "未找到提交记录");
        }

        // 教师可以随时查看答案，所以isAfterEndTime始终为true
        boolean isAfterEndTime = true;

        StudentProcedureDetailWithAnswerResponse response = new StudentProcedureDetailWithAnswerResponse();
        response.setId(procedure.getId());
        response.setNumber(procedure.getNumber());
        response.setType(procedure.getType());
        response.setRemark(procedure.getRemark());
        response.setProportion(procedure.getProportion());
        response.setSubmissionTime(studentProcedure.getCreatedTime());
        response.setScore(studentProcedure.getScore());
        response.setTeacherComment(studentProcedure.getTeacherComment());
        response.setIsGraded(studentProcedure.getIsGraded());
        response.setIsAfterEndTime(isAfterEndTime);

        // 根据步骤类型填充详细信息
        fillCompletedProcedureDetailByTypeForTeacher(response, procedure, studentProcedure, studentUsername, isAfterEndTime);

        return response;
    }

    /**
     * 查询指定学生未提交的步骤详情
     *
     * @param studentUsername 学生用户名
     * @param courseId   课程ID
     * @param experimentId 实验ID
     * @param procedureId 步骤ID
     * @return 步骤详情（不含答案）
     */
    public StudentProcedureDetailWithoutAnswerResponse getStudentUncompletedProcedureDetail(
            String studentUsername, String courseId, Long experimentId, Long procedureId) {

        log.info("教师查询学生未提交步骤详情，学生: {}, 课程: {}, 实验: {}, 步骤: {}",
                studentUsername, courseId, experimentId, procedureId);

        // 查询步骤基本信息
        ExperimentalProcedure procedure = experimentalProcedureService.getById(procedureId);
        if (procedure == null) {
            throw new com.example.demo.exception.BusinessException(404, "步骤不存在");
        }

        if (!procedure.getExperimentId().equals(experimentId)) {
            throw new com.example.demo.exception.BusinessException(400, "步骤与实验不匹配");
        }

        // 检查是否已提交
        LambdaQueryWrapper<StudentExperimentalProcedure> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudentExperimentalProcedure::getExperimentalProcedureId, procedureId);
        wrapper.eq(StudentExperimentalProcedure::getStudentUsername, studentUsername);
        StudentExperimentalProcedure studentProcedure = studentExperimentalProcedureService.getOne(wrapper);

        if (studentProcedure != null) {
            throw new com.example.demo.exception.BusinessException(400, "步骤已提交，请使用已提交接口查询");
        }

        StudentProcedureDetailWithoutAnswerResponse response = new StudentProcedureDetailWithoutAnswerResponse();
        response.setId(procedure.getId());
        response.setNumber(procedure.getNumber());
        response.setType(procedure.getType());
        response.setRemark(procedure.getRemark());
        response.setProportion(procedure.getProportion());

        // 根据步骤类型填充详细信息
        fillUncompletedProcedureDetailByTypeForTeacher(response, procedure);

        return response;
    }

    /**
     * 根据步骤类型填充已提交步骤的详细信息（教师版，始终返回答案）
     */
    private void fillCompletedProcedureDetailByTypeForTeacher(
            StudentProcedureDetailWithAnswerResponse response,
            ExperimentalProcedure procedure,
            StudentExperimentalProcedure studentProcedure,
            String username,
            boolean isAfterEndTime) {

        int type = procedure.getType();

        if (type == 1) {
            // 观看视频
            fillVideoDetailForCompletedTeacher(response, procedure);
        } else if (type == 2) {
            // 数据收集
            fillDataCollectionDetailForCompletedTeacher(response, procedure, username, isAfterEndTime);
        } else if (type == 3) {
            // 题库答题
            fillTopicDetailForCompletedTeacher(response, procedure, studentProcedure, isAfterEndTime);
        }
    }

    /**
     * 根据步骤类型填充未提交步骤的详细信息（教师版）
     */
    private void fillUncompletedProcedureDetailByTypeForTeacher(
            StudentProcedureDetailWithoutAnswerResponse response,
            ExperimentalProcedure procedure) {

        int type = procedure.getType();

        if (type == 1) {
            // 观看视频
            fillVideoDetailForUncompletedTeacher(response, procedure);
        } else if (type == 2) {
            // 数据收集
            fillDataCollectionDetailForUncompletedTeacher(response, procedure);
        } else if (type == 3) {
            // 题库答题
            fillTopicDetailForUncompletedTeacher(response, procedure);
        }
    }

    /**
     * 填充视频详情（已提交）- 教师版
     */
    private void fillVideoDetailForCompletedTeacher(
            StudentProcedureDetailWithAnswerResponse response,
            ExperimentalProcedure procedure) {

        if (procedure.getVideoId() != null) {
            VideoFile videoFile = videoFileMapper.selectById(procedure.getVideoId());
            if (videoFile != null) {
                StudentProcedureDetailWithAnswerResponse.VideoDetail detail =
                    new StudentProcedureDetailWithAnswerResponse.VideoDetail();
                detail.setId(videoFile.getId());
                detail.setTitle(videoFile.getOriginalFileName());
                detail.setSeconds(videoFile.getVideoSeconds());
                detail.setFilePath(videoFile.getFilePath());
                detail.setFileSize(videoFile.getFileSize());
                response.setVideoDetail(detail);
            }
        }
    }

    /**
     * 填充数据收集详情（已提交）- 教师版
     */
    private void fillDataCollectionDetailForCompletedTeacher(
            StudentProcedureDetailWithAnswerResponse response,
            ExperimentalProcedure procedure,
            String username,
            boolean isAfterEndTime) {

        if (procedure.getDataCollectionId() != null) {
            DataCollection dataCollection = dataCollectionMapper.selectById(procedure.getDataCollectionId());
            if (dataCollection != null) {
                StudentProcedureDetailWithAnswerResponse.DataCollectionDetail detail =
                    new StudentProcedureDetailWithAnswerResponse.DataCollectionDetail();
                detail.setId(dataCollection.getId());
                detail.setType(dataCollection.getType() != null ? dataCollection.getType().intValue() : null);
                detail.setRemark(dataCollection.getRemark());
                detail.setNeedPhoto(dataCollection.getNeedPhoto());
                detail.setNeedDoc(dataCollection.getNeedDoc());

                // 查询附件信息
                LambdaQueryWrapper<StudentProcedureAttachment> attachmentWrapper = new LambdaQueryWrapper<>();
                attachmentWrapper.eq(StudentProcedureAttachment::getProcedureId, procedure.getId());
                attachmentWrapper.eq(StudentProcedureAttachment::getStudentUsername, username);
                List<StudentProcedureAttachment> attachments = studentProcedureAttachmentMapper.selectList(attachmentWrapper);

                List<StudentProcedureDetailWithAnswerResponse.AttachmentInfo> photos = new ArrayList<>();
                List<StudentProcedureDetailWithAnswerResponse.AttachmentInfo> documents = new ArrayList<>();

                for (StudentProcedureAttachment attachment : attachments) {
                    StudentProcedureDetailWithAnswerResponse.AttachmentInfo info =
                        new StudentProcedureDetailWithAnswerResponse.AttachmentInfo();
                    info.setId(attachment.getId());
                    info.setFileType(attachment.getFileType());
                    info.setFileFormat(attachment.getFileFormat());
                    info.setOriginalFileName(attachment.getOriginalFileName());
                    info.setFileSize(attachment.getFileSize());
                    info.setRemark(attachment.getRemark());
                    info.setCreateTime(attachment.getCreateTime());

                    // 生成文件下载密钥
                    String currentUsername = com.example.demo.util.SecurityUtil.getCurrentUsername().orElse(null);
                    if (currentUsername != null) {
                        String downloadKey = downloadService.generateFileKey(
                            DownloadService.TYPE_ATTACHMENT, attachment.getId(), currentUsername);
                        info.setDownloadKey(downloadKey);
                    }

                    if (attachment.getFileType() == 1) {
                        photos.add(info);
                    } else if (attachment.getFileType() == 2) {
                        documents.add(info);
                    }
                }

                detail.setPhotos(photos);
                detail.setDocuments(documents);

                // 解析答案JSON
                String answer = studentExperimentalProcedureService.getOne(
                    new LambdaQueryWrapper<StudentExperimentalProcedure>()
                        .eq(StudentExperimentalProcedure::getExperimentalProcedureId, procedure.getId())
                        .eq(StudentExperimentalProcedure::getStudentUsername, username)
                ).getAnswer();

                if (answer != null && !answer.isEmpty()) {
                    // 使用工具类解析 data 字段（支持嵌套对象）
                    Map<String, Object> dataMap = com.example.demo.util.AnswerMapJSONUntil.parseDataAsObject(answer);

                    @SuppressWarnings("unchecked")
                    Map<String, String> fillBlankAnswers = (Map<String, String>) dataMap.get("fillBlankAnswers");
                    @SuppressWarnings("unchecked")
                    Map<String, String> tableCellAnswers = (Map<String, String>) dataMap.get("tableCellAnswers");

                    detail.setFillBlankAnswers(FillBlankAnswer.fromMap(fillBlankAnswers));
                    detail.setTableCellAnswers(TableCellAnswer.fromMap(tableCellAnswers));
                }

                // 教师始终可以查看正确答案
                if (dataCollection.getCorrectAnswer() != null) {
                    detail.setCorrectAnswer(dataCollection.getCorrectAnswer());
                }

                response.setDataCollectionDetail(detail);
            }
        }
    }

    /**
     * 填充题库详情（已提交）- 教师版，始终返回答案
     */
    private void fillTopicDetailForCompletedTeacher(
            StudentProcedureDetailWithAnswerResponse response,
            ExperimentalProcedure procedure,
            StudentExperimentalProcedure studentProcedure,
            boolean isAfterEndTime) {

        if (procedure.getProcedureTopicId() != null) {
            ProcedureTopic procedureTopic = procedureTopicMapper.selectById(procedure.getProcedureTopicId());
            if (procedureTopic != null) {
                StudentProcedureDetailWithAnswerResponse.TopicDetail detail =
                    new StudentProcedureDetailWithAnswerResponse.TopicDetail();
                detail.setId(procedureTopic.getId());
                detail.setIsRandom(procedureTopic.getIsRandom());
                detail.setNumber(procedureTopic.getNumber());
                detail.setTags(procedureTopic.getTags());

                // 查询题目列表
                List<Topic> topics = getTopicsForProcedureTeacher(procedureTopic);
                List<StudentProcedureDetailWithAnswerResponse.TopicItem> topicItems = new ArrayList<>();

                // 解析学生答案
                Map<Long, String> studentAnswers = parseTopicAnswers(studentProcedure.getAnswer());

                for (Topic topic : topics) {
                    StudentProcedureDetailWithAnswerResponse.TopicItem item =
                        new StudentProcedureDetailWithAnswerResponse.TopicItem();
                    item.setId(topic.getId());
                    item.setNumber(topic.getNumber());
                    item.setType(topic.getType());
                    item.setContent(topic.getContent());
                    item.setChoices(topic.getChoices());

                    String studentAnswer = studentAnswers.get(topic.getId());
                    item.setStudentAnswer(studentAnswer);

                    // 教师始终可以查看正确答案和是否正确
                    item.setCorrectAnswer(topic.getCorrectAnswer());

                    item.setIsCorrect(studentAnswer != null && studentAnswer.equals(topic.getCorrectAnswer()));

                    topicItems.add(item);
                }

                detail.setTopics(topicItems);
                response.setTopicDetail(detail);
            }
        }
    }

    /**
     * 填充视频详情（未提交）- 教师版
     */
    private void fillVideoDetailForUncompletedTeacher(
            StudentProcedureDetailWithoutAnswerResponse response,
            ExperimentalProcedure procedure) {

        if (procedure.getVideoId() != null) {
            VideoFile videoFile = videoFileMapper.selectById(procedure.getVideoId());
            if (videoFile != null) {
                StudentProcedureDetailWithoutAnswerResponse.VideoDetail detail =
                    new StudentProcedureDetailWithoutAnswerResponse.VideoDetail();
                detail.setId(videoFile.getId());
                detail.setTitle(videoFile.getOriginalFileName());
                detail.setSeconds(videoFile.getVideoSeconds());
                response.setVideoDetail(detail);
            }
        }
    }

    /**
     * 填充数据收集详情（未提交）- 教师版
     */
    private void fillDataCollectionDetailForUncompletedTeacher(
            StudentProcedureDetailWithoutAnswerResponse response,
            ExperimentalProcedure procedure) {

        if (procedure.getDataCollectionId() != null) {
            DataCollection dataCollection = dataCollectionMapper.selectById(procedure.getDataCollectionId());
            if (dataCollection != null) {
                StudentProcedureDetailWithoutAnswerResponse.DataCollectionDetail detail =
                    new StudentProcedureDetailWithoutAnswerResponse.DataCollectionDetail();
                detail.setId(dataCollection.getId());
                detail.setType(dataCollection.getType() != null ? dataCollection.getType().intValue() : null);
                detail.setRemark(dataCollection.getRemark());
                detail.setNeedPhoto(dataCollection.getNeedPhoto());
                detail.setNeedDoc(dataCollection.getNeedDoc());
                response.setDataCollectionDetail(detail);
            }
        }
    }

    /**
     * 填充题库详情（未提���）- 教师版
     */
    private void fillTopicDetailForUncompletedTeacher(
            StudentProcedureDetailWithoutAnswerResponse response,
            ExperimentalProcedure procedure) {

        if (procedure.getProcedureTopicId() != null) {
            ProcedureTopic procedureTopic = procedureTopicMapper.selectById(procedure.getProcedureTopicId());
            if (procedureTopic != null) {
                StudentProcedureDetailWithoutAnswerResponse.TopicDetail detail =
                    new StudentProcedureDetailWithoutAnswerResponse.TopicDetail();
                detail.setId(procedureTopic.getId());
                detail.setIsRandom(procedureTopic.getIsRandom());
                detail.setNumber(procedureTopic.getNumber());
                detail.setTags(procedureTopic.getTags());

                // 查询题目列表（不含答案）
                List<Topic> topics = getTopicsForProcedureTeacher(procedureTopic);
                List<StudentProcedureDetailWithoutAnswerResponse.TopicItem> topicItems = new ArrayList<>();

                for (Topic topic : topics) {
                    StudentProcedureDetailWithoutAnswerResponse.TopicItem item =
                        new StudentProcedureDetailWithoutAnswerResponse.TopicItem();
                    item.setId(topic.getId());
                    item.setNumber(topic.getNumber());
                    item.setType(topic.getType());
                    item.setContent(topic.getContent());
                    item.setChoices(topic.getChoices());
                    topicItems.add(item);
                }

                detail.setTopics(topicItems);
                response.setTopicDetail(detail);
            }
        }
    }

    /**
     * 根据题库详情获取题目列表（教师版）
     */
    private List<Topic> getTopicsForProcedureTeacher(ProcedureTopic procedureTopic) {
        if (procedureTopic.getIsRandom()) {
            // 随机抽取：根据标签过滤题目
            if (procedureTopic.getTags() != null && !procedureTopic.getTags().isEmpty()) {
                String[] tagIds = procedureTopic.getTags().split(",");
                List<Long> tagIdList = Arrays.stream(tagIds)
                    .filter(s -> s != null && !s.isEmpty())
                    .map(Long::parseLong)
                    .collect(Collectors.toList());

                if (!tagIdList.isEmpty()) {
                    LambdaQueryWrapper<TopicTagMap> tagWrapper = new LambdaQueryWrapper<>();
                    tagWrapper.in(TopicTagMap::getTagId, tagIdList);
                    List<TopicTagMap> topicTagMaps = topicTagMapMapper.selectList(tagWrapper);

                    if (!topicTagMaps.isEmpty()) {
                        List<Long> topicIds = topicTagMaps.stream()
                            .map(TopicTagMap::getTopicId)
                            .distinct()
                            .collect(Collectors.toList());

                        if (!topicIds.isEmpty()) {
                            LambdaQueryWrapper<Topic> topicWrapper = new LambdaQueryWrapper<>();
                            topicWrapper.in(Topic::getId, topicIds);

                            // 添加题目类型过滤
                            if (procedureTopic.getTopicTypes() != null && !procedureTopic.getTopicTypes().isEmpty()) {
                                String[] typeArray = procedureTopic.getTopicTypes().split(",");
                                List<Integer> types = Arrays.stream(typeArray)
                                    .filter(s -> s != null && !s.isEmpty())
                                    .map(Integer::parseInt)
                                    .collect(Collectors.toList());
                                if (!types.isEmpty()) {
                                    topicWrapper.in(Topic::getType, types);
                                }
                            }

                            topicWrapper.orderByAsc(Topic::getNumber);
                            return topicMapper.selectList(topicWrapper);
                        }
                    }
                }
            }
            return new ArrayList<>();
        } else {
            // 固定题目：从题库详情映射表查询
            LambdaQueryWrapper<ProcedureTopicMap> mapWrapper = new LambdaQueryWrapper<>();
            mapWrapper.eq(ProcedureTopicMap::getProcedureTopicId, procedureTopic.getId());
            mapWrapper.orderByAsc(ProcedureTopicMap::getId);
            List<ProcedureTopicMap> topicMaps = procedureTopicMapMapper.selectList(mapWrapper);

            if (!topicMaps.isEmpty()) {
                List<Long> topicIds = topicMaps.stream()
                    .map(ProcedureTopicMap::getTopicId)
                    .collect(Collectors.toList());

                LambdaQueryWrapper<Topic> topicWrapper = new LambdaQueryWrapper<>();
                topicWrapper.in(Topic::getId, topicIds);
                topicWrapper.orderByAsc(Topic::getNumber);
                return topicMapper.selectList(topicWrapper);
            }
            return new ArrayList<>();
        }
    }

    /**
     * 解析题库答案JSON
     */
    private Map<Long, String> parseTopicAnswers(String answerJson) {
        return com.example.demo.util.AnswerMapJSONUntil.parseTopicData(answerJson);
    }

    // ============================================
    // 按类型分化的专门查询方法
    // ============================================

    /**
     * 查询学生已提交的视频观看步骤详情
     */
    public com.example.demo.pojo.response.StudentVideoProcedureDetailResponse getStudentCompletedVideoProcedure(
            String studentUsername, String courseId, Long experimentId, Long procedureId) {

        log.info("教师查询学生已提交视频观看步骤详情，学生: {}, 步骤: {}", studentUsername, procedureId);

        // 查询步骤基本信息
        ExperimentalProcedure procedure = experimentalProcedureService.getById(procedureId);
        if (procedure == null) {
            throw new com.example.demo.exception.BusinessException(404, "步骤不存在");
        }

        if (!Integer.valueOf(1).equals(procedure.getType())) {
            throw new com.example.demo.exception.BusinessException(400, "步骤类型不是视频观看");
        }

        // 查询学生提交记录
        LambdaQueryWrapper<StudentExperimentalProcedure> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudentExperimentalProcedure::getExperimentalProcedureId, procedureId);
        wrapper.eq(StudentExperimentalProcedure::getStudentUsername, studentUsername);
        StudentExperimentalProcedure studentProcedure = studentExperimentalProcedureService.getOne(wrapper);

        if (studentProcedure == null) {
            throw new com.example.demo.exception.BusinessException(404, "未找到提交记录");
        }

        // 构建响应
        com.example.demo.pojo.response.StudentVideoProcedureDetailResponse response =
            new com.example.demo.pojo.response.StudentVideoProcedureDetailResponse();
        response.setId(procedure.getId());
        response.setNumber(procedure.getNumber());
        response.setRemark(procedure.getRemark());
        response.setProportion(procedure.getProportion());
        response.setSubmissionTime(studentProcedure.getCreatedTime());
        response.setScore(studentProcedure.getScore());
        response.setTeacherComment(studentProcedure.getTeacherComment());
        response.setIsGraded(studentProcedure.getIsGraded());

        // 填充视频详情
        if (procedure.getVideoId() != null) {
            VideoFile videoFile = videoFileMapper.selectById(procedure.getVideoId());
            if (videoFile != null) {
                com.example.demo.pojo.response.StudentVideoProcedureDetailResponse.VideoDetail detail =
                    new com.example.demo.pojo.response.StudentVideoProcedureDetailResponse.VideoDetail();
                detail.setId(videoFile.getId());
                detail.setTitle(videoFile.getOriginalFileName());
                detail.setSeconds(videoFile.getVideoSeconds());
                detail.setFilePath(videoFile.getFilePath());
                detail.setFileSize(videoFile.getFileSize());
                response.setVideoDetail(detail);
            }
        }

        return response;
    }

    /**
     * 查询学生未提交的视频观看步骤详情
     */
    public com.example.demo.pojo.response.StudentVideoProcedureDetailResponse getStudentUncompletedVideoProcedure(
            String studentUsername, String courseId, Long experimentId, Long procedureId) {

        log.info("教师查询学生未提交视频观看步骤详情，学生: {}, 步骤: {}", studentUsername, procedureId);

        // 查询步骤基本信息
        ExperimentalProcedure procedure = experimentalProcedureService.getById(procedureId);
        if (procedure == null) {
            throw new com.example.demo.exception.BusinessException(404, "步骤不存在");
        }

        if (!Integer.valueOf(1).equals(procedure.getType())) {
            throw new com.example.demo.exception.BusinessException(400, "步骤类型不是视频观看");
        }

        // 构建响应
        com.example.demo.pojo.response.StudentVideoProcedureDetailResponse response =
            new com.example.demo.pojo.response.StudentVideoProcedureDetailResponse();
        response.setId(procedure.getId());
        response.setNumber(procedure.getNumber());
        response.setRemark(procedure.getRemark());
        response.setProportion(procedure.getProportion());

        // 填充视频详情
        if (procedure.getVideoId() != null) {
            VideoFile videoFile = videoFileMapper.selectById(procedure.getVideoId());
            if (videoFile != null) {
                com.example.demo.pojo.response.StudentVideoProcedureDetailResponse.VideoDetail detail =
                    new com.example.demo.pojo.response.StudentVideoProcedureDetailResponse.VideoDetail();
                detail.setId(videoFile.getId());
                detail.setTitle(videoFile.getOriginalFileName());
                detail.setSeconds(videoFile.getVideoSeconds());
                detail.setFilePath(videoFile.getFilePath());
                detail.setFileSize(videoFile.getFileSize());
                response.setVideoDetail(detail);
            }
        }

        return response;
    }

    /**
     * 查询学生已提交的数据收集步骤详情
     */
    public com.example.demo.pojo.response.StudentDataCollectionProcedureDetailResponse getStudentCompletedDataCollectionProcedure(
            String studentUsername, String courseId, Long experimentId, Long procedureId) {

        log.info("教师查询学生已提交数据收集步骤详情，学生: {}, 步骤: {}", studentUsername, procedureId);

        // 查询步骤基本信息
        ExperimentalProcedure procedure = experimentalProcedureService.getById(procedureId);
        if (procedure == null) {
            throw new com.example.demo.exception.BusinessException(404, "步骤不存在");
        }

        if (!Integer.valueOf(2).equals(procedure.getType())) {
            throw new com.example.demo.exception.BusinessException(400, "步骤类型不是数据收集");
        }

        // 查询学生提交记录
        LambdaQueryWrapper<StudentExperimentalProcedure> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudentExperimentalProcedure::getExperimentalProcedureId, procedureId);
        wrapper.eq(StudentExperimentalProcedure::getStudentUsername, studentUsername);
        StudentExperimentalProcedure studentProcedure = studentExperimentalProcedureService.getOne(wrapper);

        if (studentProcedure == null) {
            throw new com.example.demo.exception.BusinessException(404, "未找到提交记录");
        }

        // 构建响应
        com.example.demo.pojo.response.StudentDataCollectionProcedureDetailResponse response =
            new com.example.demo.pojo.response.StudentDataCollectionProcedureDetailResponse();
        response.setId(procedure.getId());
        response.setNumber(procedure.getNumber());
        response.setRemark(procedure.getRemark());
        response.setProportion(procedure.getProportion());
        response.setSubmissionTime(studentProcedure.getCreatedTime());
        response.setScore(studentProcedure.getScore());
        response.setTeacherComment(studentProcedure.getTeacherComment());
        response.setIsGraded(studentProcedure.getIsGraded());

        // 填充数据收集详情
        if (procedure.getDataCollectionId() != null) {
            DataCollection dataCollection = dataCollectionMapper.selectById(procedure.getDataCollectionId());
            if (dataCollection != null) {
                com.example.demo.pojo.response.StudentDataCollectionProcedureDetailResponse.DataCollectionDetail detail =
                    new com.example.demo.pojo.response.StudentDataCollectionProcedureDetailResponse.DataCollectionDetail();
                detail.setId(dataCollection.getId());
                detail.setType(dataCollection.getType() != null ? dataCollection.getType().intValue() : null);
                detail.setRemark(dataCollection.getRemark());
                detail.setNeedPhoto(dataCollection.getNeedPhoto());
                detail.setNeedDoc(dataCollection.getNeedDoc());

                // 查询附件信息
                LambdaQueryWrapper<StudentProcedureAttachment> attachmentWrapper = new LambdaQueryWrapper<>();
                attachmentWrapper.eq(StudentProcedureAttachment::getProcedureId, procedure.getId());
                attachmentWrapper.eq(StudentProcedureAttachment::getStudentUsername, studentUsername);
                List<StudentProcedureAttachment> attachments = studentProcedureAttachmentMapper.selectList(attachmentWrapper);

                List<com.example.demo.pojo.response.StudentDataCollectionProcedureDetailResponse.AttachmentInfo> photos = new ArrayList<>();
                List<com.example.demo.pojo.response.StudentDataCollectionProcedureDetailResponse.AttachmentInfo> documents = new ArrayList<>();

                for (StudentProcedureAttachment attachment : attachments) {
                    com.example.demo.pojo.response.StudentDataCollectionProcedureDetailResponse.AttachmentInfo info =
                        new com.example.demo.pojo.response.StudentDataCollectionProcedureDetailResponse.AttachmentInfo();
                    info.setId(attachment.getId());
                    info.setFileType(attachment.getFileType());
                    info.setFileFormat(attachment.getFileFormat());
                    info.setOriginalFileName(attachment.getOriginalFileName());
                    info.setFileSize(attachment.getFileSize());
                    info.setRemark(attachment.getRemark());
                    info.setCreateTime(attachment.getCreateTime());

                    // 生成文件下载密钥
                    String currentUsername = com.example.demo.util.SecurityUtil.getCurrentUsername().orElse(null);
                    if (currentUsername != null) {
                        String downloadKey = downloadService.generateFileKey(
                            DownloadService.TYPE_ATTACHMENT, attachment.getId(), currentUsername);
                        info.setDownloadKey(downloadKey);
                    }

                    if (attachment.getFileType() == 1) {
                        photos.add(info);
                    } else if (attachment.getFileType() == 2) {
                        documents.add(info);
                    }
                }

                detail.setPhotos(photos);
                detail.setDocuments(documents);

                // 解析答案JSON
                String answer = studentProcedure.getAnswer();
                if (answer != null && !answer.isEmpty()) {
                    // 使用工具类解析 data 字段（支持嵌套对象）
                    Map<String, Object> dataMap = com.example.demo.util.AnswerMapJSONUntil.parseDataAsObject(answer);

                    @SuppressWarnings("unchecked")
                    Map<String, String> fillBlankAnswers = (Map<String, String>) dataMap.get("fillBlankAnswers");
                    @SuppressWarnings("unchecked")
                    Map<String, String> tableCellAnswers = (Map<String, String>) dataMap.get("tableCellAnswers");

                    detail.setFillBlankAnswers(FillBlankAnswer.fromMap(fillBlankAnswers));
                    detail.setTableCellAnswers(TableCellAnswer.fromMap(tableCellAnswers));
                }

                // 正确答案
                if (dataCollection.getCorrectAnswer() != null) {
                    detail.setCorrectAnswer(dataCollection.getCorrectAnswer());
                }

                response.setDataCollectionDetail(detail);
            }
        }

        return response;
    }

    /**
     * 查询学生未提交的数据收集步骤详情
     */
    public com.example.demo.pojo.response.StudentDataCollectionProcedureDetailResponse getStudentUncompletedDataCollectionProcedure(
            String studentUsername, String courseId, Long experimentId, Long procedureId) {

        log.info("教师查询学生未提交数据收集步骤详情，学生: {}, 步骤: {}", studentUsername, procedureId);

        // 查询步骤基本信息
        ExperimentalProcedure procedure = experimentalProcedureService.getById(procedureId);
        if (procedure == null) {
            throw new com.example.demo.exception.BusinessException(404, "步骤不存在");
        }

        if (!Integer.valueOf(2).equals(procedure.getType())) {
            throw new com.example.demo.exception.BusinessException(400, "步骤类型不是数据收集");
        }

        // 构建响应
        com.example.demo.pojo.response.StudentDataCollectionProcedureDetailResponse response =
            new com.example.demo.pojo.response.StudentDataCollectionProcedureDetailResponse();
        response.setId(procedure.getId());
        response.setNumber(procedure.getNumber());
        response.setRemark(procedure.getRemark());
        response.setProportion(procedure.getProportion());

        // 填充数据收集详情（不含学生答案）
        if (procedure.getDataCollectionId() != null) {
            DataCollection dataCollection = dataCollectionMapper.selectById(procedure.getDataCollectionId());
            if (dataCollection != null) {
                com.example.demo.pojo.response.StudentDataCollectionProcedureDetailResponse.DataCollectionDetail detail =
                    new com.example.demo.pojo.response.StudentDataCollectionProcedureDetailResponse.DataCollectionDetail();
                detail.setId(dataCollection.getId());
                detail.setType(dataCollection.getType() != null ? dataCollection.getType().intValue() : null);
                detail.setRemark(dataCollection.getRemark());
                detail.setNeedPhoto(dataCollection.getNeedPhoto());
                detail.setNeedDoc(dataCollection.getNeedDoc());

                // 正确答案
                if (dataCollection.getCorrectAnswer() != null) {
                    detail.setCorrectAnswer(dataCollection.getCorrectAnswer());
                }

                response.setDataCollectionDetail(detail);
            }
        }

        return response;
    }

    /**
     * 查询学生已提交的题库答题步骤详情
     */
    public com.example.demo.pojo.response.StudentTopicProcedureDetailResponse getStudentCompletedTopicProcedure(
            String studentUsername, String courseId, Long experimentId, Long procedureId) {

        log.info("教师查询学生已提交题库答题步骤详情，学生: {}, 步骤: {}", studentUsername, procedureId);

        // 查询步骤基本信息
        ExperimentalProcedure procedure = experimentalProcedureService.getById(procedureId);
        if (procedure == null) {
            throw new com.example.demo.exception.BusinessException(404, "步骤不存在");
        }

        if (!Integer.valueOf(3).equals(procedure.getType())) {
            throw new com.example.demo.exception.BusinessException(400, "步骤类型不是题库答题");
        }

        // 查询学生提交记录
        LambdaQueryWrapper<StudentExperimentalProcedure> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudentExperimentalProcedure::getExperimentalProcedureId, procedureId);
        wrapper.eq(StudentExperimentalProcedure::getStudentUsername, studentUsername);
        StudentExperimentalProcedure studentProcedure = studentExperimentalProcedureService.getOne(wrapper);

        if (studentProcedure == null) {
            throw new com.example.demo.exception.BusinessException(404, "未找到提交记录");
        }

        // 构建响应
        com.example.demo.pojo.response.StudentTopicProcedureDetailResponse response =
            new com.example.demo.pojo.response.StudentTopicProcedureDetailResponse();
        response.setId(procedure.getId());
        response.setNumber(procedure.getNumber());
        response.setRemark(procedure.getRemark());
        response.setProportion(procedure.getProportion());
        response.setSubmissionTime(studentProcedure.getCreatedTime());
        response.setScore(studentProcedure.getScore());
        response.setTeacherComment(studentProcedure.getTeacherComment());
        response.setIsGraded(studentProcedure.getIsGraded());

        // 填充题库详情
        if (procedure.getProcedureTopicId() != null) {
            ProcedureTopic procedureTopic = procedureTopicMapper.selectById(procedure.getProcedureTopicId());
            if (procedureTopic != null) {
                com.example.demo.pojo.response.StudentTopicProcedureDetailResponse.TopicDetail detail =
                    new com.example.demo.pojo.response.StudentTopicProcedureDetailResponse.TopicDetail();
                detail.setId(procedureTopic.getId());
                detail.setIsRandom(procedureTopic.getIsRandom());
                detail.setNumber(procedureTopic.getNumber());
                detail.setTags(procedureTopic.getTags());

                // 查询题目列表
                List<Topic> topics = getTopicsForProcedureTeacher(procedureTopic);
                List<com.example.demo.pojo.response.StudentTopicProcedureDetailResponse.TopicItem> topicItems = new ArrayList<>();

                // 解析学生答案
                Map<Long, String> studentAnswers = parseTopicAnswers(studentProcedure.getAnswer());

                for (Topic topic : topics) {
                    com.example.demo.pojo.response.StudentTopicProcedureDetailResponse.TopicItem item =
                        new com.example.demo.pojo.response.StudentTopicProcedureDetailResponse.TopicItem();
                    item.setId(topic.getId());
                    item.setNumber(topic.getNumber());
                    item.setType(topic.getType());
                    item.setContent(topic.getContent());
                    item.setChoices(topic.getChoices());

                    String studentAnswer = studentAnswers.get(topic.getId());
                    item.setStudentAnswer(studentAnswer);

                    // 教师始终可以查看正确答案和是否正确
                    item.setCorrectAnswer(topic.getCorrectAnswer());

                    if (studentAnswer != null && studentAnswer.equals(topic.getCorrectAnswer())) {
                        item.setIsCorrect(true);
                    } else {
                        item.setIsCorrect(false);
                    }

                    topicItems.add(item);
                }

                detail.setTopics(topicItems);
                response.setTopicDetail(detail);
            }
        }

        return response;
    }

    /**
     * 查询学生未提交的题库答题步骤详情
     */
    public com.example.demo.pojo.response.StudentTopicProcedureDetailResponse getStudentUncompletedTopicProcedure(
            String studentUsername, String courseId, Long experimentId, Long procedureId) {

        log.info("教师查询学生未提交题库答题步骤详情，学生: {}, 步骤: {}", studentUsername, procedureId);

        // 查询步骤基本信息
        ExperimentalProcedure procedure = experimentalProcedureService.getById(procedureId);
        if (procedure == null) {
            throw new com.example.demo.exception.BusinessException(404, "步骤不存在");
        }

        if (!Integer.valueOf(3).equals(procedure.getType())) {
            throw new com.example.demo.exception.BusinessException(400, "步骤类型不是题库答题");
        }

        // 构建响应
        com.example.demo.pojo.response.StudentTopicProcedureDetailResponse response =
            new com.example.demo.pojo.response.StudentTopicProcedureDetailResponse();
        response.setId(procedure.getId());
        response.setNumber(procedure.getNumber());
        response.setRemark(procedure.getRemark());
        response.setProportion(procedure.getProportion());

        // 填充题库详情（不含答案）
        if (procedure.getProcedureTopicId() != null) {
            ProcedureTopic procedureTopic = procedureTopicMapper.selectById(procedure.getProcedureTopicId());
            if (procedureTopic != null) {
                com.example.demo.pojo.response.StudentTopicProcedureDetailResponse.TopicDetail detail =
                    new com.example.demo.pojo.response.StudentTopicProcedureDetailResponse.TopicDetail();
                detail.setId(procedureTopic.getId());
                detail.setIsRandom(procedureTopic.getIsRandom());
                detail.setNumber(procedureTopic.getNumber());
                detail.setTags(procedureTopic.getTags());

                // 查询题目列表（不含答案）
                List<Topic> topics = getTopicsForProcedureTeacher(procedureTopic);
                List<com.example.demo.pojo.response.StudentTopicProcedureDetailResponse.TopicItem> topicItems = new ArrayList<>();

                for (Topic topic : topics) {
                    com.example.demo.pojo.response.StudentTopicProcedureDetailResponse.TopicItem item =
                        new com.example.demo.pojo.response.StudentTopicProcedureDetailResponse.TopicItem();
                    item.setId(topic.getId());
                    item.setNumber(topic.getNumber());
                    item.setType(topic.getType());
                    item.setContent(topic.getContent());
                    item.setChoices(topic.getChoices());
                    topicItems.add(item);
                }

                detail.setTopics(topicItems);
                response.setTopicDetail(detail);
            }
        }

        return response;
    }

    /**
     * 查询学生已提交的限时答题步骤详情
     */
    public com.example.demo.pojo.response.StudentTimedQuizProcedureDetailResponse getStudentCompletedTimedQuizProcedure(
            String studentUsername, String courseId, Long experimentId, Long procedureId) {

        log.info("教师查询学生已提交限时答题步骤详情，学生: {}, 步骤: {}", studentUsername, procedureId);

        // 查询步骤基本信息
        ExperimentalProcedure procedure = experimentalProcedureService.getById(procedureId);
        if (procedure == null) {
            throw new com.example.demo.exception.BusinessException(404, "步骤不存在");
        }

        if (!Integer.valueOf(5).equals(procedure.getType())) {
            throw new com.example.demo.exception.BusinessException(400, "步骤类型不是限时答题");
        }

        // 查询学生提交记录
        LambdaQueryWrapper<StudentExperimentalProcedure> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudentExperimentalProcedure::getExperimentalProcedureId, procedureId);
        wrapper.eq(StudentExperimentalProcedure::getStudentUsername, studentUsername);
        StudentExperimentalProcedure studentProcedure = studentExperimentalProcedureService.getOne(wrapper);

        if (studentProcedure == null) {
            throw new com.example.demo.exception.BusinessException(404, "未找到提交记录");
        }

        // 构建响应
        com.example.demo.pojo.response.StudentTimedQuizProcedureDetailResponse response =
            new com.example.demo.pojo.response.StudentTimedQuizProcedureDetailResponse();
        response.setId(procedure.getId());
        response.setNumber(procedure.getNumber());
        response.setRemark(procedure.getRemark());
        response.setProportion(procedure.getProportion());
        response.setSubmissionTime(studentProcedure.getCreatedTime());
        response.setScore(studentProcedure.getScore());
        response.setTeacherComment(studentProcedure.getTeacherComment());
        response.setIsGraded(studentProcedure.getIsGraded());

        // 填充限时答题详情
        if (procedure.getTimedQuizId() != null) {
            TimedQuizProcedure timedQuiz = timedQuizProcedureMapper.selectById(procedure.getTimedQuizId());
            if (timedQuiz != null) {
                com.example.demo.pojo.response.StudentTimedQuizProcedureDetailResponse.TimedQuizDetail detail =
                    new com.example.demo.pojo.response.StudentTimedQuizProcedureDetailResponse.TimedQuizDetail();
                detail.setId(timedQuiz.getId());
                detail.setIsRandom(timedQuiz.getIsRandom());
                detail.setNumber(timedQuiz.getTopicNumber());
                detail.setQuizTimeLimit(timedQuiz.getQuizTimeLimit());
                detail.setIsLocked(studentProcedure.getIsLocked());

                // 查询题目列表
                List<Topic> topics = getTopicsForTimedQuizProcedure(procedure, timedQuiz);
                List<com.example.demo.pojo.response.StudentTimedQuizProcedureDetailResponse.TopicItem> topicItems = new ArrayList<>();

                // 解析学生答案
                Map<Long, String> studentAnswers = parseTopicAnswers(studentProcedure.getAnswer());

                for (Topic topic : topics) {
                    com.example.demo.pojo.response.StudentTimedQuizProcedureDetailResponse.TopicItem item =
                        new com.example.demo.pojo.response.StudentTimedQuizProcedureDetailResponse.TopicItem();
                    item.setId(topic.getId());
                    item.setNumber(topic.getNumber());
                    item.setType(topic.getType());
                    item.setContent(topic.getContent());
                    item.setChoices(topic.getChoices());

                    String studentAnswer = studentAnswers.get(topic.getId());
                    item.setStudentAnswer(studentAnswer);

                    // 教师始终可以查看正确答案和是否正确
                    item.setCorrectAnswer(topic.getCorrectAnswer());

                    if (studentAnswer != null && studentAnswer.equals(topic.getCorrectAnswer())) {
                        item.setIsCorrect(true);
                    } else {
                        item.setIsCorrect(false);
                    }

                    topicItems.add(item);
                }

                detail.setTopics(topicItems);
                response.setTimedQuizDetail(detail);
            }
        }

        return response;
    }

    /**
     * 查询学生未提交的限时答题步骤详情
     */
    public com.example.demo.pojo.response.StudentTimedQuizProcedureDetailResponse getStudentUncompletedTimedQuizProcedure(
            String studentUsername, String courseId, Long experimentId, Long procedureId) {

        log.info("教师查询学生未提交限时答题步骤详情，学生: {}, 步骤: {}", studentUsername, procedureId);

        // 查询步骤基本信息
        ExperimentalProcedure procedure = experimentalProcedureService.getById(procedureId);
        if (procedure == null) {
            throw new com.example.demo.exception.BusinessException(404, "步骤不存在");
        }

        if (!Integer.valueOf(5).equals(procedure.getType())) {
            throw new com.example.demo.exception.BusinessException(400, "步骤类型不是限时答题");
        }

        // 构建响应
        com.example.demo.pojo.response.StudentTimedQuizProcedureDetailResponse response =
            new com.example.demo.pojo.response.StudentTimedQuizProcedureDetailResponse();
        response.setId(procedure.getId());
        response.setNumber(procedure.getNumber());
        response.setRemark(procedure.getRemark());
        response.setProportion(procedure.getProportion());

        // 填充限时答题详情（不含答案）
        if (procedure.getTimedQuizId() != null) {
            TimedQuizProcedure timedQuiz = timedQuizProcedureMapper.selectById(procedure.getTimedQuizId());
            if (timedQuiz != null) {
                com.example.demo.pojo.response.StudentTimedQuizProcedureDetailResponse.TimedQuizDetail detail =
                    new com.example.demo.pojo.response.StudentTimedQuizProcedureDetailResponse.TimedQuizDetail();
                detail.setId(timedQuiz.getId());
                detail.setIsRandom(timedQuiz.getIsRandom());
                detail.setNumber(timedQuiz.getTopicNumber());
                detail.setQuizTimeLimit(timedQuiz.getQuizTimeLimit());

                // 查询题目列表（不含答案）
                List<Topic> topics = getTopicsForTimedQuizProcedure(procedure, timedQuiz);
                List<com.example.demo.pojo.response.StudentTimedQuizProcedureDetailResponse.TopicItem> topicItems = new ArrayList<>();

                for (Topic topic : topics) {
                    com.example.demo.pojo.response.StudentTimedQuizProcedureDetailResponse.TopicItem item =
                        new com.example.demo.pojo.response.StudentTimedQuizProcedureDetailResponse.TopicItem();
                    item.setId(topic.getId());
                    item.setNumber(topic.getNumber());
                    item.setType(topic.getType());
                    item.setContent(topic.getContent());
                    item.setChoices(topic.getChoices());
                    topicItems.add(item);
                }

                detail.setTopics(topicItems);
                response.setTimedQuizDetail(detail);
            }
        }

        return response;
    }

    /**
     * 获取限时答题的题目列表
     */
    private List<Topic> getTopicsForTimedQuizProcedure(ExperimentalProcedure procedure, TimedQuizProcedure timedQuiz) {
        if (Boolean.TRUE.equals(timedQuiz.getIsRandom())) {
            // 随机模式：使用现有的随机题目方法
            if (timedQuiz.getTopicTags() != null && !timedQuiz.getTopicTags().isEmpty()) {
                String[] tagIds = timedQuiz.getTopicTags().split(",");
                List<Long> tagIdList = Arrays.stream(tagIds)
                    .filter(s -> s != null && !s.isEmpty())
                    .map(Long::parseLong)
                    .collect(Collectors.toList());

                if (!tagIdList.isEmpty()) {
                    LambdaQueryWrapper<TopicTagMap> tagWrapper = new LambdaQueryWrapper<>();
                    tagWrapper.in(TopicTagMap::getTagId, tagIdList);
                    List<TopicTagMap> topicTagMaps = topicTagMapMapper.selectList(tagWrapper);

                    if (!topicTagMaps.isEmpty()) {
                        List<Long> topicIds = topicTagMaps.stream()
                            .map(TopicTagMap::getTopicId)
                            .distinct()
                            .collect(Collectors.toList());

                        if (!topicIds.isEmpty()) {
                            LambdaQueryWrapper<Topic> topicWrapper = new LambdaQueryWrapper<>();
                            topicWrapper.in(Topic::getId, topicIds);

                            // 添加题目类型过滤
                            if (timedQuiz.getTopicTypes() != null && !timedQuiz.getTopicTypes().isEmpty()) {
                                String[] typeArray = timedQuiz.getTopicTypes().split(",");
                                List<Integer> types = Arrays.stream(typeArray)
                                    .filter(s -> s != null && !s.isEmpty())
                                    .map(Integer::parseInt)
                                    .collect(Collectors.toList());
                                if (!types.isEmpty()) {
                                    topicWrapper.in(Topic::getType, types);
                                }
                            }

                            topicWrapper.orderByAsc(Topic::getNumber);
                            return topicMapper.selectList(topicWrapper);
                        }
                    }
                }
            }
            return new ArrayList<>();
        } else {
            // 老师选定模式：从题库详情映射表查询
            QueryWrapper<ProcedureTopicMap> mapWrapper = new QueryWrapper<>();
            mapWrapper.eq("experimental_procedure_id", procedure.getId());
            mapWrapper.orderByAsc("id");
            List<ProcedureTopicMap> topicMaps = procedureTopicMapMapper.selectList(mapWrapper);

            if (!topicMaps.isEmpty()) {
                List<Long> topicIds = topicMaps.stream()
                    .map(ProcedureTopicMap::getTopicId)
                    .collect(Collectors.toList());

                QueryWrapper<Topic> topicWrapper = new QueryWrapper<>();
                topicWrapper.in("id", topicIds);
                topicWrapper.orderByAsc("number");
                return topicMapper.selectList(topicWrapper);
            }
            return new ArrayList<>();
        }
    }

    // ==================== 使用 classExperimentId 的新方法 ====================

    /**
     * 查询学生实验步骤完成情况（使用 classExperimentId）
     *
     * @param studentUsername   学生用户名
     * @param classExperimentId 班级实验ID
     * @return 步骤完成情况
     */
    public StudentProcedureCompletionResponse getStudentProcedureCompletionByClassExperimentId(
            String studentUsername, Long classExperimentId) {

        log.info("查询学生实验步骤完成情况，学生: {}, 班级实验ID: {}",
                studentUsername, classExperimentId);

        // 1. 查询班级实验信息
        ClassExperiment classExperiment = classExperimentMapper.selectById(classExperimentId);
        if (classExperiment == null) {
            throw new com.example.demo.exception.BusinessException(404, "班级实验不存在");
        }

        Long experimentId = Long.parseLong(classExperiment.getExperimentId());

        // 2. 获取关联的班级列表（使用第一个班级）
        List<String> classCodes = classExperimentClassRelationService.getClassCodesByExperimentId(classExperimentId);
        if (classCodes == null || classCodes.isEmpty()) {
            throw new com.example.demo.exception.BusinessException(404, "班级实验未关联任何班级");
        }
        String classCode = classCodes.get(0);

        // 3. 调用原有方法
        return getStudentProcedureCompletion(studentUsername, classCode, experimentId);
    }

    /**
     * 查询学生在指定步骤的完成详情（使用 classExperimentId）
     *
     * @param studentUsername   学生用户名
     * @param procedureId       步骤ID
     * @param classExperimentId 班级实验ID
     * @return 步骤完成详情
     */
    public StudentProcedureDetailCompletionResponse getStudentProcedureDetailCompletionByClassExperimentId(
            String studentUsername, Long procedureId, Long classExperimentId) {

        log.info("查询学生步骤完成详情，学生: {}, 步骤: {}, 班级实验ID: {}",
                studentUsername, procedureId, classExperimentId);

        // 1. 查询班级实验信息
        ClassExperiment classExperiment = classExperimentMapper.selectById(classExperimentId);
        if (classExperiment == null) {
            throw new com.example.demo.exception.BusinessException(404, "班级实验不存在");
        }

        // 2. 获取关联的班级列表（使用第一个班级）
        List<String> classCodes = classExperimentClassRelationService.getClassCodesByExperimentId(classExperimentId);
        if (classCodes == null || classCodes.isEmpty()) {
            throw new com.example.demo.exception.BusinessException(404, "班级实验未关联任何班级");
        }
        String classCode = classCodes.get(0);

        // 3. 调用原有方法
        return getStudentProcedureDetailCompletion(studentUsername, classCode, procedureId);
    }

    /**
     * 查询班级实验完成统计（使用 classExperimentId）
     *
     * @param classExperimentId 班级实验ID
     * @return 班级实验完成统计
     */
    public ClassExperimentStatisticsResponse getClassExperimentStatisticsByClassExperimentId(
            Long classExperimentId) {

        log.info("查询班级实验完成统计，班级实验ID: {}", classExperimentId);

        // 1. 查询班级实验信息
        ClassExperiment classExperiment = classExperimentMapper.selectById(classExperimentId);
        if (classExperiment == null) {
            throw new com.example.demo.exception.BusinessException(404, "班级实验不存在");
        }

        Long experimentId = Long.parseLong(classExperiment.getExperimentId());

        // 2. 获取关联的班级列表（使用第一个班级）
        List<String> classCodes = classExperimentClassRelationService.getClassCodesByExperimentId(classExperimentId);
        if (classCodes == null || classCodes.isEmpty()) {
            throw new com.example.demo.exception.BusinessException(404, "班级实验未关联任何班级");
        }
        String classCode = classCodes.get(0);

        // 3. 调用原有方法
        return getClassExperimentStatistics(classCode, experimentId);
    }
}
