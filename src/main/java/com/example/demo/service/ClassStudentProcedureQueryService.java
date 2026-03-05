package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.*;
import com.example.demo.pojo.dto.mapvo.FillBlankAnswer;
import com.example.demo.pojo.dto.mapvo.TableCellAnswer;
import com.example.demo.pojo.dto.mapvo.TopicChoice;
import com.example.demo.pojo.entity.*;
import com.example.demo.pojo.request.ClassProcedureDetailRequest;
import com.example.demo.pojo.response.ClassStudentProcedureDetailResponse;
import com.example.demo.pojo.response.StudentProcedureDetailWithAnswerResponse;
import com.example.demo.util.AnswerMapJSONUntil;
import com.example.demo.util.ProcedureTimeCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 班级学生步骤详情查询服务
 * 提供查询指定班级中所有学生步骤详情的功能
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ClassStudentProcedureQueryService {

    private final StudentClassRelationService studentClassRelationService;
    private final UserMapper userMapper;
    private final ExperimentalProcedureMapper experimentalProcedureMapper;
    private final StudentExperimentalProcedureMapper studentExperimentalProcedureMapper;
    private final StudentProcedureAttachmentMapper studentProcedureAttachmentMapper;
    private final VideoFileMapper videoFileMapper;
    private final DataCollectionMapper dataCollectionMapper;
    private final ProcedureTopicMapper procedureTopicMapper;
    private final TopicMapper topicMapper;
    private final TopicTagMapMapper topicTagMapMapper;
    private final ProcedureTopicMapMapper procedureTopicMapMapper;
    private final ClassExperimentMapper classExperimentMapper;
    private final TimedQuizProcedureMapper timedQuizProcedureMapper;
    private final DownloadService downloadService;

    // 步骤类型常量
    private static final int TYPE_VIDEO = 1;
    private static final int TYPE_DATA_COLLECTION = 2;
    private static final int TYPE_TOPIC = 3;
    private static final int TYPE_TIMED_QUIZ = 5;

    /**
     * 查询班级学生已提交的视频观看步骤详情
     */
    public ClassStudentProcedureDetailResponse<StudentProcedureDetailWithAnswerResponse.VideoDetail> getCompletedVideoDetails(
            ClassProcedureDetailRequest request) {
        return getCompletedProcedureDetails(request, TYPE_VIDEO);
    }

    /**
     * 查询班级学生未提交的视频观看步骤详情
     */
    public ClassStudentProcedureDetailResponse<StudentProcedureDetailWithAnswerResponse.VideoDetail> getUncompletedVideoDetails(
            ClassProcedureDetailRequest request) {
        return getUncompletedProcedureDetails(request, TYPE_VIDEO);
    }

    /**
     * 查询班级学生已提交的数据收集步骤详情
     */
    public ClassStudentProcedureDetailResponse<StudentProcedureDetailWithAnswerResponse.DataCollectionDetail> getCompletedDataCollectionDetails(
            ClassProcedureDetailRequest request) {
        return getCompletedProcedureDetails(request, TYPE_DATA_COLLECTION);
    }

    /**
     * 查询班级学生未提交的数据收集步骤详情
     */
    public ClassStudentProcedureDetailResponse<StudentProcedureDetailWithAnswerResponse.DataCollectionDetail> getUncompletedDataCollectionDetails(
            ClassProcedureDetailRequest request) {
        return getUncompletedProcedureDetails(request, TYPE_DATA_COLLECTION);
    }

    /**
     * 查询班级学生已提交的题库答题步骤详情
     */
    public ClassStudentProcedureDetailResponse<StudentProcedureDetailWithAnswerResponse.TopicDetail> getCompletedTopicDetails(
            ClassProcedureDetailRequest request) {
        return getCompletedProcedureDetails(request, TYPE_TOPIC);
    }

    /**
     * 查询班级学生未提交的题库答题步骤详情
     */
    public ClassStudentProcedureDetailResponse<StudentProcedureDetailWithAnswerResponse.TopicDetail> getUncompletedTopicDetails(
            ClassProcedureDetailRequest request) {
        return getUncompletedProcedureDetails(request, TYPE_TOPIC);
    }

    /**
     * 查询班级学生已提交的限时答题步骤详情
     */
    public ClassStudentProcedureDetailResponse<StudentProcedureDetailWithAnswerResponse.TimedQuizDetail> getCompletedTimedQuizDetails(
            ClassProcedureDetailRequest request) {
        return getCompletedProcedureDetails(request, TYPE_TIMED_QUIZ);
    }

    /**
     * 查询班级学生未提交的限时答题步骤详情
     */
    public ClassStudentProcedureDetailResponse<StudentProcedureDetailWithAnswerResponse.TimedQuizDetail> getUncompletedTimedQuizDetails(
            ClassProcedureDetailRequest request) {
        return getUncompletedProcedureDetails(request, TYPE_TIMED_QUIZ);
    }

    /**
     * 通用已提交详情查询方法
     */
    @SuppressWarnings("unchecked")
    private <T> ClassStudentProcedureDetailResponse<T> getCompletedProcedureDetails(
            ClassProcedureDetailRequest request, int procedureType) {

        // 1. 查询步骤基本信息
        ExperimentalProcedure procedure = experimentalProcedureMapper.selectById(request.getProcedureId());
        if (procedure == null) {
            throw new BusinessException(404, "步骤不存在");
        }

        if (!procedure.getExperimentId().equals(request.getExperimentId())) {
            throw new BusinessException(400, "步骤与实验不匹配");
        }

        // 2. 验证步骤类型
        if (procedure.getType() != procedureType) {
            throw new BusinessException(400, "步骤类型不匹配");
        }

        // 3. 获取班级学生列表
        List<String> studentUsernames = getStudentUsernames(request.getClassCode(), request.getStudentUsername());
        if (studentUsernames.isEmpty()) {
            return buildEmptyResponse(procedure);
        }

        // 4. 批量查询已提交记录
        LambdaQueryWrapper<StudentExperimentalProcedure> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudentExperimentalProcedure::getExperimentalProcedureId, request.getProcedureId())
               .in(StudentExperimentalProcedure::getStudentUsername, studentUsernames);
        List<StudentExperimentalProcedure> submissions = studentExperimentalProcedureMapper.selectList(wrapper);

        if (submissions.isEmpty()) {
            return buildEmptyResponse(procedure);
        }

        // 5. 批量获取学生姓名
        Map<String, String> studentNameMap = batchGetStudentNames(studentUsernames);

        // 6. 计算是否已过答题时间
        boolean isAfterEndTime = calculateIsAfterEndTime(request.getCourseId(), request.getExperimentId(), procedure);

        // 7. 构建响应
        ClassStudentProcedureDetailResponse<T> response = new ClassStudentProcedureDetailResponse<>();
        response.setProcedureId(procedure.getId());
        response.setProcedureNumber(procedure.getNumber());
        response.setProcedureType(procedure.getType());
        response.setProcedureRemark(procedure.getRemark());
        response.setProportion(procedure.getProportion());
        response.setIsAfterEndTime(isAfterEndTime);

        List<ClassStudentProcedureDetailResponse.StudentProcedureItem<T>> items = new ArrayList<>();
        for (StudentExperimentalProcedure submission : submissions) {
            ClassStudentProcedureDetailResponse.StudentProcedureItem<T> item =
                new ClassStudentProcedureDetailResponse.StudentProcedureItem<>();
            item.setStudentUsername(submission.getStudentUsername());
            item.setStudentName(studentNameMap.get(submission.getStudentUsername()));
            item.setSubmissionTime(submission.getCreatedTime());
            item.setScore(submission.getScore());
            item.setTeacherComment(submission.getTeacherComment());
            item.setIsGraded(submission.getIsGraded());

            // 根据类型填充详情
            T detail = (T) fillCompletedDetail(procedure, submission, submission.getStudentUsername(), isAfterEndTime, procedureType);
            item.setDetail(detail);

            items.add(item);
        }
        response.setStudents(items);

        return response;
    }

    /**
     * 通用未提交详情查询方法
     */
    @SuppressWarnings("unchecked")
    private <T> ClassStudentProcedureDetailResponse<T> getUncompletedProcedureDetails(
            ClassProcedureDetailRequest request, int procedureType) {

        // 1. 查询步骤基本信息
        ExperimentalProcedure procedure = experimentalProcedureMapper.selectById(request.getProcedureId());
        if (procedure == null) {
            throw new BusinessException(404, "步骤不存在");
        }

        if (!procedure.getExperimentId().equals(request.getExperimentId())) {
            throw new BusinessException(400, "步骤与实验不匹配");
        }

        // 2. 验证步骤类型
        if (procedure.getType() != procedureType) {
            throw new BusinessException(400, "步骤类型不匹配");
        }

        // 3. 获取班级学生列表
        List<String> studentUsernames = getStudentUsernames(request.getClassCode(), request.getStudentUsername());
        if (studentUsernames.isEmpty()) {
            return buildEmptyResponse(procedure);
        }

        // 4. 批量查询已提交记录
        LambdaQueryWrapper<StudentExperimentalProcedure> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudentExperimentalProcedure::getExperimentalProcedureId, request.getProcedureId())
               .in(StudentExperimentalProcedure::getStudentUsername, studentUsernames);
        List<StudentExperimentalProcedure> submissions = studentExperimentalProcedureMapper.selectList(wrapper);

        // 5. 获取未提交的学生列表
        Set<String> submittedUsernames = submissions.stream()
            .map(StudentExperimentalProcedure::getStudentUsername)
            .collect(Collectors.toSet());

        List<String> unsubmittedUsernames = studentUsernames.stream()
            .filter(username -> !submittedUsernames.contains(username))
            .collect(Collectors.toList());

        if (unsubmittedUsernames.isEmpty()) {
            return buildEmptyResponse(procedure);
        }

        // 6. 批量获取学生姓名
        Map<String, String> studentNameMap = batchGetStudentNames(unsubmittedUsernames);

        // 7. 构建响应
        ClassStudentProcedureDetailResponse<T> response = new ClassStudentProcedureDetailResponse<>();
        response.setProcedureId(procedure.getId());
        response.setProcedureNumber(procedure.getNumber());
        response.setProcedureType(procedure.getType());
        response.setProcedureRemark(procedure.getRemark());
        response.setProportion(procedure.getProportion());
        response.setIsAfterEndTime(false);

        List<ClassStudentProcedureDetailResponse.StudentProcedureItem<T>> items = new ArrayList<>();
        for (String username : unsubmittedUsernames) {
            ClassStudentProcedureDetailResponse.StudentProcedureItem<T> item =
                new ClassStudentProcedureDetailResponse.StudentProcedureItem<>();
            item.setStudentUsername(username);
            item.setStudentName(studentNameMap.get(username));
            item.setSubmissionTime(null);
            item.setScore(null);
            item.setTeacherComment(null);

            // 根据类型填充详情（未提交时每个学生的详情相同）
            T detail = (T) fillUncompletedDetail(procedure, username, procedureType);
            item.setDetail(detail);

            items.add(item);
        }
        response.setStudents(items);

        return response;
    }

    /**
     * 构建空响应
     */
    private <T> ClassStudentProcedureDetailResponse<T> buildEmptyResponse(ExperimentalProcedure procedure) {
        ClassStudentProcedureDetailResponse<T> response = new ClassStudentProcedureDetailResponse<>();
        response.setProcedureId(procedure.getId());
        response.setProcedureNumber(procedure.getNumber());
        response.setProcedureType(procedure.getType());
        response.setProcedureRemark(procedure.getRemark());
        response.setProportion(procedure.getProportion());
        response.setIsAfterEndTime(false);
        response.setStudents(new ArrayList<>());
        return response;
    }

    /**
     * 获取班级学生用户名列表
     */
    private List<String> getStudentUsernames(String classCode, String filterUsername) {
        List<StudentClassRelation> relations = studentClassRelationService.getByClassCode(classCode);

        if (filterUsername != null && !filterUsername.trim().isEmpty()) {
            return relations.stream()
                .map(StudentClassRelation::getStudentUsername)
                .filter(username -> username.contains(filterUsername.trim()))
                .collect(Collectors.toList());
        }

        return relations.stream()
            .map(StudentClassRelation::getStudentUsername)
            .collect(Collectors.toList());
    }

    /**
     * 批量获取学生姓名
     */
    private Map<String, String> batchGetStudentNames(List<String> usernames) {
        if (usernames == null || usernames.isEmpty()) {
            return Collections.emptyMap();
        }
        List<User> users = userMapper.selectList(
            new LambdaQueryWrapper<User>().in(User::getUsername, usernames)
        );
        return users.stream()
            .collect(Collectors.toMap(User::getUsername, User::getName, (a, b) -> a));
    }

    /**
     * 计算是否已过答题时间
     */
    private boolean calculateIsAfterEndTime(String courseId, Long experimentId, ExperimentalProcedure procedure) {
        try {
            LambdaQueryWrapper<ClassExperiment> classExperimentWrapper = new LambdaQueryWrapper<>();
            classExperimentWrapper.eq(ClassExperiment::getCourseId, courseId);
            classExperimentWrapper.eq(ClassExperiment::getExperimentId, experimentId);
            ClassExperiment classExperiment = classExperimentMapper.selectOne(classExperimentWrapper);

            if (classExperiment != null && procedure.getOffsetMinutes() != null) {
                LocalDateTime endTime = ProcedureTimeCalculator.calculateEndTime(
                        ProcedureTimeCalculator.calculateStartTime(
                                classExperiment.getStartTime(),
                                procedure.getOffsetMinutes()
                        ),
                        procedure.getDurationMinutes()
                );

                if (endTime != null) {
                    return LocalDateTime.now().isAfter(endTime);
                }
            }
        } catch (Exception e) {
            log.error("计算步骤时间失败", e);
        }
        return false;
    }

    /**
     * 填充已提交详情
     */
    private Object fillCompletedDetail(ExperimentalProcedure procedure,
                                        StudentExperimentalProcedure submission,
                                        String username,
                                        boolean isAfterEndTime,
                                        int procedureType) {
        switch (procedureType) {
            case TYPE_VIDEO:
                return fillVideoDetail(procedure);
            case TYPE_DATA_COLLECTION:
                return fillDataCollectionDetailForCompleted(procedure, username, isAfterEndTime);
            case TYPE_TOPIC:
                return fillTopicDetailForCompleted(procedure, submission, isAfterEndTime);
            case TYPE_TIMED_QUIZ:
                return fillTimedQuizDetailForCompleted(procedure, submission, isAfterEndTime);
            default:
                return null;
        }
    }

    /**
     * 填充未提交详情
     */
    private Object fillUncompletedDetail(ExperimentalProcedure procedure, String username, int procedureType) {
        switch (procedureType) {
            case TYPE_VIDEO:
                return fillVideoDetail(procedure);
            case TYPE_DATA_COLLECTION:
                return fillDataCollectionDetailForUncompleted(procedure);
            case TYPE_TOPIC:
                return fillTopicDetailForUncompleted(procedure);
            case TYPE_TIMED_QUIZ:
                return fillTimedQuizDetailForUncompleted(procedure, username);
            default:
                return null;
        }
    }

    /**
     * 填充视频详情
     */
    private StudentProcedureDetailWithAnswerResponse.VideoDetail fillVideoDetail(ExperimentalProcedure procedure) {
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
                return detail;
            }
        }
        return null;
    }

    /**
     * 填充数据收集详情（已提交）
     */
    private StudentProcedureDetailWithAnswerResponse.DataCollectionDetail fillDataCollectionDetailForCompleted(
            ExperimentalProcedure procedure, String username, boolean isAfterEndTime) {

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
                detail.setTolerance(dataCollection.getTolerance());

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
                    String downloadKey = downloadService.generateFileKey(
                        DownloadService.TYPE_ATTACHMENT, attachment.getId(), username);
                    info.setDownloadKey(downloadKey);

                    if (attachment.getFileType() == 1) {
                        photos.add(info);
                    } else if (attachment.getFileType() == 2) {
                        documents.add(info);
                    }
                }

                detail.setPhotos(photos);
                detail.setDocuments(documents);

                // 解析答案JSON
                LambdaQueryWrapper<StudentExperimentalProcedure> spWrapper = new LambdaQueryWrapper<>();
                spWrapper.eq(StudentExperimentalProcedure::getExperimentalProcedureId, procedure.getId());
                spWrapper.eq(StudentExperimentalProcedure::getStudentUsername, username);
                StudentExperimentalProcedure studentProcedure = studentExperimentalProcedureMapper.selectOne(spWrapper);

                if (studentProcedure != null) {
                    String answer = studentProcedure.getAnswer();
                    if (answer != null && !answer.isEmpty()) {
                        Map<String, Object> dataMap = AnswerMapJSONUntil.parseDataAsObject(answer);

                        @SuppressWarnings("unchecked")
                        Map<String, String> fillBlankAnswers = (Map<String, String>) dataMap.get("fillBlankAnswers");

                        @SuppressWarnings("unchecked")
                        Map<String, String> tableCellAnswers = (Map<String, String>) dataMap.get("tableCellAnswers");

                        detail.setFillBlankAnswers(FillBlankAnswer.fromMap(fillBlankAnswers));
                        detail.setTableCellAnswers(TableCellAnswer.fromMap(tableCellAnswers));
                    }

                    // 如果已过答题时间，返回正确答案
                    if (isAfterEndTime && dataCollection.getCorrectAnswer() != null) {
                        detail.setCorrectAnswer(dataCollection.getCorrectAnswer());
                    }
                }

                return detail;
            }
        }
        return null;
    }

    /**
     * 填充数据收集详情（未提交）
     */
    private StudentProcedureDetailWithAnswerResponse.DataCollectionDetail fillDataCollectionDetailForUncompleted(
            ExperimentalProcedure procedure) {

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
                return detail;
            }
        }
        return null;
    }

    /**
     * 填充题库详情（已提交）
     */
    private StudentProcedureDetailWithAnswerResponse.TopicDetail fillTopicDetailForCompleted(
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

                // 解析学生答案
                Map<Long, String> studentAnswers = parseTopicAnswers(studentProcedure.getAnswer());
                List<Long> topicIds = new ArrayList<>(studentAnswers.keySet());

                List<Topic> topics = new ArrayList<>();
                if (!topicIds.isEmpty()) {
                    topics = topicMapper.selectList(new LambdaQueryWrapper<Topic>().in(Topic::getId, topicIds));
                }

                List<StudentProcedureDetailWithAnswerResponse.TopicItem> topicItems = new ArrayList<>();
                for (Topic topic : topics) {
                    StudentProcedureDetailWithAnswerResponse.TopicItem item =
                        new StudentProcedureDetailWithAnswerResponse.TopicItem();
                    item.setId(topic.getId());
                    item.setNumber(topic.getNumber());
                    item.setType(topic.getType());
                    item.setContent(topic.getContent());
                    item.setChoices(parseTopicChoices(topic.getChoices()));

                    String studentAnswer = studentAnswers.get(topic.getId());
                    item.setStudentAnswer(studentAnswer);

                    // 返回正确答案和是否正确
                    item.setCorrectAnswer(topic.getCorrectAnswer());
                    item.setIsCorrect(studentAnswer != null && studentAnswer.equals(topic.getCorrectAnswer()));

                    topicItems.add(item);
                }

                detail.setTopics(topicItems);
                return detail;
            }
        }
        return null;
    }

    /**
     * 填充题库详情（未提交）
     */
    private StudentProcedureDetailWithAnswerResponse.TopicDetail fillTopicDetailForUncompleted(
            ExperimentalProcedure procedure) {

        if (procedure.getProcedureTopicId() != null) {
            ProcedureTopic procedureTopic = procedureTopicMapper.selectById(procedure.getProcedureTopicId());
            if (procedureTopic != null) {
                StudentProcedureDetailWithAnswerResponse.TopicDetail detail =
                    new StudentProcedureDetailWithAnswerResponse.TopicDetail();
                detail.setId(procedureTopic.getId());
                detail.setIsRandom(procedureTopic.getIsRandom());
                detail.setNumber(procedureTopic.getNumber());
                detail.setTags(procedureTopic.getTags());

                // 查询题目列表（不含答案）
                List<Topic> topics = getTopicsForProcedure(procedureTopic);
                List<StudentProcedureDetailWithAnswerResponse.TopicItem> topicItems = new ArrayList<>();

                for (Topic topic : topics) {
                    StudentProcedureDetailWithAnswerResponse.TopicItem item =
                        new StudentProcedureDetailWithAnswerResponse.TopicItem();
                    item.setId(topic.getId());
                    item.setNumber(topic.getNumber());
                    item.setType(topic.getType());
                    item.setContent(topic.getContent());
                    item.setChoices(parseTopicChoices(topic.getChoices()));
                    topicItems.add(item);
                }

                detail.setTopics(topicItems);
                return detail;
            }
        }
        return null;
    }

    /**
     * 填充限时答题详情（已提交）
     */
    private StudentProcedureDetailWithAnswerResponse.TimedQuizDetail fillTimedQuizDetailForCompleted(
            ExperimentalProcedure procedure,
            StudentExperimentalProcedure studentProcedure,
            boolean isAfterEndTime) {

        if (procedure.getTimedQuizId() != null) {
            TimedQuizProcedure timedQuiz = timedQuizProcedureMapper.selectById(procedure.getTimedQuizId());
            if (timedQuiz != null) {
                StudentProcedureDetailWithAnswerResponse.TimedQuizDetail detail =
                    new StudentProcedureDetailWithAnswerResponse.TimedQuizDetail();
                detail.setId(timedQuiz.getId());
                detail.setIsRandom(timedQuiz.getIsRandom());
                detail.setNumber(timedQuiz.getTopicNumber());
                detail.setQuizTimeLimit(timedQuiz.getQuizTimeLimit());
                detail.setIsLocked(studentProcedure.getIsLocked());

                // 查询题目列表
                List<Topic> topics = getTopicsForTimedQuiz(procedure, timedQuiz);
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
                    item.setChoices(parseTopicChoices(topic.getChoices()));

                    String studentAnswer = studentAnswers.get(topic.getId());
                    item.setStudentAnswer(studentAnswer);

                    // 返回正确答案和是否正确
                    item.setCorrectAnswer(topic.getCorrectAnswer());
                    item.setIsCorrect(studentAnswer != null && studentAnswer.equals(topic.getCorrectAnswer()));

                    topicItems.add(item);
                }

                detail.setTopics(topicItems);
                return detail;
            }
        }
        return null;
    }

    /**
     * 填充限时答题详情（未提交）
     */
    private StudentProcedureDetailWithAnswerResponse.TimedQuizDetail fillTimedQuizDetailForUncompleted(
            ExperimentalProcedure procedure, String username) {

        if (procedure.getTimedQuizId() != null) {
            TimedQuizProcedure timedQuiz = timedQuizProcedureMapper.selectById(procedure.getTimedQuizId());
            if (timedQuiz != null) {
                StudentProcedureDetailWithAnswerResponse.TimedQuizDetail detail =
                    new StudentProcedureDetailWithAnswerResponse.TimedQuizDetail();
                detail.setId(timedQuiz.getId());
                detail.setIsRandom(timedQuiz.getIsRandom());
                detail.setNumber(timedQuiz.getTopicNumber());
                detail.setQuizTimeLimit(timedQuiz.getQuizTimeLimit());

                // 查询题目列表（不含答案）
                List<Topic> topics = getTopicsForTimedQuiz(procedure, timedQuiz);
                List<StudentProcedureDetailWithAnswerResponse.TopicItem> topicItems = new ArrayList<>();

                for (Topic topic : topics) {
                    StudentProcedureDetailWithAnswerResponse.TopicItem item =
                        new StudentProcedureDetailWithAnswerResponse.TopicItem();
                    item.setId(topic.getId());
                    item.setNumber(topic.getNumber());
                    item.setType(topic.getType());
                    item.setContent(topic.getContent());
                    item.setChoices(parseTopicChoices(topic.getChoices()));
                    topicItems.add(item);
                }

                detail.setTopics(topicItems);
                return detail;
            }
        }
        return null;
    }

    /**
     * 根据题库详情获取题目列表
     */
    private List<Topic> getTopicsForProcedure(ProcedureTopic procedureTopic) {
        if (Boolean.TRUE.equals(procedureTopic.getIsRandom())) {
            // 随机抽取：根据标签过滤题目
            if (procedureTopic.getTags() != null && !procedureTopic.getTags().isEmpty()) {
                String[] tagIds = procedureTopic.getTags().split(",");
                List<Long> tagIdList = Arrays.stream(tagIds)
                    .filter(s -> s != null && !s.isEmpty())
                    .map(Long::parseLong)
                    .toList();

                if (!tagIdList.isEmpty()) {
                    String sql = "SELECT topic_id FROM topic_tag_map " +
                                "WHERE tag_id IN (" + tagIdList.stream().map(String::valueOf).collect(Collectors.joining(",")) + ") " +
                                "GROUP BY topic_id " +
                                "HAVING COUNT(DISTINCT tag_id) = " + tagIdList.size();

                    List<Long> topicIds = topicTagMapMapper.selectList(
                        new LambdaQueryWrapper<TopicTagMap>().apply(sql)
                    ).stream()
                    .map(TopicTagMap::getTopicId)
                    .distinct()
                    .collect(Collectors.toList());

                    if (!topicIds.isEmpty()) {
                        LambdaQueryWrapper<Topic> topicWrapper = new LambdaQueryWrapper<>();
                        topicWrapper.in(Topic::getId, topicIds);

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

                        if (procedureTopic.getNumber() != null && procedureTopic.getNumber() > 0) {
                            topicWrapper.last("ORDER BY RAND() LIMIT " + procedureTopic.getNumber());
                        } else {
                            topicWrapper.last("ORDER BY RAND()");
                        }
                        return topicMapper.selectList(topicWrapper);
                    }
                }
            }
            return new ArrayList<>();
        } else {
            // 固定题目:从题库详情映射表查询
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
     * 获取限时答题的题目列表
     */
    private List<Topic> getTopicsForTimedQuiz(ExperimentalProcedure procedure, TimedQuizProcedure timedQuiz) {
        if (Boolean.TRUE.equals(timedQuiz.getIsRandom())) {
            // 随机模式：从题库中随机抽取
            return getRandomTopicsForTimedQuiz(timedQuiz);
        } else {
            // 老师选定模式:查询映射的题目
            LambdaQueryWrapper<ProcedureTopicMap> topicMapQueryWrapper = new LambdaQueryWrapper<>();
            topicMapQueryWrapper.eq(ProcedureTopicMap::getExperimentalProcedureId, procedure.getId());
            List<ProcedureTopicMap> topicMaps = procedureTopicMapMapper.selectList(topicMapQueryWrapper);

            if (topicMaps == null || topicMaps.isEmpty()) {
                return new ArrayList<>();
            }

            List<Long> topicIds = topicMaps.stream()
                    .map(ProcedureTopicMap::getTopicId)
                    .collect(Collectors.toList());

            LambdaQueryWrapper<Topic> topicQueryWrapper = new LambdaQueryWrapper<>();
            topicQueryWrapper.in(Topic::getId, topicIds)
                    .orderByAsc(Topic::getNumber);
            return topicMapper.selectList(topicQueryWrapper);
        }
    }

    /**
     * 随机抽取题目（限时答题专用）
     */
    private List<Topic> getRandomTopicsForTimedQuiz(TimedQuizProcedure timedQuiz) {
        if (timedQuiz.getTopicTags() != null && !timedQuiz.getTopicTags().isEmpty()) {
            String[] tagIds = timedQuiz.getTopicTags().split(",");
            List<Long> tagIdList = Arrays.stream(tagIds)
                .filter(s -> s != null && !s.isEmpty())
                .map(Long::parseLong)
                .toList();

            if (!tagIdList.isEmpty()) {
                String sql = "SELECT topic_id FROM topic_tag_map " +
                            "WHERE tag_id IN (" + tagIdList.stream().map(String::valueOf).collect(Collectors.joining(",")) + ") " +
                            "GROUP BY topic_id " +
                            "HAVING COUNT(DISTINCT tag_id) = " + tagIdList.size();

                List<Long> topicIds = topicTagMapMapper.selectList(
                    new LambdaQueryWrapper<TopicTagMap>().apply(sql)
                ).stream()
                .map(TopicTagMap::getTopicId)
                .distinct()
                .collect(Collectors.toList());

                if (!topicIds.isEmpty()) {
                    LambdaQueryWrapper<Topic> topicWrapper = new LambdaQueryWrapper<>();
                    topicWrapper.in(Topic::getId, topicIds);

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

                    if (timedQuiz.getTopicNumber() != null && timedQuiz.getTopicNumber() > 0) {
                        topicWrapper.last("ORDER BY RAND() LIMIT " + timedQuiz.getTopicNumber());
                    } else {
                        topicWrapper.last("ORDER BY RAND()");
                    }
                    return topicMapper.selectList(topicWrapper);
                }
            }
        }
        return new ArrayList<>();
    }

    /**
     * 解析题库答案
     */
    private Map<Long, String> parseTopicAnswers(String answer) {
        return AnswerMapJSONUntil.parseTopicData(answer);
    }

    /**
     * 解析题目选项字符串为List<TopicChoice>
     */
    private List<TopicChoice> parseTopicChoices(String choices) {
        if (choices == null || choices.isEmpty()) {
            return new ArrayList<>();
        }

        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            java.util.Map<String, String> map = mapper.readValue(choices,
                    new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, String>>() {});
            return TopicChoice.fromMap(map);
        } catch (Exception e) {
            log.error("解析题目选项失败, choices: {}", choices, e);
            return new ArrayList<>();
        }
    }
}
