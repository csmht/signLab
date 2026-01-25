package com.example.demo.controller;

import com.example.demo.annotation.RequireRole;
import com.example.demo.enums.UserRole;
import com.example.demo.pojo.dto.ApiResponse;
import com.example.demo.pojo.entity.Course;
import com.example.demo.service.CourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 课程管理控制器
 * 提供课程的查询、创建、更新、删除等接口
 */
@RequestMapping("/api/course")
@RestController
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CourseController {

    private final CourseService courseService;

    /**
     * 根据ID查询课程
     * @param id 课程ID
     * @return 课程信息
     */
    @GetMapping("/{id}")
    public ApiResponse<Course> getById(@PathVariable Long id) {
        try {
            Course course = courseService.getById(id);
            if (course == null) {
                return ApiResponse.error(404, "课程不存在");
            }
            return ApiResponse.success(course);
        } catch (Exception e) {
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据课程代码查询课程
     * @param courseCode 课程代码
     * @return 课程信息
     */
    @GetMapping("/code/{courseCode}")
    public ApiResponse<Course> getByCourseCode(@PathVariable String courseCode) {
        try {
            Course course = courseService.getByCourseCode(courseCode);
            if (course == null) {
                return ApiResponse.error(404, "课程不存在");
            }
            return ApiResponse.success(course);
        } catch (Exception e) {
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 创建课程
     * 仅管理员可调用
     * @param course 课程信息
     * @return 创建结果
     */
    @RequireRole(value = UserRole.ADMIN)
    @PostMapping
    public ApiResponse<Course> create(@RequestBody Course course) {
        try {
            boolean success = courseService.save(course);
            if (success) {
                return ApiResponse.success(course, "课程创建成功");
            } else {
                return ApiResponse.error(500, "课程创建失败");
            }
        } catch (Exception e) {
            return ApiResponse.error(500, "创建失败: " + e.getMessage());
        }
    }

    /**
     * 更新课程信息
     * 仅管理员可调用
     * @param id 课程ID
     * @param course 课程信息
     * @return 更新结果
     */
    @RequireRole(value = UserRole.ADMIN)
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody Course course) {
        try {
            course.setId(id);
            boolean success = courseService.updateById(course);
            if (success) {
                return ApiResponse.success(null, "课程更新成功");
            } else {
                return ApiResponse.error(404, "课程不存在");
            }
        } catch (Exception e) {
            return ApiResponse.error(500, "更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除课程
     * 仅管理员可调用
     * @param id 课程ID
     * @return 删除结果
     */
    @RequireRole(value = UserRole.ADMIN)
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        try {
            boolean success = courseService.removeById(id);
            if (success) {
                return ApiResponse.success(null, "课程删除成功");
            } else {
                return ApiResponse.error(404, "课程不存在");
            }
        } catch (Exception e) {
            return ApiResponse.error(500, "删除失败: " + e.getMessage());
        }
    }
}