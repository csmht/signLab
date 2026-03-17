package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.*;
import com.example.demo.pojo.entity.*;
import com.example.demo.pojo.request.teacher.CreateClassroomQuizRequest;
import com.example.demo.pojo.response.ClassroomQuizHistoryResponse;
import com.example.demo.pojo.response.ClassroomQuizStatisticsResponse;
import com.example.demo.pojo.response.StudentClassroomQuizDetailResponse;
import com.example.demo.service.ClassExperimentClassRelationService;
import com.example.demo.service.TeacherClassroomQuizService;
import com.example.demo.util.AnswerMapJSONUntil;
import com.example.demo.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 教师课堂小测服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TeacherClassroomQuizServiceImpl extends ServiceImpl<ClassroomQuizMapper, ClassroomQuiz>
        implements TeacherClassroomQuizService {

    private final ClassroomQuizMapper classroomQuizMapper;
    private final ClassroomQuizAnswerMapper classroomQuizAnswerMapper;
    private final ClassExperimentMapper classExperimentMapper;
    private final ProcedureTopicMapper procedureTopicMapper;
    private final ProcedureTopicMapMapper procedureTopicMapMapper;
    private final TopicMapper topicMapper;
    private final TopicTagMapMapper topicTagMapMapper;
    private final ClassExperimentClassRelationService classExperimentClassRelationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createClassroomQuiz(CreateClassroomQuizRequest request) {
        log.info("创建课堂小测，请求: {}", request);

        // 验证班级实验ID是否存在
        ClassExperiment classExperiment = classExperimentMapper.selectById(request.getClassExperimentId());
        if (classExperiment == null) {
            throw new BusinessException(404, "班级实验不存在");
        }

        // 如果没有传入已有的题库配置ID，则创建新的题库配置
        Long procedureTopicId = createProcedureTopicForQuiz(request);
        log.info("为课堂小测创建新的题库配置，ID: {}", procedureTopicId);


        // 创建小测记录
        ClassroomQuiz quiz = new ClassroomQuiz();
        quiz.setClassExperimentId(request.getClassExperimentId());
        quiz.setProcedureTopicId(procedureTopicId);
        quiz.setQuizTitle(request.getQuizTitle());
        quiz.setQuizDescription(request.getQuizDescription());
        quiz.setQuizTimeLimit(request.getQuizTimeLimit());
        quiz.setStatus(0); // 未开始
        quiz.setCreatedBy(getCurrentUsername());
        quiz.setCreatedTime(LocalDateTime.now());

        classroomQuizMapper.insert(quiz);

        log.info("课堂小测创建成功，ID: {}", quiz.getId());
        return quiz.getId();
    }

    /**
     * 为课堂小测创建题库配置
     */
    private Long createProcedureTopicForQuiz(CreateClassroomQuizRequest request) {
        // 验证题库配置字段
        if (request.getIsRandom() == null) {
            throw new BusinessException(400, "是否随机抽取不能为空");
        }

        // 验证随机模式的字段
        if (Boolean.TRUE.equals(request.getIsRandom())) {
            if (request.getTopicNumber() == null || request.getTopicNumber() <= 0) {
                throw new BusinessException(400, "随机模式下题目数量必须大于0");
            }
        } else {
            // 验证老师选定模式的字段
            if (request.getTeacherSelectedTopicIds() == null || request.getTeacherSelectedTopicIds().isEmpty()) {
                throw new BusinessException(400, "非随机模式下必须选择题目");
            }
        }

        // 创建题库配置记录（experimentalProcedureId 设为 null，表示这是课堂小测专用的配置）
        ProcedureTopic procedureTopic = new ProcedureTopic();
        procedureTopic.setExperimentalProcedureId(null); // 课堂小测不关联实验步骤
        procedureTopic.setIsRandom(request.getIsRandom());
        procedureTopic.setNumber(request.getTopicNumber());
        procedureTopic.setTags(joinListToString(request.getTopicTags()));
        procedureTopic.setTopicTypes(joinIntegerListToString(request.getTopicTypes()));
        procedureTopic.setCreatedTime(LocalDateTime.now());
        procedureTopic.setIsDeleted(false);

        procedureTopicMapper.insert(procedureTopic);

        // 如果是老师选定模式，创建题目映射记录
        if (!Boolean.TRUE.equals(request.getIsRandom())) {
            List<Long> topicIds = request.getTeacherSelectedTopicIds();

            // 验证题目是否存在
            LambdaQueryWrapper<Topic> topicQueryWrapper = new LambdaQueryWrapper<>();
            topicQueryWrapper.in(Topic::getId, topicIds);
            long existingTopicCount = topicMapper.selectCount(topicQueryWrapper);

            if (existingTopicCount != topicIds.size()) {
                throw new BusinessException(400,
                        String.format("有%d道题目不存在或已删除", topicIds.size() - existingTopicCount));
            }

            // 创建题目映射记录
            for (Long topicId : topicIds) {
                ProcedureTopicMap topicMap = new ProcedureTopicMap();
                topicMap.setExperimentalProcedureId(null); // 课堂小测不关联实验步骤
                topicMap.setTopicId(topicId);
                topicMap.setProcedureTopicId(procedureTopic.getId());
                procedureTopicMapMapper.insert(topicMap);
            }
            log.info("为课堂小测创建了{}道题目的映射记录", topicIds.size());
        }

        return procedureTopic.getId();
    }

    /**
     * 将 Long 列表拼接成逗号分隔的字符串
     */
    private String joinListToString(List<Long> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }

    /**
     * 将 Integer 列表拼接成逗号分隔的字符串
     */
    private String joinIntegerListToString(List<Integer> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startQuiz(Long quizId) {
        log.info("开始课堂小测，ID: {}", quizId);

        ClassroomQuiz quiz = classroomQuizMapper.selectById(quizId);
        if (quiz == null) {
            throw new BusinessException(404, "小测不存在");
        }

        if (quiz.getStatus() != 0) {
            throw new BusinessException(400, "小测状态不正确，无法开始");
        }

        // 检查该班级实验是否有正在进行的小测，如果有则先停止
        stopOngoingQuiz(quiz.getClassExperimentId());

        // 更新状态为进行中
        quiz.setStatus(1);
        quiz.setStartTime(LocalDateTime.now());
        // 计算结束时间
        if (quiz.getQuizTimeLimit() != null && quiz.getQuizTimeLimit() > 0) {
            quiz.setEndTime(quiz.getStartTime().plusMinutes(quiz.getQuizTimeLimit()));
        }

        classroomQuizMapper.updateById(quiz);
        log.info("课堂小测已开始，ID: {}", quizId);
    }

    /**
     * 停止班级实验中正在进行的小测
     *
     * @param classExperimentId 班级实验ID
     */
    private void stopOngoingQuiz(Long classExperimentId) {
        LambdaQueryWrapper<ClassroomQuiz> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ClassroomQuiz::getClassExperimentId, classExperimentId);
        wrapper.eq(ClassroomQuiz::getStatus, 1);
        wrapper.last("LIMIT 1");

        ClassroomQuiz ongoingQuiz = classroomQuizMapper.selectOne(wrapper);
        if (ongoingQuiz != null) {
            log.info("发现班级实验 {} 有正在进行的小测 {}，先停止", classExperimentId, ongoingQuiz.getId());
            ongoingQuiz.setStatus(2);
            ongoingQuiz.setEndTime(LocalDateTime.now());
            classroomQuizMapper.updateById(ongoingQuiz);
            log.info("已停止小测，ID: {}", ongoingQuiz.getId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void endQuiz(Long quizId) {
        log.info("结束课堂小测，ID: {}", quizId);

        ClassroomQuiz quiz = classroomQuizMapper.selectById(quizId);
        if (quiz == null) {
            throw new BusinessException(404, "小测不存在");
        }

        if (quiz.getStatus() != 1) {
            throw new BusinessException(400, "小测状态不正确，无法结束");
        }

        // 更新状态为已结束
        quiz.setStatus(2);
        quiz.setEndTime(LocalDateTime.now());

        classroomQuizMapper.updateById(quiz);
        log.info("课堂小测已结束，ID: {}", quizId);
    }

    @Override
    public ClassroomQuizStatisticsResponse getQuizStatistics(Long quizId) {
        log.info("查询课堂小测统计，ID: {}", quizId);

        // 查询小测信息
        ClassroomQuiz quiz = classroomQuizMapper.selectById(quizId);
        if (quiz == null) {
            throw new BusinessException(404, "小测不存在");
        }

        // 查询题库配置
        ProcedureTopic procedureTopic = procedureTopicMapper.selectById(quiz.getProcedureTopicId());
        if (procedureTopic == null) {
            throw new BusinessException(404, "题库配置不存在");
        }

        // 查询所有参与班级
        List<String> classCodes = classExperimentClassRelationService.getClassCodesByExperimentId(quiz.getClassExperimentId());

        // 统计总参与人数（所有班级的学生数总和）
        // TODO: 这里需要通过班级查询学生总数，暂时设为0
        Integer totalParticipants = 0;

        // 查询所有答案记录
        LambdaQueryWrapper<ClassroomQuizAnswer> answerWrapper = new LambdaQueryWrapper<>();
        answerWrapper.eq(ClassroomQuizAnswer::getClassroomQuizId, quizId);
        List<ClassroomQuizAnswer> answers = classroomQuizAnswerMapper.selectList(answerWrapper);

        Integer submittedCount = answers.size();
        BigDecimal completionRate = totalParticipants > 0
                ? new BigDecimal(submittedCount)
                        .multiply(new BigDecimal(100))
                        .divide(new BigDecimal(totalParticipants), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // 计算平均分
        BigDecimal averageScore = answers.stream()
                .filter(a -> a.getScore() != null)
                .map(ClassroomQuizAnswer::getScore)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        averageScore = submittedCount > 0
                ? averageScore.divide(new BigDecimal(submittedCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // 计算全对率
        long correctCount = answers.stream()
                .filter(a -> Boolean.TRUE.equals(a.getIsCorrect()))
                .count();
        BigDecimal correctRate = submittedCount > 0
                ? new BigDecimal(correctCount)
                        .multiply(new BigDecimal(100))
                        .divide(new BigDecimal(submittedCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // 查询题目列表
        List<Topic> topics = getTopicsForQuiz(quiz, procedureTopic);

        // 题目级统计
        List<ClassroomQuizStatisticsResponse.TopicStatistics> topicStatistics = calculateTopicStatistics(answers, topics);

        // 学生级统计
        List<ClassroomQuizStatisticsResponse.StudentAnswerInfo> studentAnswers = answers.stream()
                .map(answer -> {
                    ClassroomQuizStatisticsResponse.StudentAnswerInfo info =
                        new ClassroomQuizStatisticsResponse.StudentAnswerInfo();
                    info.setStudentUsername(answer.getStudentUsername());
                    info.setClassCode(answer.getClassCode());
                    info.setScore(answer.getScore());
                    info.setIsCorrect(answer.getIsCorrect());
                    info.setSubmissionTime(answer.getSubmissionTime());
                    return info;
                })
                .collect(Collectors.toList());

        // 构建响应
        ClassroomQuizStatisticsResponse response = new ClassroomQuizStatisticsResponse();
        response.setQuizId(quiz.getId());
        response.setQuizTitle(quiz.getQuizTitle());
        response.setQuizDescription(quiz.getQuizDescription());
        response.setStatus(quiz.getStatus());
        response.setStartTime(quiz.getStartTime());
        response.setEndTime(quiz.getEndTime());
        response.setTotalParticipants(totalParticipants);
        response.setSubmittedCount(submittedCount);
        response.setCompletionRate(completionRate);
        response.setAverageScore(averageScore);
        response.setCorrectRate(correctRate);
        response.setTopicStatistics(topicStatistics);
        response.setStudentAnswers(studentAnswers);

        return response;
    }

    @Override
    public StudentClassroomQuizDetailResponse getStudentAnswerDetail(Long quizId, String studentUsername) {
        log.info("查询学生答题详情，小测ID: {}, 学生: {}", quizId, studentUsername);

        // 查询小测信息
        ClassroomQuiz quiz = classroomQuizMapper.selectById(quizId);
        if (quiz == null) {
            throw new BusinessException(404, "小测不存在");
        }

        // 查询题库配置
        ProcedureTopic procedureTopic = procedureTopicMapper.selectById(quiz.getProcedureTopicId());
        if (procedureTopic == null) {
            throw new BusinessException(404, "题库配置不存在");
        }

        // 查询学生答案
        LambdaQueryWrapper<ClassroomQuizAnswer> answerWrapper = new LambdaQueryWrapper<>();
        answerWrapper.eq(ClassroomQuizAnswer::getClassroomQuizId, quizId);
        answerWrapper.eq(ClassroomQuizAnswer::getStudentUsername, studentUsername);
        ClassroomQuizAnswer answer = classroomQuizAnswerMapper.selectOne(answerWrapper);

        // 查询题目列表
        List<Topic> topics = getTopicsForQuiz(quiz, procedureTopic);

        // 解析学生答案
        Map<Long, String> studentAnswers = parseTopicAnswers(answer != null ? answer.getAnswer() : null);

        // 构建响应
        StudentClassroomQuizDetailResponse response = new StudentClassroomQuizDetailResponse();
        response.setQuizId(quiz.getId());
        response.setQuizTitle(quiz.getQuizTitle());
        response.setQuizDescription(quiz.getQuizDescription());
        response.setQuizTimeLimit(quiz.getQuizTimeLimit());
        response.setStartTime(quiz.getStartTime());
        response.setEndTime(quiz.getEndTime());
        response.setStatus(quiz.getStatus());
        response.setIsSubmitted(answer != null);
        response.setSubmissionTime(answer != null ? answer.getSubmissionTime() : null);
        response.setScore(answer != null ? answer.getScore() : null);

        // 构建题目详情
        List<StudentClassroomQuizDetailResponse.TopicDetail> topicDetails = topics.stream()
                .map(topic -> {
                    StudentClassroomQuizDetailResponse.TopicDetail detail =
                        new StudentClassroomQuizDetailResponse.TopicDetail();
                    detail.setTopicId(topic.getId());
                    detail.setNumber(topic.getNumber());
                    detail.setType(topic.getType());
                    detail.setContent(topic.getContent());
                    detail.setChoices(topic.getChoices());

                    // 学生答案
                    detail.setStudentAnswer(studentAnswers.get(topic.getId()));

                    // 正确答案和是否正确（教师始终可以查看）
                    detail.setCorrectAnswer(topic.getCorrectAnswer());
                    String studentAnswer = studentAnswers.get(topic.getId());
                    detail.setIsCorrect(studentAnswer != null && studentAnswer.equals(topic.getCorrectAnswer()));

                    return detail;
                })
                .collect(Collectors.toList());

        response.setTopics(topicDetails);

        return response;
    }

    /**
     * 获取小测的题目列表
     */
    private List<Topic> getTopicsForQuiz(ClassroomQuiz quiz, ProcedureTopic procedureTopic) {
        if (Boolean.TRUE.equals(procedureTopic.getIsRandom())) {
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

                // 先查询所有题目
                LambdaQueryWrapper<Topic> topicWrapper = new LambdaQueryWrapper<>();
                topicWrapper.in(Topic::getId, topicIds);
                List<Topic> topics = topicMapper.selectList(topicWrapper);

                // 按照 procedureTopicMaps 中的顺序重新排序
                Map<Long, Topic> topicMap = topics.stream()
                        .collect(Collectors.toMap(Topic::getId, t -> t));

                return topicMaps.stream()
                        .map(m -> topicMap.get(m.getTopicId()))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
            }
            return new ArrayList<>();
        }
    }

    /**
     * 计算题目级统计
     */
    private List<ClassroomQuizStatisticsResponse.TopicStatistics> calculateTopicStatistics(
            List<ClassroomQuizAnswer> answers, List<Topic> topics) {

        return topics.stream()
                .map(topic -> {
                    ClassroomQuizStatisticsResponse.TopicStatistics stats =
                        new ClassroomQuizStatisticsResponse.TopicStatistics();
                    stats.setTopicId(topic.getId());
                    stats.setNumber(topic.getNumber());
                    stats.setType(topic.getType());
                    stats.setContent(topic.getContent());
                    stats.setCorrectAnswer(topic.getCorrectAnswer());

                    // 统计该题目的正确率
                    int correct = 0;
                    for (ClassroomQuizAnswer answer : answers) {
                        Map<Long, String> studentAnswers = parseTopicAnswers(answer.getAnswer());
                        String studentAnswer = studentAnswers.get(topic.getId());
                        if (studentAnswer != null && studentAnswer.equals(topic.getCorrectAnswer())) {
                            correct++;
                        }
                    }

                    stats.setCorrectCount(correct);
                    stats.setIncorrectCount(answers.size() - correct);

                    BigDecimal correctRate = answers.size() > 0
                            ? new BigDecimal(correct)
                                    .multiply(new BigDecimal(100))
                                    .divide(new BigDecimal(answers.size()), 2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;
                    stats.setCorrectRate(correctRate);

                    return stats;
                })
                .collect(Collectors.toList());
    }

    /**
     * 解析题库答案JSON
     */
    private Map<Long, String> parseTopicAnswers(String answerJson) {
        return AnswerMapJSONUntil.parseTopicData(answerJson);
    }

    /**
     * 获取当前用户名
     */
    private String getCurrentUsername() {
        return SecurityUtil.getCurrentUsername().orElse("system");
    }

    @Override
    public List<ClassroomQuizHistoryResponse> getHistoryQuizzes(Long classExperimentId) {
        log.info("查询教师历史小测列表，班级实验ID: {}", classExperimentId);

        // 获取当前教师用户名
        String currentUsername = getCurrentUsername();

        // 构建查询条件
        LambdaQueryWrapper<ClassroomQuiz> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ClassroomQuiz::getCreatedBy, currentUsername);

        // 如果指定了班级实验ID，则只查询该课次的小测
        if (classExperimentId != null) {
            queryWrapper.eq(ClassroomQuiz::getClassExperimentId, classExperimentId);
        }

        // 按创建时间倒序排列
        queryWrapper.orderByDesc(ClassroomQuiz::getCreatedTime);

        List<ClassroomQuiz> quizzes = classroomQuizMapper.selectList(queryWrapper);

        // 转换为响应对象
        return quizzes.stream().map(quiz -> {
            ClassroomQuizHistoryResponse response = new ClassroomQuizHistoryResponse();
            response.setId(quiz.getId());
            response.setClassExperimentId(quiz.getClassExperimentId());
            response.setQuizTitle(quiz.getQuizTitle());
            response.setQuizDescription(quiz.getQuizDescription());
            response.setQuizTimeLimit(quiz.getQuizTimeLimit());
            response.setStatus(quiz.getStatus());
            response.setStartTime(quiz.getStartTime());
            response.setEndTime(quiz.getEndTime());
            response.setCreatedBy(quiz.getCreatedBy());
            response.setCreatedTime(quiz.getCreatedTime());

            // 查询题库配置信息
            ProcedureTopic procedureTopic = procedureTopicMapper.selectById(quiz.getProcedureTopicId());
            if (procedureTopic != null) {
                // 非随机模式下，查询选定的题���数量
                Integer selectedTopicCount = null;
                if (!Boolean.TRUE.equals(procedureTopic.getIsRandom())) {
                    LambdaQueryWrapper<ProcedureTopicMap> mapWrapper = new LambdaQueryWrapper<>();
                    mapWrapper.eq(ProcedureTopicMap::getProcedureTopicId, procedureTopic.getId());
                    selectedTopicCount = Math.toIntExact(procedureTopicMapMapper.selectCount(mapWrapper));
                }
                ClassroomQuizHistoryResponse.ProcedureTopicInfo procedureTopicInfo =
                        ClassroomQuizHistoryResponse.ProcedureTopicInfo.fromEntity(procedureTopic, selectedTopicCount);

                // 查询题目列表
                List<Topic> topics = getTopicsForQuiz(quiz, procedureTopic);
                List<ClassroomQuizHistoryResponse.ProcedureTopicInfo.TopicInfo> topicInfos = topics.stream()
                        .map(topic -> {
                            ClassroomQuizHistoryResponse.ProcedureTopicInfo.TopicInfo topicInfo =
                                new ClassroomQuizHistoryResponse.ProcedureTopicInfo.TopicInfo();
                            topicInfo.setTopicId(topic.getId());
                            topicInfo.setNumber(topic.getNumber());
                            topicInfo.setType(topic.getType());
                            topicInfo.setContent(topic.getContent());
                            topicInfo.setChoices(topic.getChoices());
                            topicInfo.setCorrectAnswer(topic.getCorrectAnswer());
                            return topicInfo;
                        })
                        .collect(Collectors.toList());
                procedureTopicInfo.setTopics(topicInfos);

                response.setProcedureTopic(procedureTopicInfo);
            }

            return response;
        }).collect(Collectors.toList());
    }
}
