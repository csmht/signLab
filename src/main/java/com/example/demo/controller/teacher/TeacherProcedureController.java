package com.example.demo.controller.teacher;

import com.example.demo.annotation.RequireRole;
import com.example.demo.enums.UserRole;
import com.example.demo.pojo.response.ApiResponse;
import com.example.demo.pojo.response.ProcedureSubmissionResponse;
import com.example.demo.service.ProcedureSubmissionService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 教师实验步骤批改控制器
 * 提供教师查询和批改实验步骤的接口
 */
@RequestMapping("/api/teacher/procedure-submissions")
@RestController
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class TeacherProcedureController {

    private final ProcedureSubmissionService procedureSubmissionService;

    /**
     * 查询课程的实验步骤列表
     *
     * @param courseId 课程ID
     * @param experimentId 实验ID（可选）
     * @param submissionStatus 提交状态（可选）
     * @return 实验步骤列表
     */
    @GetMapping("/course/{courseId}")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<List<ProcedureSubmissionResponse>> getCourseProcedureSubmissions(
            @PathVariable("courseId") String courseId,
            @RequestParam(value = "experimentId", required = false) String experimentId,
            @RequestParam(value = "submissionStatus", required = false) String submissionStatus) {
        try {
            List<ProcedureSubmissionResponse> submissions = procedureSubmissionService.getCourseSubmissions(
                    courseId, experimentId, submissionStatus
            );

            return ApiResponse.success(submissions, "查询成功");
        } catch (Exception e) {
            log.error("查询课程实验步骤失败", e);
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 查看实验步骤详情
     *
     * @param submissionId 实验步骤ID
     * @return 实验步骤详情
     */
    @GetMapping("/{submissionId}")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<ProcedureSubmissionResponse> getProcedureSubmissionById(@PathVariable("submissionId") Long submissionId) {
        try {
            ProcedureSubmissionResponse submission = procedureSubmissionService.getSubmissionById(submissionId);
            return ApiResponse.success(submission);
        } catch (com.example.demo.exception.BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("查看实验步骤详情失败", e);
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 批改实验步骤
     *
     * @param submissionId 实验步骤ID
     * @param request 批改请求（包含评语和评分）
     * @return 是否批改成功
     */
    @PostMapping("/{submissionId}/grade")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<Void> gradeProcedure(
            @PathVariable("submissionId") Long submissionId,
            @RequestBody GradeProcedureRequest request) {
        try {
            procedureSubmissionService.gradeProcedure(
                    submissionId,
                    request.getTeacherComment(),
                    request.getScore()
            );

            return ApiResponse.success(null, "批改成功");
        } catch (com.example.demo.exception.BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("批改实验步骤失败", e);
            return ApiResponse.error(500, "批改失败: " + e.getMessage());
        }
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
