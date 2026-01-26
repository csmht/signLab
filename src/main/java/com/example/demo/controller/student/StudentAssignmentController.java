package com.example.demo.controller.student;

import com.example.demo.annotation.RequireRole;
import com.example.demo.enums.UserRole;
import com.example.demo.pojo.response.ApiResponse;
import com.example.demo.pojo.response.AssignmentSubmissionResponse;
import com.example.demo.service.AssignmentSubmissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 学生作业提交控制器
 * 提供学生上传、提交、查询作业的接口
 */
@RequestMapping("/api/student/assignments")
@RestController
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class StudentAssignmentController {

    private final AssignmentSubmissionService assignmentSubmissionService;

    /**
     * 上传作业文件
     *
     * @param courseId 课程ID
     * @param experimentId 实验ID
     * @param submissionType 提交类型（实验报告、数据文件等）
     * @param file 作业文件
     * @return 上传后的作业信息
     */
    @PostMapping("/upload")
    @RequireRole(value = UserRole.STUDENT)
    public ApiResponse<AssignmentSubmissionResponse> uploadAssignment(
            @RequestParam("courseId") String courseId,
            @RequestParam("experimentId") String experimentId,
            @RequestParam("submissionType") String submissionType,
            @RequestParam("file") MultipartFile file) {
        try {
            // 获取当前登录学生用户名
            String studentUsername = com.example.demo.util.SecurityUtil.getCurrentUsername()
                    .orElseThrow(() -> new com.example.demo.exception.BusinessException(401, "未登录"));

            AssignmentSubmissionResponse response = assignmentSubmissionService.uploadAssignment(
                    courseId, experimentId, studentUsername, submissionType, file
            );

            return ApiResponse.success(response, "上传成功");
        } catch (com.example.demo.exception.BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("上传作业失败", e);
            return ApiResponse.error(500, "上传失败: " + e.getMessage());
        }
    }

    /**
     * 提交作业（将草稿状态改为已提交）
     *
     * @param submissionId 作业ID
     * @return 是否提交成功
     */
    @PostMapping("/{submissionId}/submit")
    @RequireRole(value = UserRole.STUDENT)
    public ApiResponse<Void> submitAssignment(@PathVariable("submissionId") Long submissionId) {
        try {
            String studentUsername = com.example.demo.util.SecurityUtil.getCurrentUsername()
                    .orElseThrow(() -> new com.example.demo.exception.BusinessException(401, "未登录"));

            assignmentSubmissionService.submitAssignment(submissionId, studentUsername);

            return ApiResponse.success(null, "提交成功");
        } catch (com.example.demo.exception.BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("提交作业失败", e);
            return ApiResponse.error(500, "提交失败: " + e.getMessage());
        }
    }

    /**
     * 查询学生的作业列表
     *
     * @param courseId 课程ID（可选）
     * @param experimentId 实验ID（可选）
     * @return 作业列表
     */
    @GetMapping
    @RequireRole(value = UserRole.STUDENT)
    public ApiResponse<List<AssignmentSubmissionResponse>> getAssignments(
            @RequestParam(value = "courseId", required = false) String courseId,
            @RequestParam(value = "experimentId", required = false) String experimentId) {
        try {
            String studentUsername = com.example.demo.util.SecurityUtil.getCurrentUsername()
                    .orElseThrow(() -> new com.example.demo.exception.BusinessException(401, "未登录"));

            List<AssignmentSubmissionResponse> submissions = assignmentSubmissionService.getStudentSubmissions(
                    studentUsername, courseId, experimentId
            );

            return ApiResponse.success(submissions, "查询成功");
        } catch (Exception e) {
            log.error("查询作业列表失败", e);
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID查询作业详情
     *
     * @param submissionId 作业ID
     * @return 作业详情
     */
    @GetMapping("/{submissionId}")
    @RequireRole(value = UserRole.STUDENT)
    public ApiResponse<AssignmentSubmissionResponse> getAssignmentById(@PathVariable("submissionId") Long submissionId) {
        try {
            AssignmentSubmissionResponse submission = assignmentSubmissionService.getSubmissionById(submissionId);
            return ApiResponse.success(submission);
        } catch (com.example.demo.exception.BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("查询作业详情失败", e);
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 删除作业（只能删除草稿状态的作业）
     *
     * @param submissionId 作业ID
     * @return 是否删除成功
     */
    @DeleteMapping("/{submissionId}")
    @RequireRole(value = UserRole.STUDENT)
    public ApiResponse<Void> deleteAssignment(@PathVariable("submissionId") Long submissionId) {
        try {
            String studentUsername = com.example.demo.util.SecurityUtil.getCurrentUsername()
                    .orElseThrow(() -> new com.example.demo.exception.BusinessException(401, "未登录"));

            assignmentSubmissionService.deleteAssignment(submissionId, studentUsername);

            return ApiResponse.success(null, "删除成功");
        } catch (com.example.demo.exception.BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("删除作业失败", e);
            return ApiResponse.error(500, "删除失败: " + e.getMessage());
        }
    }
}
