package com.example.demo.controller.teacher;

import com.example.demo.annotation.RequireRole;
import com.example.demo.enums.UserRole;
import com.example.demo.exception.BusinessException;
import com.example.demo.pojo.request.CourseQueryRequest;
import com.example.demo.pojo.request.CreateCourseRequest;
import com.example.demo.pojo.request.UpdateCourseRequest;
import com.example.demo.pojo.response.ApiResponse;
import com.example.demo.pojo.response.CourseResponse;
import com.example.demo.pojo.response.PageResponse;
import com.example.demo.service.CourseService;
import com.example.demo.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 教师课程管理控制器
 * 提供课程的查询、创建、更新、删除等接口
 */
@RequestMapping("/api/teacher/courses")
@RestController
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CourseController {

    private final CourseService courseService;

    /**
     * 分页查询课程列表
     *
     * @param request 查询请求
     * @return 课程列表
     */
    @PostMapping("/query")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<PageResponse<CourseResponse>> queryCourses(@RequestBody CourseQueryRequest request) {
        try {
            // 如果设置只查询我的课程
            if (request.getMyOnly() != null && request.getMyOnly()) {
                String teacherUsername = SecurityUtil.getCurrentUsername()
                        .orElseThrow(() -> new BusinessException(401, "未登录"));

                PageResponse<CourseResponse> response = courseService.queryMyCourses(teacherUsername, request);
                return ApiResponse.success(response);
            }

            // 查询所有课程
            PageResponse<CourseResponse> response = courseService.queryCourses(request);
            return ApiResponse.success(response);
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("查询课程列表失败", e);
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID查询课程
     *
     * @param id 课程ID
     * @return 课程信息
     */
    @GetMapping("/{id}")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<CourseResponse> getCourseById(@PathVariable Long id) {
        try {
            CourseResponse response = courseService.getCourseById(id);
            return ApiResponse.success(response);
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("查询课程详情失败", e);
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 创建课程
     *
     * @param request 创建课程请求
     * @return 课程信息
     */
    @PostMapping
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<CourseResponse> createCourse(@RequestBody CreateCourseRequest request) {
        try {
            String teacherUsername = SecurityUtil.getCurrentUsername()
                    .orElseThrow(() -> new BusinessException(401, "未登录"));

            CourseResponse response = courseService.createCourse(request, teacherUsername);
            return ApiResponse.success(response, "创建课程成功");
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("创建课程失败", e);
            return ApiResponse.error(500, "创建失败: " + e.getMessage());
        }
    }

    /**
     * 更新课程
     *
     * @param id      课程ID
     * @param request 更新课程请求
     * @return 课程信息
     */
    @PutMapping("/{id}")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<CourseResponse> updateCourse(
            @PathVariable Long id,
            @RequestBody UpdateCourseRequest request) {
        try {
            String teacherUsername = SecurityUtil.getCurrentUsername()
                    .orElseThrow(() -> new BusinessException(401, "未登录"));

            CourseResponse response = courseService.updateCourse(id, request, teacherUsername);
            return ApiResponse.success(response, "更新课程成功");
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("更新课程失败", e);
            return ApiResponse.error(500, "更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除课程
     *
     * @param id 课程ID
     * @return 是否删除成功
     */
    @DeleteMapping("/{id}")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<Void> deleteCourse(@PathVariable Long id) {
        try {
            String teacherUsername = SecurityUtil.getCurrentUsername()
                    .orElseThrow(() -> new BusinessException(401, "未登录"));

            boolean deleted = courseService.deleteCourse(id, teacherUsername);
            if (deleted) {
                return ApiResponse.success(null, "删除课程成功");
            } else {
                return ApiResponse.error(500, "删除课程失败");
            }
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("删除课程失败", e);
            return ApiResponse.error(500, "删除失败: " + e.getMessage());
        }
    }
}
