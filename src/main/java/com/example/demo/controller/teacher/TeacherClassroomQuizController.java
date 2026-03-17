package com.example.demo.controller.teacher;

import com.example.demo.annotation.RequireRole;
import com.example.demo.enums.UserRole;
import com.example.demo.pojo.response.ApiResponse;
import com.example.demo.pojo.request.teacher.CreateClassroomQuizRequest;
import com.example.demo.pojo.response.ClassroomQuizHistoryResponse;
import com.example.demo.pojo.response.ClassroomQuizStatisticsResponse;
import com.example.demo.pojo.response.StudentClassroomQuizDetailResponse;
import com.example.demo.service.TeacherClassroomQuizService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 教师课堂小测控制器
 */
@RequestMapping("/api/teacher/classroom-quiz")
@RestController
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class TeacherClassroomQuizController {

    private final TeacherClassroomQuizService teacherClassroomQuizService;

    /**
     * 创建课堂小测
     */
    @PostMapping
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<Long> createClassroomQuiz(@RequestBody CreateClassroomQuizRequest request) {
        Long quizId = teacherClassroomQuizService.createClassroomQuiz(request);
        return ApiResponse.success(quizId, "创建成功");
    }

    /**
     * 开始小测
     */
    @PostMapping("/{quizId}/start")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<Void> startQuiz(@PathVariable("quizId") Long quizId) {
        teacherClassroomQuizService.startQuiz(quizId);
        return ApiResponse.success(null, "小测已开始");
    }

    /**
     * 结束小测
     */
    @PostMapping("/{quizId}/end")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<Void> endQuiz(@PathVariable("quizId") Long quizId) {
        teacherClassroomQuizService.endQuiz(quizId);
        return ApiResponse.success(null, "小测已结束");
    }

    /**
     * 查询小测统计
     */
    @GetMapping("/{quizId}/statistics")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<ClassroomQuizStatisticsResponse> getQuizStatistics(
            @PathVariable("quizId") Long quizId) {
        ClassroomQuizStatisticsResponse response = teacherClassroomQuizService.getQuizStatistics(quizId);
        return ApiResponse.success(response, "查询成功");
    }

    /**
     * 查询指定学生答题详情
     */
    @GetMapping("/{quizId}/student/{studentUsername}")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<StudentClassroomQuizDetailResponse> getStudentAnswerDetail(
            @PathVariable("quizId") Long quizId,
            @PathVariable("studentUsername") String studentUsername) {
        StudentClassroomQuizDetailResponse response =
            teacherClassroomQuizService.getStudentAnswerDetail(quizId, studentUsername);
        return ApiResponse.success(response, "查询成功");
    }

    /**
     * 查询教师发布的历史小测列表
     */
    @GetMapping("/history")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<List<ClassroomQuizHistoryResponse>> getHistoryQuizzes(
            @RequestParam(value = "classExperimentId", required = false) Long classExperimentId) {
        List<ClassroomQuizHistoryResponse> quizzes = teacherClassroomQuizService.getHistoryQuizzes(classExperimentId);
        return ApiResponse.success(quizzes, "查询成功");
    }
}
