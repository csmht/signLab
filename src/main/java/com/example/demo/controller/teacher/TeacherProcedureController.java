package com.example.demo.controller.teacher;

import com.example.demo.annotation.RequireRole;
import com.example.demo.enums.UserRole;
import com.example.demo.pojo.response.ApiResponse;
import com.example.demo.pojo.response.StudentProcedureSubmissionResponse;
import com.example.demo.service.StudentProcedureSubmissionService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 教师实验步骤批改控制器
 * 提供教师查询和批改实验步骤的接口
 * 基于 StudentExperimentalProcedure 实体
 */
@RequestMapping("/api/teacher/procedure-submissions")
@RestController
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class TeacherProcedureController {

    private final StudentProcedureSubmissionService studentProcedureSubmissionService;

    /**
     * 查询班级的实验步骤列表
     *
     * @param classExperimentId 班级实验ID
     * @param submissionStatus  提交状态（可选）
     * @return 实验步骤列表
     */
    @GetMapping("/class")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<List<StudentProcedureSubmissionResponse>> getClassProcedureSubmissions(
            @RequestParam("classExperimentId") Long classExperimentId,
            @RequestParam(value = "submissionStatus", required = false) Integer submissionStatus) {
        List<StudentProcedureSubmissionResponse> submissions = studentProcedureSubmissionService.getCourseSubmissionsByClassExperimentId(
                classExperimentId, submissionStatus
        );

        return ApiResponse.success(submissions, "查询成功");
    }

    /**
     * 查看实验步骤详情
     *
     * @param submissionId 实验步骤ID
     * @return 实验步骤详情
     */
    @GetMapping("/{submissionId}")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<StudentProcedureSubmissionResponse> getProcedureSubmissionById(@PathVariable("submissionId") Long submissionId) {
        StudentProcedureSubmissionResponse submission = studentProcedureSubmissionService.getSubmissionById(submissionId);
        return ApiResponse.success(submission);
    }

    /**
     * 批改实验步骤
     *
     * @param submissionId 实验步骤ID
     * @param request      批改请求（包含评语和评分）
     * @return 是否批改成功
     */
    @PostMapping("/{submissionId}/grade")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<Void> gradeProcedure(
            @PathVariable("submissionId") Long submissionId,
            @RequestBody GradeProcedureRequest request) {
        studentProcedureSubmissionService.gradeProcedure(
                submissionId,
                request.getTeacherComment(),
                request.getScore()
        );

        return ApiResponse.success(null, "批改成功");
    }

    /**
     * 批改实验步骤请求
     */
    @Data
    public static class GradeProcedureRequest {
        /**
         * 教师评语
         */
        private String teacherComment;

        /**
         * 评分
         */
        private BigDecimal score;
    }
}
