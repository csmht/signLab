package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.mapper.*;
import com.example.demo.pojo.entity.*;
import com.example.demo.pojo.response.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
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
    private final ClassExperimentClassRelationService classExperimentClassRelationService;
    private final DownloadService downloadService;

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
                .eq("experimental_procedure_id", procedureId);
        List<StudentExperimentalProcedure> studentProcedures = studentExperimentalProcedureService.list(queryWrapper);

        // 从多条记录中找到匹配班级的记录
        StudentExperimentalProcedure studentProcedure = null;
        if (studentProcedures != null && !studentProcedures.isEmpty()) {
            // 通过关联表查询班级实验ID列表
            List<Long> experimentIds = classExperimentClassRelationService.getExperimentIdsByClassCode(classCode);
            for (StudentExperimentalProcedure sp : studentProcedures) {
                if (experimentIds.contains(sp.getClassExperimentId())) {
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

        QueryWrapper<StudentProcedureAttachment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("procedure_id", procedure.getId())
                .eq("student_username", response.getStudentUsername())
                .eq("class_experiment_id", studentProcedure.getClassExperimentId())
                .orderByDesc("create_time");

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

        // 2. 查询班级实验关系
        List<Long> experimentIds = classExperimentClassRelationService.getExperimentIdsByClassCode(classCode);

        com.example.demo.pojo.entity.ClassExperiment classExperiment = null;
        for (Long id : experimentIds) {
            com.example.demo.pojo.entity.ClassExperiment ce = classExperimentMapper.selectById(id);
            if (ce != null && ce.getExperimentId().equals(experimentId.toString())) {
                classExperiment = ce;
                break;
            }
        }

        // 3. 查询实验的所有步骤
        List<ExperimentalProcedure> procedures = experimentalProcedureService.getByExperimentId(experimentId);

        // 4. 查询班级中所有学生提交记录
        // 使用已查询的班级实验ID列表
        QueryWrapper<StudentExperimentalProcedure> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("class_experiment_id", experimentIds);
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
        response.setUserName(classExperiment != null ? classExperiment.getUserName() : null);
        response.setTotalStudents(totalStudents);
        response.setSubmittedCount(submittedStudents);
        response.setCompletionRate(overallCompletionRate);
        response.setAverageScore(overallAverageScore);
        response.setProcedureStatistics(procedureStatisticsList);
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
        QueryWrapper<StudentExperimentalProcedure> wrapper = new QueryWrapper<>();
        wrapper.eq("experimental_procedure_id", procedureId);
        wrapper.eq("student_username", studentUsername);
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
        QueryWrapper<StudentExperimentalProcedure> wrapper = new QueryWrapper<>();
        wrapper.eq("experimental_procedure_id", procedureId);
        wrapper.eq("student_username", studentUsername);
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
                QueryWrapper<StudentProcedureAttachment> attachmentWrapper = new QueryWrapper<>();
                attachmentWrapper.eq("procedure_id", procedure.getId());
                attachmentWrapper.eq("student_username", username);
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
                    new QueryWrapper<StudentExperimentalProcedure>()
                        .eq("experimental_procedure_id", procedure.getId())
                        .eq("student_username", username)
                ).getAnswer();

                if (answer != null && !answer.isEmpty()) {
                    try {
                        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                        Map<String, Object> answerMap = mapper.readValue(answer,
                            new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});

                        @SuppressWarnings("unchecked")
                        Map<String, String> fillBlankAnswers = (Map<String, String>) answerMap.get("fillBlankAnswers");
                        @SuppressWarnings("unchecked")
                        Map<String, String> tableCellAnswers = (Map<String, String>) answerMap.get("tableCellAnswers");

                        detail.setFillBlankAnswers(fillBlankAnswers);
                        detail.setTableCellAnswers(tableCellAnswers);
                    } catch (Exception e) {
                        log.warn("解析数据收集答案失败: {}", e.getMessage());
                    }
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
                    QueryWrapper<TopicTagMap> tagWrapper = new QueryWrapper<>();
                    tagWrapper.in("tag_id", tagIdList);
                    List<TopicTagMap> topicTagMaps = topicTagMapMapper.selectList(tagWrapper);

                    if (!topicTagMaps.isEmpty()) {
                        List<Long> topicIds = topicTagMaps.stream()
                            .map(TopicTagMap::getTopicId)
                            .distinct()
                            .collect(Collectors.toList());

                        if (!topicIds.isEmpty()) {
                            QueryWrapper<Topic> topicWrapper = new QueryWrapper<>();
                            topicWrapper.in("id", topicIds);

                            // 添加题目类型过滤
                            if (procedureTopic.getTopicTypes() != null && !procedureTopic.getTopicTypes().isEmpty()) {
                                String[] typeArray = procedureTopic.getTopicTypes().split(",");
                                List<Integer> types = Arrays.stream(typeArray)
                                    .filter(s -> s != null && !s.isEmpty())
                                    .map(Integer::parseInt)
                                    .collect(Collectors.toList());
                                if (!types.isEmpty()) {
                                    topicWrapper.in("type", types);
                                }
                            }

                            topicWrapper.orderByAsc("number");
                            return topicMapper.selectList(topicWrapper);
                        }
                    }
                }
            }
            return new ArrayList<>();
        } else {
            // 固定题目：从题库详情映射表查询
            QueryWrapper<ProcedureTopicMap> mapWrapper = new QueryWrapper<>();
            mapWrapper.eq("procedure_topic_id", procedureTopic.getId());
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

    /**
     * 解析题库答案JSON
     */
    private Map<Long, String> parseTopicAnswers(String answerJson) {
        if (answerJson == null || answerJson.isEmpty()) {
            return new HashMap<>();
        }

        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readValue(answerJson,
                new com.fasterxml.jackson.core.type.TypeReference<Map<Long, String>>() {});
        } catch (Exception e) {
            log.warn("解析题库答案失败: {}", e.getMessage());
            return new HashMap<>();
        }
    }
}
