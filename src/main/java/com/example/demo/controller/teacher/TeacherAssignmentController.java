package com.example.demo.controller.teacher;

import com.example.demo.annotation.RequireRole;
import com.example.demo.enums.UserRole;
import com.example.demo.pojo.response.ApiResponse;
import com.example.demo.pojo.response.AssignmentSubmissionResponse;
import com.example.demo.service.AssignmentSubmissionService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 教师作业批改控制器
 * 提供教师查询和批改作业的接口
 */
@RequestMapping("/api/teacher/assignments")
@RestController
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class TeacherAssignmentController {

    private final AssignmentSubmissionService assignmentSubmissionService;

    /**
     * 查询课程的作业列表
     *
     * @param courseId 课程ID
     * @param experimentId 实验ID（可选）
     * @param submissionStatus 提交状态（可选）
     * @return 作业列表
     */
    @GetMapping("/course/{courseId}")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<List<AssignmentSubmissionResponse>> getCourseAssignments(
            @PathVariable("courseId") String courseId,
            @RequestParam(value = "experimentId", required = false) String experimentId,
            @RequestParam(value = "submissionStatus", required = false) String submissionStatus) {
        try {
            List<AssignmentSubmissionResponse> submissions = assignmentSubmissionService.getCourseSubmissions(
                    courseId, experimentId, submissionStatus
            );

            return ApiResponse.success(submissions, "查询成功");
        } catch (Exception e) {
            log.error("查询课程作业失败", e);
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 查看作业详情
     *
     * @param submissionId 作业ID
     * @return 作业详情
     */
    @GetMapping("/{submissionId}")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<AssignmentSubmissionResponse> getAssignmentById(@PathVariable("submissionId") Long submissionId) {
        try {
            AssignmentSubmissionResponse submission = assignmentSubmissionService.getSubmissionById(submissionId);
            return ApiResponse.success(submission);
        } catch (com.example.demo.exception.BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("查看作业详情失败", e);
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 批改作业
     *
     * @param submissionId 作业ID
     * @param request 批改请求（包含评语和评分）
     * @return 是否批改成功
     */
    @PostMapping("/{submissionId}/grade")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<Void> gradeAssignment(
            @PathVariable("submissionId") Long submissionId,
            @RequestBody GradeAssignmentRequest request) {
        try {
            assignmentSubmissionService.gradeAssignment(
                    submissionId,
                    request.getTeacherComment(),
                    request.getScore()
            );

            return ApiResponse.success(null, "批改成功");
        } catch (com.example.demo.exception.BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("批改作业失败", e);
            return ApiResponse.error(500, "批改失败: " + e.getMessage());
        }
    }

    /**
     * 批改作业请求
     */
    @Data
    public static class GradeAssignmentRequest {
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
