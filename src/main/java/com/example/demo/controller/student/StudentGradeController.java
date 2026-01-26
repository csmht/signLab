package com.example.demo.controller.student;

import com.example.demo.annotation.RequireRole;
import com.example.demo.enums.UserRole;
import com.example.demo.pojo.response.ApiResponse;
import com.example.demo.pojo.response.CourseGradeResponse;
import com.example.demo.service.CourseGradeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学生成绩查询控制器
 * 提供学生查询自己的课程成绩的接口
 */
@RequestMapping("/api/student/grades")
@RestController
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class StudentGradeController {

    private final CourseGradeService courseGradeService;

    /**
     * 查询学生的所有课程成绩
     *
     * @param semester 学期（可选）
     * @return 成绩列表
     */
    @GetMapping
    @RequireRole(value = UserRole.STUDENT)
    public ApiResponse<List<CourseGradeResponse>> getGrades(
            @RequestParam(value = "semester", required = false) String semester) {
        try {
            String studentUsername = com.example.demo.util.SecurityUtil.getCurrentUsername()
                    .orElseThrow(() -> new com.example.demo.exception.BusinessException(401, "未登录"));

            List<CourseGradeResponse> grades = courseGradeService.getStudentGrades(studentUsername, semester);

            return ApiResponse.success(grades, "查询成功");
        } catch (Exception e) {
            log.error("查询成绩失败", e);
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
    @RequireRole(value = UserRole.STUDENT)
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
}
