package com.example.demo.controller.teacher;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.annotation.RequireRole;
import com.example.demo.enums.UserRole;
import com.example.demo.pojo.entity.Experiment;
import com.example.demo.pojo.request.teacher.CreateExperimentRequest;
import com.example.demo.pojo.request.teacher.UpdateExperimentRequest;
import com.example.demo.pojo.response.ApiResponse;
import com.example.demo.pojo.response.ExperimentResponse;
import com.example.demo.service.CourseService;
import com.example.demo.service.ExperimentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 教师实验管理控制器
 * 提供教师管理实验的接口
 */
@RequestMapping("/api/teacher/experiments")
@RestController
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class TeacherExperimentController {

    private final ExperimentService experimentService;
    private final CourseService courseService;

    /**
     * 创建实验
     *
     * @param request 创建实验请求
     * @return 实验ID
     */
    @PostMapping
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<Long> createExperiment(@RequestBody CreateExperimentRequest request) {
        try {
            log.info("创建实验，课程ID: {}, 实验名称: {}", request.getCourseId(), request.getExperimentName());

            // 验证必填字段
            if (request.getCourseId() == null || request.getCourseId().trim().isEmpty()) {
                return ApiResponse.error(400, "课程ID不能为空");
            }
            if (request.getExperimentName() == null || request.getExperimentName().trim().isEmpty()) {
                return ApiResponse.error(400, "实验名称不能为空");
            }
            if (request.getEndTime() == null) {
                return ApiResponse.error(400, "实验结束填写时间不能为空");
            }

            // 创建实验实体
            Experiment experiment = new Experiment();
            experiment.setCourseId(request.getCourseId());
            experiment.setExperimentName(request.getExperimentName());
            experiment.setPercentage(request.getPercentage() != null ? request.getPercentage() : 0);
            experiment.setEndTime(request.getEndTime());
            experiment.setIsDeleted(false);

            // 保存实验
            experimentService.save(experiment);

            log.info("实验创建成功，实验ID: {}", experiment.getId());
            return ApiResponse.success(experiment.getId(), "创建成功");
        } catch (Exception e) {
            log.error("创建实验失败", e);
            return ApiResponse.error(500, "创建失败: " + e.getMessage());
        }
    }

    /**
     * 修改实验
     *
     * @param request 修改实验请求
     * @return 是否修改成功
     */
    @PutMapping
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<Void> updateExperiment(@RequestBody UpdateExperimentRequest request) {
        try {
            log.info("修改实验，实验ID: {}", request.getId());

            // 查询实验是否存在
            Experiment experiment = experimentService.getById(request.getId());
            if (experiment == null) {
                return ApiResponse.error(404, "实验不存在");
            }

            // 验证时间
            if (request.getEndTime() != null && experiment.getEndTime().isAfter(request.getEndTime())) {
                // 这里可以添加业务逻辑，比如检查是否有学生已经完成实验等
            }

            // 更新实验信息
            if (request.getExperimentName() != null) {
                experiment.setExperimentName(request.getExperimentName());
            }
            if (request.getPercentage() != null) {
                experiment.setPercentage(request.getPercentage());
            }
            if (request.getEndTime() != null) {
                experiment.setEndTime(request.getEndTime());
            }

            experimentService.updateById(experiment);

            log.info("实验修改成功，实验ID: {}", request.getId());
            return ApiResponse.success(null, "修改成功");
        } catch (Exception e) {
            log.error("修改实验失败", e);
            return ApiResponse.error(500, "修改失败: " + e.getMessage());
        }
    }

    /**
     * 删除实验
     *
     * @param experimentId 实验ID
     * @return 是否删除成功
     */
    @DeleteMapping("/{experimentId}")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<Void> deleteExperiment(@PathVariable("experimentId") Long experimentId) {
        try {
            log.info("删除实验，实验ID: {}", experimentId);

            // 查询实验是否存在
            Experiment experiment = experimentService.getById(experimentId);
            if (experiment == null) {
                return ApiResponse.error(404, "实验不存在");
            }

            // 软删除实验
            experiment.setIsDeleted(true);
            experimentService.updateById(experiment);

            log.info("实验删除成功，实验ID: {}", experimentId);
            return ApiResponse.success(null, "删除成功");
        } catch (Exception e) {
            log.error("删除实验失败", e);
            return ApiResponse.error(500, "删除失败: " + e.getMessage());
        }
    }

    /**
     * 查询实验详情
     *
     * @param experimentId 实验ID
     * @return 实验详情
     */
    @GetMapping("/{experimentId}")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<ExperimentResponse> getExperiment(@PathVariable("experimentId") Long experimentId) {
        try {
            Experiment experiment = experimentService.getById(experimentId);
            if (experiment == null) {
                return ApiResponse.error(404, "实验不存在");
            }

            ExperimentResponse response = new ExperimentResponse();
            BeanUtils.copyProperties(experiment, response);

            // 设置课程名称
            if (experiment.getCourseId() != null) {
                var course = courseService.getByCourseCode(experiment.getCourseId());
                if (course != null) {
                    response.setCourseName(course.getCourseName());
                }
            }

            return ApiResponse.success(response, "查询成功");
        } catch (Exception e) {
            log.error("查询实验详情失败", e);
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据课程ID查询实验列表
     *
     * @param courseId 课程ID
     * @return 实验列表
     */
    @GetMapping("/course/{courseId}")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<List<ExperimentResponse>> getExperimentsByCourseId(@PathVariable("courseId") String courseId) {
        try {
            QueryWrapper<Experiment> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("course_id", courseId);
            queryWrapper.eq("is_deleted", false);
            queryWrapper.orderByDesc("created_time");

            List<Experiment> experiments = experimentService.list(queryWrapper);

            // 只查询一次课程信息
            String courseName = null;
            if (!experiments.isEmpty() && experiments.get(0).getCourseId() != null) {
                var course = courseService.getByCourseCode(experiments.get(0).getCourseId());
                if (course != null) {
                    courseName = course.getCourseName();
                }
            }

            String finalCourseName = courseName;
            List<ExperimentResponse> responses = experiments.stream().map(experiment -> {
                ExperimentResponse response = new ExperimentResponse();
                BeanUtils.copyProperties(experiment, response);
                response.setCourseName(finalCourseName);
                return response;
            }).collect(Collectors.toList());

            return ApiResponse.success(responses, "查询成功");
        } catch (Exception e) {
            log.error("查询实验列表失败", e);
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 查询所有实验列表
     *
     * @return 实验列表
     */
    @GetMapping
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<List<ExperimentResponse>> getAllExperiments() {
        try {
            QueryWrapper<Experiment> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("is_deleted", false);
            queryWrapper.orderByDesc("created_time");

            List<Experiment> experiments = experimentService.list(queryWrapper);

            // 收集所有不同的 courseId
            var distinctCourseIds = experiments.stream()
                    .map(Experiment::getCourseId)
                    .filter(id -> id != null)
                    .distinct()
                    .collect(Collectors.toList());

            // 批量查询所有课程信息
            var courseMap = new java.util.HashMap<String, String>();
            if (!distinctCourseIds.isEmpty()) {
                QueryWrapper<com.example.demo.pojo.entity.Course> courseQuery = new QueryWrapper<>();
                courseQuery.in("course_id", distinctCourseIds);
                List<com.example.demo.pojo.entity.Course> courses = courseService.list(courseQuery);
                courses.forEach(course -> courseMap.put(course.getCourseId(), course.getCourseName()));
            }

            // 构建响应，使用 Map 避免重复查询
            java.util.Map<String, String> finalCourseMap = courseMap;
            List<ExperimentResponse> responses = experiments.stream().map(experiment -> {
                ExperimentResponse response = new ExperimentResponse();
                BeanUtils.copyProperties(experiment, response);
                response.setCourseName(finalCourseMap.get(experiment.getCourseId()));
                return response;
            }).collect(Collectors.toList());

            return ApiResponse.success(responses, "查询成功");
        } catch (Exception e) {
            log.error("查询实验列表失败", e);
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }
}
