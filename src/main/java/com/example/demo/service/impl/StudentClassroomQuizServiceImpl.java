package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.*;
import com.example.demo.pojo.entity.*;
import com.example.demo.pojo.request.student.SubmitClassroomQuizAnswerRequest;
import com.example.demo.pojo.response.StudentClassroomQuizDetailResponse;
import com.example.demo.service.StudentClassroomQuizService;
import com.example.demo.util.ClassroomQuizScorer;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 学生课堂小测服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StudentClassroomQuizServiceImpl implements StudentClassroomQuizService {

    private final ClassroomQuizMapper classroomQuizMapper;
    private final ClassroomQuizAnswerMapper classroomQuizAnswerMapper;
    private final ProcedureTopicMapper procedureTopicMapper;
    private final ProcedureTopicMapMapper procedureTopicMapMapper;
    private final TopicMapper topicMapper;
    private final TopicTagMapMapper topicTagMapMapper;
    private final ClassroomQuizScorer classroomQuizScorer;

    @Override
    public StudentClassroomQuizDetailResponse getCurrentQuiz(Long classExperimentId) {
        log.info("查询当前进行中的小测，班级实验ID: {}", classExperimentId);

        // 查询进行中的小测
        LambdaQueryWrapper<ClassroomQuiz> quizWrapper = new LambdaQueryWrapper<>();
        quizWrapper.eq(ClassroomQuiz::getClassExperimentId, classExperimentId);
        quizWrapper.eq(ClassroomQuiz::getStatus, 1); // 进行中
        quizWrapper.orderByDesc(ClassroomQuiz::getCreatedTime);
        quizWrapper.last("LIMIT 1");

        ClassroomQuiz quiz = classroomQuizMapper.selectOne(quizWrapper);
        if (quiz == null) {
            throw new BusinessException(404, "暂无进行中的小测");
        }

        // 查询题库配置
        ProcedureTopic procedureTopic = procedureTopicMapper.selectById(quiz.getProcedureTopicId());
        if (procedureTopic == null) {
            throw new BusinessException(404, "题库配置不存在");
        }

        // 查询题目列表
        List<Topic> topics = getTopicsForQuiz(quiz, procedureTopic);

        // 检查时间限制
        if (quiz.getEndTime() != null && LocalDateTime.now().isAfter(quiz.getEndTime())) {
            throw new BusinessException(400, "小测已结束");
        }

        // 构建响应（不包含正确答案）
        StudentClassroomQuizDetailResponse response = new StudentClassroomQuizDetailResponse();
        response.setQuizId(quiz.getId());
        response.setQuizTitle(quiz.getQuizTitle());
        response.setQuizDescription(quiz.getQuizDescription());
        response.setQuizTimeLimit(quiz.getQuizTimeLimit());
        response.setStartTime(quiz.getStartTime());
        response.setEndTime(quiz.getEndTime());
        response.setStatus(quiz.getStatus());
        response.setIsSubmitted(false);
        response.setScore(null);

        // 构建题目详情（不包含正确答案）
        List<StudentClassroomQuizDetailResponse.TopicDetail> topicDetails = topics.stream()
                .map(topic -> {
                    StudentClassroomQuizDetailResponse.TopicDetail detail =
                        new StudentClassroomQuizDetailResponse.TopicDetail();
                    detail.setTopicId(topic.getId());
                    detail.setNumber(topic.getNumber());
                    detail.setType(topic.getType());
                    detail.setContent(topic.getContent());
                    detail.setChoices(topic.getChoices());
                    detail.setStudentAnswer(null);
                    detail.setCorrectAnswer(null); // 进行中不返回正确答案
                    detail.setIsCorrect(null);
                    return detail;
                })
                .collect(Collectors.toList());

        response.setTopics(topicDetails);

        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitAnswer(SubmitClassroomQuizAnswerRequest request, String studentUsername, String classCode) {
        log.info("提交小测答案，小测ID: {}, 学生: {}", request.getQuizId(), studentUsername);

        // 查询小测信息
        ClassroomQuiz quiz = classroomQuizMapper.selectById(request.getQuizId());
        if (quiz == null) {
            throw new BusinessException(404, "小测不存在");
        }

        // 检查小测状态
        if (quiz.getStatus() != 1) {
            throw new BusinessException(400, "小测未开始或已结束");
        }

        // 检查时间限制
        if (quiz.getEndTime() != null && LocalDateTime.now().isAfter(quiz.getEndTime())) {
            throw new BusinessException(400, "小测已结束，不能提交");
        }

        // 检查是否已提交
        LambdaQueryWrapper<ClassroomQuizAnswer> existWrapper = new LambdaQueryWrapper<>();
        existWrapper.eq(ClassroomQuizAnswer::getClassroomQuizId, request.getQuizId());
        existWrapper.eq(ClassroomQuizAnswer::getStudentUsername, studentUsername);
        ClassroomQuizAnswer existAnswer = classroomQuizAnswerMapper.selectOne(existWrapper);
        if (existAnswer != null) {
            throw new BusinessException(400, "您已提交答案，不能重复提交");
        }

        // 查询题库配置
        ProcedureTopic procedureTopic = procedureTopicMapper.selectById(quiz.getProcedureTopicId());
        if (procedureTopic == null) {
            throw new BusinessException(404, "题库配置不存在");
        }

        // 查询题目列表
        List<Topic> topics = getTopicsForQuiz(quiz, procedureTopic);

        // 将答案Map转换为JSON字符串
        String answerJson;
        try {
            ObjectMapper mapper = new ObjectMapper();
            answerJson = mapper.writeValueAsString(request.getAnswers());
        } catch (Exception e) {
            log.error("答案JSON转换失败", e);
            throw new BusinessException(500, "答案格式错误");
        }

        // 自动评分
        BigDecimal score = classroomQuizScorer.calculateScore(request.getAnswers(), topics);
        Boolean isAllCorrect = classroomQuizScorer.isAllCorrect(request.getAnswers(), topics);

        // 保存答案记录
        ClassroomQuizAnswer answer = new ClassroomQuizAnswer();
        answer.setClassroomQuizId(request.getQuizId());
        answer.setStudentUsername(studentUsername);
        answer.setClassCode(classCode);
        answer.setAnswer(answerJson);
        answer.setScore(score);
        answer.setIsCorrect(isAllCorrect);
        answer.setSubmissionTime(LocalDateTime.now());

        classroomQuizAnswerMapper.insert(answer);

        log.info("小测答案提交成功，得分: {}", score);
    }

    @Override
    public StudentClassroomQuizDetailResponse getFinishedQuiz(Long quizId, String studentUsername) {
        log.info("查询已结束的小测，ID: {}, 学生: {}", quizId, studentUsername);

        // 查询小测信息
        ClassroomQuiz quiz = classroomQuizMapper.selectById(quizId);
        if (quiz == null) {
            throw new BusinessException(404, "小测不存在");
        }

        // 检查小测状态（必须是已结束）
        if (quiz.getStatus() != 2) {
            throw new BusinessException(400, "小测未结束，无法查看答案");
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

        // 构建响应（包含正确答案）
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

        // 构建题目详情（包含正确答案）
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

                    // 正确答案和是否正确（已结束的小测可以查看）
                    detail.setCorrectAnswer(topic.getCorrectAnswer());
                    String studentAnswer = studentAnswers.get(topic.getId());
                    detail.setIsCorrect(studentAnswer != null && studentAnswer.equals(topic.getCorrectAnswer()));

                    return detail;
                })
                .collect(Collectors.toList());

        response.setTopics(topicDetails);

        return response;
    }

    @Override
    public List<StudentClassroomQuizDetailResponse> getHistoryQuizzes(String studentUsername, Long classExperimentId) {
        log.info("查询历史小测列表，学生: {}, 班级实验ID: {}", studentUsername, classExperimentId);

        // 查询该班级实验的所有已结束小测
        LambdaQueryWrapper<ClassroomQuiz> quizWrapper = new LambdaQueryWrapper<>();
        quizWrapper.eq(ClassroomQuiz::getClassExperimentId, classExperimentId);
        quizWrapper.eq(ClassroomQuiz::getStatus, 2); // 已结束
        quizWrapper.orderByDesc(ClassroomQuiz::getCreatedTime);

        List<ClassroomQuiz> quizzes = classroomQuizMapper.selectList(quizWrapper);

        // 构建响应列表
        return quizzes.stream()
                .map(quiz -> {
                    // 查询学生答案
                    LambdaQueryWrapper<ClassroomQuizAnswer> answerWrapper = new LambdaQueryWrapper<>();
                    answerWrapper.eq(ClassroomQuizAnswer::getClassroomQuizId, quiz.getId());
                    answerWrapper.eq(ClassroomQuizAnswer::getStudentUsername, studentUsername);
                    ClassroomQuizAnswer answer = classroomQuizAnswerMapper.selectOne(answerWrapper);

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
                    response.setTopics(null); // 历史列表不包含题目详情

                    return response;
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取小测的题目列表
     */
    private List<Topic> getTopicsForQuiz(ClassroomQuiz quiz, ProcedureTopic procedureTopic) {
        if (Boolean.TRUE.equals(procedureTopic.getIsRandom())) {
            // 随机抽取
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
            // 固定题目
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
        if (answerJson == null || answerJson.isEmpty()) {
            return new HashMap<>();
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(answerJson,
                    new com.fasterxml.jackson.core.type.TypeReference<Map<Long, String>>() {});
        } catch (Exception e) {
            log.warn("解析题库答案失败: {}", e.getMessage());
            return new HashMap<>();
        }
    }
}
