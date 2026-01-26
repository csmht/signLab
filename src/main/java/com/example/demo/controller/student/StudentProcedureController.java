package com.example.demo.controller.student;

import com.example.demo.annotation.RequireRole;
import com.example.demo.enums.UserRole;
import com.example.demo.pojo.response.ApiResponse;
import com.example.demo.pojo.response.ProcedureSubmissionResponse;
import com.example.demo.service.ProcedureSubmissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 学生实验步骤提交控制器
 * 提供学生上传、提交、查询实验步骤的接口
 */
@RequestMapping("/api/student/procedure-submissions")
@RestController
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class StudentProcedureController {

    private final ProcedureSubmissionService procedureSubmissionService;

    /**
     * 上传步骤文件
     *
     * @param courseId 课程ID
     * @param experimentId 实验ID
     * @param submissionType 提交类型（实验报告、数据文件等）
     * @param file 步骤文件
     * @return 上传后的步骤信息
     */
    @PostMapping("/upload")
    @RequireRole(value = UserRole.STUDENT)
    public ApiResponse<ProcedureSubmissionResponse> uploadProcedureSubmission(
            @RequestParam("courseId") String courseId,
            @RequestParam("experimentId") String experimentId,
            @RequestParam("submissionType") String submissionType,
            @RequestParam("file") MultipartFile file) {
        try {
            // 获取当前登录学生用户名
            String studentUsername = com.example.demo.util.SecurityUtil.getCurrentUsername()
                    .orElseThrow(() -> new com.example.demo.exception.BusinessException(401, "未登录"));

            ProcedureSubmissionResponse response = procedureSubmissionService.uploadProcedureSubmission(
                    courseId, experimentId, studentUsername, submissionType, file
            );

            return ApiResponse.success(response, "上传成功");
        } catch (com.example.demo.exception.BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("上传步骤文件失败", e);
            return ApiResponse.error(500, "上传失败: " + e.getMessage());
        }
    }

    /**
     * 提交步骤（将草稿状态改为已提交）
     *
     * @param submissionId 步骤提交ID
     * @return 是否提交成功
     */
    @PostMapping("/{submissionId}/submit")
    @RequireRole(value = UserRole.STUDENT)
    public ApiResponse<Void> submitProcedure(@PathVariable("submissionId") Long submissionId) {
        try {
            String studentUsername = com.example.demo.util.SecurityUtil.getCurrentUsername()
                    .orElseThrow(() -> new com.example.demo.exception.BusinessException(401, "未登录"));

            procedureSubmissionService.submitProcedure(submissionId, studentUsername);

            return ApiResponse.success(null, "提交成功");
        } catch (com.example.demo.exception.BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("提交步骤失败", e);
            return ApiResponse.error(500, "提交失败: " + e.getMessage());
        }
    }

    /**
     * 查询学生的步骤提交列表
     *
     * @param courseId 课程ID（可选）
     * @param experimentId 实验ID（可选）
     * @return 步骤列表
     */
    @GetMapping
    @RequireRole(value = UserRole.STUDENT)
    public ApiResponse<List<ProcedureSubmissionResponse>> getProcedureSubmissions(
            @RequestParam(value = "courseId", required = false) String courseId,
            @RequestParam(value = "experimentId", required = false) String experimentId) {
        try {
            String studentUsername = com.example.demo.util.SecurityUtil.getCurrentUsername()
                    .orElseThrow(() -> new com.example.demo.exception.BusinessException(401, "未登录"));

            List<ProcedureSubmissionResponse> submissions = procedureSubmissionService.getStudentSubmissions(
                    studentUsername, courseId, experimentId
            );

            return ApiResponse.success(submissions, "查询成功");
        } catch (Exception e) {
            log.error("查询步骤列表失败", e);
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 按课程查询步骤提交
     *
     * @param courseId 课程ID
     * @return 步骤列表
     */
    @GetMapping("/course/{courseId}")
    @RequireRole(value = UserRole.STUDENT)
    public ApiResponse<List<ProcedureSubmissionResponse>> getProcedureSubmissionsByCourse(
            @PathVariable("courseId") String courseId) {
        try {
            String studentUsername = com.example.demo.util.SecurityUtil.getCurrentUsername()
                    .orElseThrow(() -> new com.example.demo.exception.BusinessException(401, "未登录"));

            List<ProcedureSubmissionResponse> submissions = procedureSubmissionService.getStudentSubmissions(
                    studentUsername, courseId, null
            );

            return ApiResponse.success(submissions, "查询成功");
        } catch (Exception e) {
            log.error("查询步骤提交失败", e);
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID查询步骤详情
     *
     * @param submissionId 步骤提交ID
     * @return 步骤详情
     */
    @GetMapping("/{submissionId}")
    @RequireRole(value = UserRole.STUDENT)
    public ApiResponse<ProcedureSubmissionResponse> getProcedureSubmissionById(@PathVariable("submissionId") Long submissionId) {
        try {
            ProcedureSubmissionResponse submission = procedureSubmissionService.getSubmissionById(submissionId);
            return ApiResponse.success(submission);
        } catch (com.example.demo.exception.BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("查询步骤详情失败", e);
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 删除步骤提交
     *
     * @param submissionId 步骤提交ID
     * @return 是否删除成功
     */
    @DeleteMapping("/{submissionId}")
    @RequireRole(value = UserRole.STUDENT)
    public ApiResponse<Void> deleteProcedure(@PathVariable("submissionId") Long submissionId) {
        try {
            String studentUsername = com.example.demo.util.SecurityUtil.getCurrentUsername()
                    .orElseThrow(() -> new com.example.demo.exception.BusinessException(401, "未登录"));

            procedureSubmissionService.deleteProcedure(submissionId, studentUsername);

            return ApiResponse.success(null, "删除成功");
        } catch (com.example.demo.exception.BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("删除步骤提交失败", e);
            return ApiResponse.error(500, "删除失败: " + e.getMessage());
        }
    }
}
