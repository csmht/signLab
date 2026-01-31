package com.example.demo.service;

import com.example.demo.pojo.request.student.SubmitClassroomQuizAnswerRequest;
import com.example.demo.pojo.response.StudentClassroomQuizDetailResponse;

import java.util.List;

/**
 * 学生课堂小测服务
 */
public interface StudentClassroomQuizService {

    /**
     * 查询当前课次的���行中小测
     *
     * @param classExperimentId 班级实验ID
     * @return 小测详情
     */
    StudentClassroomQuizDetailResponse getCurrentQuiz(Long classExperimentId);

    /**
     * 提交小测答案
     *
     * @param request          提交请求
     * @param studentUsername  学生用户名
     * @param classCode        班级编号
     */
    void submitAnswer(SubmitClassroomQuizAnswerRequest request, String studentUsername, String classCode);

    /**
     * 查询已结束的小测（包含正确答案和自己的答案）
     *
     * @param quizId           小测ID
     * @param studentUsername  学生用户名
     * @return 小测详情
     */
    StudentClassroomQuizDetailResponse getFinishedQuiz(Long quizId, String studentUsername);

    /**
     * 查询历史小测列表
     *
     * @param studentUsername  学生用户名
     * @param classExperimentId 班级实验ID
     * @return 小测列表
     */
    List<StudentClassroomQuizDetailResponse> getHistoryQuizzes(String studentUsername, Long classExperimentId);
}
