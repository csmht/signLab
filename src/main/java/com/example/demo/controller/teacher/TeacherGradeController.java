package com.example.demo.controller.teacher;

import com.example.demo.annotation.RequireRole;
import com.example.demo.enums.UserRole;
import com.example.demo.pojo.response.ApiResponse;
import com.example.demo.pojo.response.CourseGradeResponse;
import com.example.demo.service.CourseGradeService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 教师成绩管理控制器
 * 提供教师管理课程成绩的接口
 */
@RequestMapping("/api/teacher/grades")
@RestController
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class TeacherGradeController {

    private final CourseGradeService courseGradeService;

    /**
     * 查询课程的所有学生成绩
     *
     * @param courseId 课程ID
     * @param semester 学期（可选）
     * @return 成绩列表
     */
    @GetMapping("/course/{courseId}")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<List<CourseGradeResponse>> getCourseGrades(
            @PathVariable("courseId") String courseId,
            @RequestParam(value = "semester", required = false) String semester) {
        try {
            List<CourseGradeResponse> grades = courseGradeService.getCourseGrades(courseId, semester);

            return ApiResponse.success(grades, "查询成功");
        } catch (Exception e) {
            log.error("查询课程成绩失败", e);
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID查询成绩详情
     *
     * @param gradeId 成绩ID
     * @return 成绩详情
     */
    @GetMapping("/{gradeId}")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<CourseGradeResponse> getGradeById(@PathVariable("gradeId") Long gradeId) {
        try {
            CourseGradeResponse grade = courseGradeService.getGradeById(gradeId);
            return ApiResponse.success(grade);
        } catch (com.example.demo.exception.BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("查询成绩详情失败", e);
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 创建或更新课程成绩
     *
     * @param request 成绩请求
     * @return 成绩信息
     */
    @PostMapping
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<CourseGradeResponse> saveGrade(@RequestBody SaveGradeRequest request) {
        try {
            String teacherUsername = com.example.demo.util.SecurityUtil.getCurrentUsername()
                    .orElseThrow(() -> new com.example.demo.exception.BusinessException(401, "未登录"));

            CourseGradeResponse grade = courseGradeService.saveGrade(
                    request.getCourseId(),
                    request.getStudentUsername(),
                    teacherUsername,
                    request.getGrade(),
                    request.getGradeNumeric(),
                    request.getGradeType(),
                    request.getTeacherComment(),
                    request.getSemester()
            );

            return ApiResponse.success(grade, "保存成功");
        } catch (com.example.demo.exception.BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("保存成绩失败", e);
            return ApiResponse.error(500, "保存失败: " + e.getMessage());
        }
    }

    /**
     * 审核成绩
     *
     * @param gradeId 成绩ID
     * @return 是否审核成功
     */
    @PostMapping("/{gradeId}/approve")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<Void> approveGrade(@PathVariable("gradeId") Long gradeId) {
        try {
            String approvedBy = com.example.demo.util.SecurityUtil.getCurrentUsername()
                    .orElseThrow(() -> new com.example.demo.exception.BusinessException(401, "未登录"));

            courseGradeService.approveGrade(gradeId, approvedBy);

            return ApiResponse.success(null, "审核成功");
        } catch (com.example.demo.exception.BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("审核成绩失败", e);
            return ApiResponse.error(500, "审核失败: " + e.getMessage());
        }
    }

    /**
     * 删除成绩
     *
     * @param gradeId 成绩ID
     * @return 是否删除成功
     */
    @DeleteMapping("/{gradeId}")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<Void> deleteGrade(@PathVariable("gradeId") Long gradeId) {
        try {
            courseGradeService.deleteGrade(gradeId);

            return ApiResponse.success(null, "删除成功");
        } catch (com.example.demo.exception.BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("删除成绩失败", e);
            return ApiResponse.error(500, "删除失败: " + e.getMessage());
        }
    }

    /**
     * 保存成绩请求
     */
    @Data
    public static class SaveGradeRequest {
        /**
         * 课程ID
         */
        private String courseId;

        /**
         * 学生学号
         */
        private String studentUsername;

        /**
         * 成绩
         */
        private String grade;

        /**
         * 数字成绩
         */
        private BigDecimal gradeNumeric;

        /**
         * 成绩类型
         */
        private String gradeType;

        /**
         * 教师评语
         */
        private String teacherComment;

        /**
         * 学期
         */
        private String semester;
    }
}
