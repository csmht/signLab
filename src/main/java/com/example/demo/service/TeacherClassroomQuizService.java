package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.pojo.entity.ClassroomQuiz;
import com.example.demo.pojo.request.teacher.CreateClassroomQuizRequest;
import com.example.demo.pojo.response.ClassroomQuizStatisticsResponse;
import com.example.demo.pojo.response.StudentClassroomQuizDetailResponse;

/**
 * 教师课堂小测服务
 */
public interface TeacherClassroomQuizService extends IService<ClassroomQuiz> {

    /**
     * 创建课堂小测
     *
     * @param request 创建请求
     * @return 小测ID
     */
    Long createClassroomQuiz(CreateClassroomQuizRequest request);

    /**
     * 开始小测
     *
     * @param quizId 小测ID
     */
    void startQuiz(Long quizId);

    /**
     * 结束小测
     *
     * @param quizId 小测ID
     */
    void endQuiz(Long quizId);

    /**
     * 查询小测统计
     *
     * @param quizId 小测ID
     * @return 统计信息
     */
    ClassroomQuizStatisticsResponse getQuizStatistics(Long quizId);

    /**
     * 查询指定学生���题详情
     *
     * @param quizId          小测ID
     * @param studentUsername 学生用户名
     * @return 答题详情
     */
    StudentClassroomQuizDetailResponse getStudentAnswerDetail(Long quizId, String studentUsername);

    /**
     * 查询教师发布的历史小测列表
     *
     * @param classExperimentId 班级实验ID（可选，不传则查询所有）
     * @return 历史小测列表
     */
    java.util.List<com.example.demo.pojo.entity.ClassroomQuiz> getHistoryQuizzes(Long classExperimentId);
}
