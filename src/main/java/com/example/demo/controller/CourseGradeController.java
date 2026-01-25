package com.example.demo.controller;

import com.example.demo.annotation.RequireRole;
import com.example.demo.enums.UserRole;
import com.example.demo.pojo.dto.ApiResponse;
import com.example.demo.pojo.entity.CourseGrade;
import com.example.demo.service.CourseGradeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 课程成绩管理控制器
 * 提供课程成绩的查询、创建、更新、删除等接口
 */
@RequestMapping("/api/course-grade")
@RestController
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CourseGradeController {

    private final CourseGradeService courseGradeService;

    /**
     * 根据ID查询课程成绩
     * @param id 课程成绩ID
     * @return 课程成绩信息
     */
    @GetMapping("/{id}")
    public ApiResponse<CourseGrade> getById(@PathVariable Long id) {
        try {
            CourseGrade courseGrade = courseGradeService.getById(id);
            if (courseGrade == null) {
                return ApiResponse.error(404, "课程成绩不存在");
            }
            return ApiResponse.success(courseGrade);
        } catch (Exception e) {
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据学生用户名查询课程成绩
     * @param studentUsername 学生用户名
     * @return 课程成绩信息
     */
    @GetMapping("/student/{studentUsername}")
    public ApiResponse<CourseGrade> getByStudentUsername(@PathVariable String studentUsername) {
        try {
            CourseGrade courseGrade = courseGradeService.getByStudentUsername(studentUsername);
            if (courseGrade == null) {
                return ApiResponse.error(404, "课程成绩不存在");
            }
            return ApiResponse.success(courseGrade);
        } catch (Exception e) {
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 创建课程成绩
     * 仅管理员可调用
     * @param courseGrade 课程成绩信息
     * @return 创建结果
     */
    @RequireRole(value = UserRole.ADMIN)
    @PostMapping
    public ApiResponse<CourseGrade> create(@RequestBody CourseGrade courseGrade) {
        try {
            boolean success = courseGradeService.save(courseGrade);
            if (success) {
                return ApiResponse.success(courseGrade, "课程成绩创建成功");
            } else {
                return ApiResponse.error(500, "课程成绩创建失败");
            }
        } catch (Exception e) {
            return ApiResponse.error(500, "创建失败: " + e.getMessage());
        }
    }

    /**
     * 更新课程成绩信息
     * 仅管理员可调用
     * @param id 课程成绩ID
     * @param courseGrade 课程成绩信息
     * @return 更新结果
     */
    @RequireRole(value = UserRole.ADMIN)
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody CourseGrade courseGrade) {
        try {
            courseGrade.setId(id);
            boolean success = courseGradeService.updateById(courseGrade);
            if (success) {
                return ApiResponse.success(null, "课程成绩更新成功");
            } else {
                return ApiResponse.error(404, "课程成绩不存在");
            }
        } catch (Exception e) {
            return ApiResponse.error(500, "更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除课程成绩
     * 仅管理员可调用
     * @param id 课程成绩ID
     * @return 删除结果
     */
    @RequireRole(value = UserRole.ADMIN)
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        try {
            boolean success = courseGradeService.removeById(id);
            if (success) {
                return ApiResponse.success(null, "课程成绩删除成功");
            } else {
                return ApiResponse.error(404, "课程成绩不存在");
            }
        } catch (Exception e) {
            return ApiResponse.error(500, "删除失败: " + e.getMessage());
        }
    }
}