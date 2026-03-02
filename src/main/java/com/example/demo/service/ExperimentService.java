package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.mapper.CourseMapper;
import com.example.demo.mapper.ExperimentMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.pojo.entity.Course;
import com.example.demo.pojo.entity.Experiment;
import com.example.demo.pojo.entity.User;
import com.example.demo.pojo.request.ExperimentQueryRequest;
import com.example.demo.pojo.response.ExperimentResponse;
import com.example.demo.pojo.response.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 实验服务
 * 提供实验的业务逻辑处理
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExperimentService extends ServiceImpl<ExperimentMapper, Experiment> {

    private final CourseMapper courseMapper;
    private final UserMapper userMapper;

    /**
     * ���据实验代码查询实验
     */
    public Experiment getByExperimentCode(String experimentCode) {
        LambdaQueryWrapper<Experiment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Experiment::getId, experimentCode);
        return getOne(queryWrapper);
    }

    /**
     * 查询实验列表（分页或列表）
     *
     * @param request 查询请求
     * @return 查询结果
     */
    public PageResponse<ExperimentResponse> queryExperiments(ExperimentQueryRequest request) {
        // 构建查询条件
        LambdaQueryWrapper<Experiment> queryWrapper = new LambdaQueryWrapper<>();

        // 课程ID（精确查询）
        if (StringUtils.hasText(request.getCourseId())) {
            queryWrapper.eq(Experiment::getCourseId, request.getCourseId());
        }

        // 实验名称（模糊查询）
        if (StringUtils.hasText(request.getExperimentName())) {
            queryWrapper.like(Experiment::getExperimentName, request.getExperimentName());
        }

        // 排序：按创建时间倒序
        queryWrapper.orderByDesc(Experiment::getCreatedTime);

        // 判断是否分页查询
        if (Boolean.TRUE.equals(request.getPageable())) {
            // 分页查询
            Page<Experiment> page = new Page<>(request.getCurrent(), request.getSize());
            Page<Experiment> resultPage = page(page, queryWrapper);

            // 批量转换为响应DTO（包含课程名称和教师姓名）
            List<ExperimentResponse> records = convertToExperimentResponses(resultPage.getRecords());

            return PageResponse.of(
                    resultPage.getCurrent(),
                    resultPage.getSize(),
                    resultPage.getTotal(),
                    records
            );
        } else {
            // 列表查询
            List<Experiment> list = list(queryWrapper);
            List<ExperimentResponse> records = convertToExperimentResponses(list);

            return PageResponse.of(
                    1L,
                    (long) records.size(),
                    (long) records.size(),
                    records
            );
        }
    }

    /**
     * 批量转换为实验响应DTO（包含课程名称和教师姓名）
     *
     * @param experiments 实验列表
     * @return 响应DTO列表
     */
    private List<ExperimentResponse> convertToExperimentResponses(List<Experiment> experiments) {
        if (experiments == null || experiments.isEmpty()) {
            return new ArrayList<>();
        }

        // 1. 提取所有courseId并批量查询课程信息
        List<String> courseIds = experiments.stream()
                .map(Experiment::getCourseId)
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.toList());

        Map<String, Course> courseMap = new HashMap<>();
        if (!courseIds.isEmpty()) {
            courseMap = courseMapper.selectList(
                    new LambdaQueryWrapper<Course>().in(Course::getCourseId, courseIds)
            ).stream().collect(Collectors.toMap(Course::getCourseId, Function.identity()));
        }

        // 2. 提取所有教师用户名并批量查询教师信息
        List<String> teacherUsernames = courseMap.values().stream()
                .map(Course::getTeacherUsername)
                .filter(username -> username != null && !username.trim().isEmpty())
                .distinct()
                .collect(Collectors.toList());

        Map<String, User> teacherMap = new HashMap<>();
        if (!teacherUsernames.isEmpty()) {
            teacherMap = userMapper.selectList(
                    new LambdaQueryWrapper<User>().in(User::getUsername, teacherUsernames)
            ).stream().collect(Collectors.toMap(User::getUsername, Function.identity()));
        }

        // 3. 转换为响应DTO
        final Map<String, Course> finalCourseMap = courseMap;
        final Map<String, User> finalTeacherMap = teacherMap;

        return experiments.stream()
                .map(experiment -> {
                    ExperimentResponse response = new ExperimentResponse();
                    BeanUtils.copyProperties(experiment, response);

                    // 填充课程名称和教师姓名
                    Course course = finalCourseMap.get(experiment.getCourseId());
                    if (course != null) {
                        response.setCourseName(course.getCourseName());

                        // 通过教师用户名查询教师姓名
                        if (course.getTeacherUsername() != null) {
                            User teacher = finalTeacherMap.get(course.getTeacherUsername());
                            if (teacher != null) {
                                response.setTeacherName(teacher.getName());
                            }
                        }
                    }

                    return response;
                })
                .collect(Collectors.toList());
    }
}