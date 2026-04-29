package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.pojo.entity.ClassroomQuiz;
import com.example.demo.pojo.request.teacher.CreateClassroomQuizRequest;
import com.example.demo.pojo.request.teacher.CreateClassroomQuizRequestV2;
import com.example.demo.pojo.response.ClassroomQuizHistoryResponse;
import com.example.demo.pojo.response.ClassroomQuizHistoryResponseV2;
import com.example.demo.pojo.response.ClassroomQuizStatisticsResponse;
import com.example.demo.pojo.response.StudentClassroomQuizDetailResponse;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 教师课堂小测服务
 */
@Service
public interface TeacherClassroomQuizService extends IService<ClassroomQuiz> {

    /**
     * 创建课堂小测（新版）
     */
    Long createClassroomQuiz(CreateClassroomQuizRequestV2 request);

    /**
     * 创建课堂小测（旧版）
     */
    @Deprecated
    default Long createClassroomQuiz(CreateClassroomQuizRequest request) {
        return createClassroomQuiz(CreateClassroomQuizRequestV2.fromLegacy(request));
    }

    void startQuiz(Long quizId);

    void endQuiz(Long quizId);

    ClassroomQuizStatisticsResponse getQuizStatistics(Long quizId);

    StudentClassroomQuizDetailResponse getStudentAnswerDetail(Long quizId, String studentUsername);

    List<ClassroomQuizHistoryResponseV2> getHistoryQuizzes(Long classExperimentId);

    @Deprecated
    default List<ClassroomQuizHistoryResponse> getHistoryQuizzesLegacy(Long classExperimentId) {
        return getHistoryQuizzes(classExperimentId).stream().map(item -> {
            ClassroomQuizHistoryResponse legacy = new ClassroomQuizHistoryResponse();
            legacy.setId(item.getId());
            legacy.setClassExperimentId(item.getClassExperimentId());
            legacy.setQuizTitle(item.getQuizTitle());
            legacy.setQuizDescription(item.getQuizDescription());
            legacy.setQuizTimeLimit(item.getQuizTimeLimit());
            legacy.setStatus(item.getStatus());
            legacy.setStartTime(item.getStartTime());
            legacy.setEndTime(item.getEndTime());
            legacy.setCreatedBy(item.getCreatedBy());
            legacy.setCreatedTime(item.getCreatedTime());
            legacy.setProcedureTopic(item.getProcedureTopic() == null ? null : toLegacy(item.getProcedureTopic()));
            return legacy;
        }).toList();
    }

    private static ClassroomQuizHistoryResponse.ProcedureTopicInfo toLegacy(ClassroomQuizHistoryResponseV2.ProcedureTopicInfo item) {
        ClassroomQuizHistoryResponse.ProcedureTopicInfo legacy = new ClassroomQuizHistoryResponse.ProcedureTopicInfo();
        legacy.setIsRandom(item.getIsRandom());
        legacy.setNumber(item.getNumber());
        legacy.setTags(item.getTags());
        legacy.setTopicTypes(item.getTopicTypes());
        legacy.setSelectedTopicCount(item.getSelectedTopicCount());
        legacy.setTopics(item.getTopics() == null ? null : item.getTopics().stream().map(topic -> {
            ClassroomQuizHistoryResponse.ProcedureTopicInfo.TopicInfo legacyTopic = new ClassroomQuizHistoryResponse.ProcedureTopicInfo.TopicInfo();
            legacyTopic.setTopicId(topic.getTopicId());
            legacyTopic.setNumber(topic.getNumber());
            legacyTopic.setType(topic.getType());
            legacyTopic.setContent(topic.getContent());
            legacyTopic.setChoices(topic.getChoices());
            legacyTopic.setCorrectAnswer(topic.getCorrectAnswer());
            return legacyTopic;
        }).toList());
        return legacy;
    }
}
