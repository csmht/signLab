package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.*;
import com.example.demo.pojo.entity.*;
import com.example.demo.pojo.request.teacher.CreateClassroomQuizRequest;
import com.example.demo.pojo.response.ClassroomQuizStatisticsResponse;
import com.example.demo.pojo.response.StudentClassroomQuizDetailResponse;
import com.example.demo.service.ClassExperimentClassRelationService;
import com.example.demo.service.TeacherClassroomQuizService;
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

        // 验证题库配置ID是否存在
        ProcedureTopic procedureTopic = procedureTopicMapper.selectById(request.getProcedureTopicId());
        if (procedureTopic == null) {
            throw new BusinessException(404, "题库配置不存在");
        }

        // 创建小测记录
        ClassroomQuiz quiz = new ClassroomQuiz();
        quiz.setClassExperimentId(request.getClassExperimentId());
        quiz.setProcedureTopicId(request.getProcedureTopicId());
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
        QueryWrapper<ClassroomQuizAnswer> answerWrapper = new QueryWrapper<>();
        answerWrapper.eq("classroom_quiz_id", quizId);
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
        QueryWrapper<ClassroomQuizAnswer> answerWrapper = new QueryWrapper<>();
        answerWrapper.eq("classroom_quiz_id", quizId);
        answerWrapper.eq("student_username", studentUsername);
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

    /**
     * 获取当前用户名
     */
    private String getCurrentUsername() {
        return com.example.demo.util.SecurityUtil.getCurrentUsername().orElse("system");
    }

    @Override
    public java.util.List<ClassroomQuiz> getHistoryQuizzes(Long classExperimentId) {
        log.info("查询教师历史小测列表，班级实验ID: {}", classExperimentId);

        // 获取当前教师用户名
        String currentUsername = getCurrentUsername();

        // 构建查询条件
        QueryWrapper<ClassroomQuiz> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("created_by", currentUsername);

        // 如果指定了班级实验ID，则只查询该课次的小测
        if (classExperimentId != null) {
            queryWrapper.eq("class_experiment_id", classExperimentId);
        }

        // 按创建时间倒序排列
        queryWrapper.orderByDesc("created_time");

        return classroomQuizMapper.selectList(queryWrapper);
    }
}
