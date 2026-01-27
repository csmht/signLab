package com.example.demo.controller.student;

import com.example.demo.annotation.RequireRole;
import com.example.demo.enums.UserRole;
import com.example.demo.pojo.request.CompleteDataCollectionProcedureRequest;
import com.example.demo.pojo.request.CompleteTopicProcedureRequest;
import com.example.demo.pojo.response.ApiResponse;
import com.example.demo.pojo.response.ProcedureSubmissionResponse;
import com.example.demo.service.ProcedureSubmissionService;
import com.example.demo.service.StudentExperimentalProcedureService;
import com.example.demo.service.StudentProcedureCompletionService;
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
    private final StudentExperimentalProcedureService studentExperimentalProcedureService;
    private final StudentProcedureCompletionService studentProcedureCompletionService;

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
     * 标记视频已观看
     * 步骤类型：type=1（观看视频）
     * 学生观看完视频后调用此接口标记为已观看
     *
     * @param procedureId 实验步骤ID
     * @param classCode   班级编号
     * @return 是否标记成功
     */
    @PostMapping("/video/{procedureId}/viewed")
    @RequireRole(value = UserRole.STUDENT)
    public ApiResponse<Void> markVideoAsViewed(
            @PathVariable("procedureId") Long procedureId,
            @RequestParam("classCode") String classCode) {
        try {
            String studentUsername = com.example.demo.util.SecurityUtil.getCurrentUsername()
                    .orElseThrow(() -> new com.example.demo.exception.BusinessException(401, "未登录"));

            studentExperimentalProcedureService.markVideoAsViewed(
                    studentUsername, classCode, procedureId);

            return ApiResponse.success(null, "标记成功");
        } catch (com.example.demo.exception.BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("标记视频观看失败", e);
            return ApiResponse.error(500, "标记失败: " + e.getMessage());
        }
    }

    /**
     * 完成题库练习步骤
     * 步骤类型：type=3（题库答题）
     * 学生完成题库练习后调用此接口提交答案
     *
     * @param request 完成题库练习请求
     * @return 是否提交成功
     */
    @PostMapping("/topic/complete")
    @RequireRole(value = UserRole.STUDENT)
    public ApiResponse<Void> completeTopicProcedure(@RequestBody CompleteTopicProcedureRequest request) {
        try {
            String studentUsername = com.example.demo.util.SecurityUtil.getCurrentUsername()
                    .orElseThrow(() -> new com.example.demo.exception.BusinessException(401, "未登录"));

            studentProcedureCompletionService.completeTopicProcedure(
                    studentUsername,
                    request.getClassCode(),
                    request.getProcedureId(),
                    request.getAnswers()
            );

            return ApiResponse.success(null, "提交成功");
        } catch (com.example.demo.exception.BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("完成题库练习失败", e);
            return ApiResponse.error(500, "提交失败: " + e.getMessage());
        }
    }

    /**
     * 完成数据收集步骤
     * 步骤类型：type=2（数据收集）
     * 学生完成数据收集后调用此接口提交数据和文件
     *
     * @param procedureId  实验步骤ID
     * @param classCode    班级编号
     * @param fillBlankAnswersJson 填空类型答案（JSON字符串）
     * @param tableCellAnswersJson 表格类型答案（JSON字符串）
     * @param photos       照片文件列表
     * @param documents    文档文件列表
     * @return 是否提交成功
     */
    @PostMapping("/data-collection/complete")
    @RequireRole(value = UserRole.STUDENT)
    public ApiResponse<Void> completeDataCollectionProcedure(
            @RequestParam("procedureId") Long procedureId,
            @RequestParam("classCode") String classCode,
            @RequestParam(value = "fillBlankAnswers", required = false) String fillBlankAnswersJson,
            @RequestParam(value = "tableCellAnswers", required = false) String tableCellAnswersJson,
            @RequestParam(value = "photos", required = false) List<MultipartFile> photos,
            @RequestParam(value = "documents", required = false) List<MultipartFile> documents) {
        try {
            String studentUsername = com.example.demo.util.SecurityUtil.getCurrentUsername()
                    .orElseThrow(() -> new com.example.demo.exception.BusinessException(401, "未登录"));

            // 解析JSON字符串为Map
            java.util.Map<String, String> fillBlankAnswers = null;
            java.util.Map<String, String> tableCellAnswers = null;

            if (fillBlankAnswersJson != null && !fillBlankAnswersJson.trim().isEmpty()) {
                com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                fillBlankAnswers = objectMapper.readValue(fillBlankAnswersJson,
                    new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, String>>() {});
            }

            if (tableCellAnswersJson != null && !tableCellAnswersJson.trim().isEmpty()) {
                com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                tableCellAnswers = objectMapper.readValue(tableCellAnswersJson,
                    new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, String>>() {});
            }

            studentProcedureCompletionService.completeDataCollectionProcedure(
                    studentUsername,
                    classCode,
                    procedureId,
                    fillBlankAnswers,
                    tableCellAnswers,
                    photos,
                    documents
            );

            return ApiResponse.success(null, "提交成功");
        } catch (com.example.demo.exception.BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("完成数据收集失败", e);
            return ApiResponse.error(500, "提交失败: " + e.getMessage());
        }
    }
}
