package com.example.demo.controller.teacher;

import com.example.demo.annotation.RequireRole;
import com.example.demo.enums.UserRole;
import com.example.demo.pojo.response.ApiResponse;
import com.example.demo.pojo.response.ClassExperimentStatisticsResponse;
import com.example.demo.pojo.response.StudentProcedureCompletionResponse;
import com.example.demo.pojo.response.StudentProcedureDetailCompletionResponse;
import com.example.demo.service.TeacherStudentProcedureQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 教师查询学生步骤完成情况控制器
 * 提供教师查询学生实验步骤完成情况的接口
 */
@RequestMapping("/api/teacher/students")
@RestController
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class TeacherStudentProcedureQueryController {

    private final TeacherStudentProcedureQueryService teacherStudentProcedureQueryService;

    /**
     * 查询学生在指定班级实验中的步骤完成情况
     *
     * @param studentUsername 学生用户名
     * @param classCode       班级编号
     * @param experimentId    实验ID
     * @return 步骤完成情况
     */
    @GetMapping("/{studentUsername}/experiments/{experimentId}/procedures")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<StudentProcedureCompletionResponse> getStudentProcedureCompletion(
            @PathVariable("studentUsername") String studentUsername,
            @PathVariable("experimentId") Long experimentId,
            @RequestParam("classCode") String classCode) {
        try {
            StudentProcedureCompletionResponse response =
                    teacherStudentProcedureQueryService.getStudentProcedureCompletion(
                            studentUsername, classCode, experimentId);
            return ApiResponse.success(response, "查询成功");
        } catch (com.example.demo.exception.BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("查询学生实验步骤完成情况失败", e);
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 查询学生在指定步骤的完成详情
     *
     * @param studentUsername 学生用户名
     * @param procedureId     步骤ID
     * @param classCode       班级编号
     * @return 步骤完成详情
     */
    @GetMapping("/{studentUsername}/procedures/{procedureId}")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<StudentProcedureDetailCompletionResponse> getStudentProcedureDetailCompletion(
            @PathVariable("studentUsername") String studentUsername,
            @PathVariable("procedureId") Long procedureId,
            @RequestParam("classCode") String classCode) {
        try {
            StudentProcedureDetailCompletionResponse response =
                    teacherStudentProcedureQueryService.getStudentProcedureDetailCompletion(
                            studentUsername, classCode, procedureId);
            return ApiResponse.success(response, "查询成功");
        } catch (com.example.demo.exception.BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("查询学生步骤完成详情失败", e);
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 查询指定学生已提交的步骤详情（带答案）
     *
     * @param studentUsername 学生用户名
     * @param courseId 课程ID
     * @param experimentId 实验ID
     * @param procedureId 步骤ID
     * @return 步骤详情（带答案）
     */
    @GetMapping("/{studentUsername}/procedures/{procedureId}/completed")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<Object> getStudentCompletedProcedureDetail(
            @PathVariable("studentUsername") String studentUsername,
            @RequestParam("courseId") String courseId,
            @RequestParam("experimentId") Long experimentId,
            @PathVariable("procedureId") Long procedureId) {
        try {
            com.example.demo.pojo.response.StudentProcedureDetailWithAnswerResponse response =
                    teacherStudentProcedureQueryService.getStudentCompletedProcedureDetail(
                            studentUsername, courseId, experimentId, procedureId);

            return ApiResponse.success(response, "查询成功");
        } catch (com.example.demo.exception.BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("查询学生已提交步骤详情失败", e);
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 查询指定学生未提交的步骤详情
     *
     * @param studentUsername 学生用户名
     * @param courseId 课程ID
     * @param experimentId 实验ID
     * @param procedureId 步骤ID
     * @return 步骤详情（不含答案）
     */
    @GetMapping("/{studentUsername}/procedures/{procedureId}/uncompleted")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<Object> getStudentUncompletedProcedureDetail(
            @PathVariable("studentUsername") String studentUsername,
            @RequestParam("courseId") String courseId,
            @RequestParam("experimentId") Long experimentId,
            @PathVariable("procedureId") Long procedureId) {
        try {
            com.example.demo.pojo.response.StudentProcedureDetailWithoutAnswerResponse response =
                    teacherStudentProcedureQueryService.getStudentUncompletedProcedureDetail(
                            studentUsername, courseId, experimentId, procedureId);

            return ApiResponse.success(response, "查询成功");
        } catch (com.example.demo.exception.BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("查询学生未提交步骤详情失败", e);
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 查询班级实验完成统计
     *
     * @param classCode    班级编号
     * @param experimentId 实验ID
     * @return 班级实验完成统计
     */
    @GetMapping("/classes/{classCode}/experiments/{experimentId}/statistics")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<ClassExperimentStatisticsResponse> getClassExperimentStatistics(
            @PathVariable("classCode") String classCode,
            @PathVariable("experimentId") Long experimentId) {
        try {
            ClassExperimentStatisticsResponse response =
                    teacherStudentProcedureQueryService.getClassExperimentStatistics(
                            classCode, experimentId);
            return ApiResponse.success(response, "查询成功");
        } catch (com.example.demo.exception.BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("查询班级实验完成统计失败", e);
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }
}
