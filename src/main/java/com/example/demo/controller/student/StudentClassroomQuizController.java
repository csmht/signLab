package com.example.demo.controller.student;

import com.example.demo.annotation.RequireRole;
import com.example.demo.enums.UserRole;
import com.example.demo.pojo.request.student.SubmitClassroomQuizAnswerRequest;
import com.example.demo.pojo.response.ApiResponse;
import com.example.demo.pojo.response.StudentClassroomQuizDetailResponse;
import com.example.demo.service.StudentClassroomQuizService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学生课堂小测控制器
 */
@RequestMapping("/api/student/classroom-quiz")
@RestController
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class StudentClassroomQuizController {

    private final StudentClassroomQuizService studentClassroomQuizService;

    /**
     * 查询当前课次的进行中小测
     */
    @GetMapping("/current")
    @RequireRole(value = UserRole.STUDENT)
    public ApiResponse<StudentClassroomQuizDetailResponse> getCurrentQuiz(
            @RequestParam("classExperimentId") Long classExperimentId) {
        StudentClassroomQuizDetailResponse response =
            studentClassroomQuizService.getCurrentQuiz(classExperimentId);
        return ApiResponse.success(response, "查询成功");
    }

    /**
     * 提交小测答案
     */
    @PostMapping("/submit")
    @RequireRole(value = UserRole.STUDENT)
    public ApiResponse<Void> submitAnswer(@RequestBody SubmitClassroomQuizAnswerRequest request,
                                          @RequestParam("classExperimentId") Long classExperimentId) {
        // 从上下文获取学生信息
        String studentUsername = getCurrentStudentUsername();

        studentClassroomQuizService.submitAnswer(request, studentUsername, classExperimentId);
        return ApiResponse.success(null, "提交成功");
    }

    /**
     * 查询已结束的小测（含正确答案和自己的答案）
     */
    @GetMapping("/{quizId}")
    @RequireRole(value = UserRole.STUDENT)
    public ApiResponse<StudentClassroomQuizDetailResponse> getFinishedQuiz(
            @PathVariable("quizId") Long quizId) {
        String studentUsername = getCurrentStudentUsername();
        StudentClassroomQuizDetailResponse response =
            studentClassroomQuizService.getFinishedQuiz(quizId, studentUsername);
        return ApiResponse.success(response, "查询成功");
    }

    /**
     * 查询历史小测列表
     */
    @GetMapping("/history")
    @RequireRole(value = UserRole.STUDENT)
    public ApiResponse<List<StudentClassroomQuizDetailResponse>> getHistoryQuizzes(
            @RequestParam("classExperimentId") Long classExperimentId) {
        String studentUsername = getCurrentStudentUsername();
        List<StudentClassroomQuizDetailResponse> responses =
            studentClassroomQuizService.getHistoryQuizzes(studentUsername, classExperimentId);
        return ApiResponse.success(responses, "查询成功");
    }

    // 辅助方法(实际项目中应从统一上下文获取)
    private String getCurrentStudentUsername() {
        return com.example.demo.util.SecurityUtil.getCurrentUsername().orElse(null);
    }
}
