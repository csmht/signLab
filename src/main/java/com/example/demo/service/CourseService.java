package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.CourseMapper;
import com.example.demo.pojo.request.CourseQueryRequest;
import com.example.demo.pojo.request.CreateCourseRequest;
import com.example.demo.pojo.request.UpdateCourseRequest;
import com.example.demo.pojo.response.CourseResponse;
import com.example.demo.pojo.response.PageResponse;
import com.example.demo.pojo.entity.Course;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 课程服务
 * 提供课程的业务逻辑处理
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CourseService extends ServiceImpl<CourseMapper, Course> {

    /**
     * 根据课程代码查询课程
     */
    public Course getByCourseCode(String courseCode) {
        LambdaQueryWrapper<Course> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Course::getCourseId, courseCode);
        return getOne(queryWrapper);
    }

    /**
     * 生成课程ID
     * 格式: COURSE + 6位数字(如 COURSE000001)
     *
     * @return 课程ID
     */
    public String generateCourseId() {
        // 查询最大的课程ID
        LambdaQueryWrapper<Course> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Course::getId);
        queryWrapper.last("LIMIT 1");
        Course lastCourse = getOne(queryWrapper);

        int nextNum = 1;
        if (lastCourse != null && lastCourse.getCourseId() != null) {
            String lastId = lastCourse.getCourseId();
            // 提取数字部分(COURSE000001 -> 000001 -> 1)
            if (lastId.matches("COURSE\\d{6}")) {
                String numStr = lastId.substring(6); // 去掉 "COURSE"
                try {
                    nextNum = Integer.parseInt(numStr) + 1;
                } catch (NumberFormatException e) {
                    log.warn("解析课程ID失败: {}", lastId);
                }
            }
        }

        // 格式化为6位数字
        return String.format("COURSE%06d", nextNum);
    }

    /**
     * 分页查询课程列表
     *
     * @param request 查询请求
     * @return 分页结果
     */
    public PageResponse<CourseResponse> queryCourses(CourseQueryRequest request) {
        LambdaQueryWrapper<Course> queryWrapper = new LambdaQueryWrapper<>();

        // 支持按课程ID精确查询
        if (request.getCourseId() != null && !request.getCourseId().trim().isEmpty()) {
            queryWrapper.eq(Course::getCourseId, request.getCourseId().trim());
        }

        // 支持按课程名称模糊查询
        if (request.getCourseName() != null && !request.getCourseName().trim().isEmpty()) {
            queryWrapper.like(Course::getCourseName, request.getCourseName().trim());
        }

        // 按创建时间倒序排序
        queryWrapper.orderByDesc(Course::getCreateTime);

        // 分页查询
        if (request.getPageable() != null && request.getPageable()) {
            Page<Course> page = new Page<>(request.getCurrent(), request.getSize());
            Page<Course> result = page(page, queryWrapper);

            // 构建响应列表
            List<CourseResponse> records = result.getRecords().stream()
                    .map(this::buildCourseResponse)
                    .collect(Collectors.toList());

            return PageResponse.of(
                    result.getCurrent(),
                    result.getSize(),
                    result.getTotal(),
                    records
            );
        } else {
            // 不分页，返回全部数据
            List<Course> courses = list(queryWrapper);
            List<CourseResponse> records = courses.stream()
                    .map(this::buildCourseResponse)
                    .collect(Collectors.toList());

            return PageResponse.of(1L, (long) records.size(), (long) records.size(), records);
        }
    }

    /**
     * 查询当前教师的课程列表（分页）
     *
     * @param teacherUsername 教师用户名
     * @param request         查询请求
     * @return 分页结果
     */
    public PageResponse<CourseResponse> queryMyCourses(
            String teacherUsername, CourseQueryRequest request) {

        LambdaQueryWrapper<Course> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Course::getTeacherUsername, teacherUsername);

        // 支持按课程ID精确查询
        if (request.getCourseId() != null && !request.getCourseId().trim().isEmpty()) {
            queryWrapper.eq(Course::getCourseId, request.getCourseId().trim());
        }

        // 支持按课程名称模糊查询
        if (request.getCourseName() != null && !request.getCourseName().trim().isEmpty()) {
            queryWrapper.like(Course::getCourseName, request.getCourseName().trim());
        }

        // 按创建时间倒序排序
        queryWrapper.orderByDesc(Course::getCreateTime);

        // 分页查询
        if (request.getPageable() != null && request.getPageable()) {
            Page<Course> page = new Page<>(request.getCurrent(), request.getSize());
            Page<Course> result = page(page, queryWrapper);

            // 构建响应列表
            List<CourseResponse> records = result.getRecords().stream()
                    .map(this::buildCourseResponse)
                    .collect(Collectors.toList());

            return PageResponse.of(
                    result.getCurrent(),
                    result.getSize(),
                    result.getTotal(),
                    records
            );
        } else {
            // 不分页，返回全部数据
            List<Course> courses = list(queryWrapper);
            List<CourseResponse> records = courses.stream()
                    .map(this::buildCourseResponse)
                    .collect(Collectors.toList());

            return PageResponse.of(1L, (long) records.size(), (long) records.size(), records);
        }
    }

    /**
     * 根据ID查询课程
     *
     * @param id 课程ID
     * @return 课程信息
     */
    public CourseResponse getCourseById(Long id) {
        Course course = getById(id);
        if (course == null) {
            throw new BusinessException(404, "课程不存在");
        }
        return buildCourseResponse(course);
    }

    /**
     * 创建课程
     *
     * @param request 创建课程请求
     * @param teacherUsername 教师用户名
     * @return 课程信息
     */
    public CourseResponse createCourse(CreateCourseRequest request, String teacherUsername) {


        // 如果未传入课程ID,则自动生成
        String courseId = request.getCourseId();
        if (courseId == null || courseId.trim().isEmpty()) {
            courseId = generateCourseId();
        }

        // 检查课程ID是否已存在
        Course existingCourse = getByCourseCode(courseId);
        if (existingCourse != null) {
            throw new BusinessException(400, "课程ID已存在");
        }

        // 创建课程
        Course course = new Course();
        course.setCourseId(courseId);
        course.setCourseName(request.getCourseName());
        course.setTeacherUsername(teacherUsername);

        boolean saved = save(course);
        if (!saved) {
            throw new BusinessException(500, "创建课程失败");
        }

        log.info("教师 {} 创建课程 {} 成功", teacherUsername, courseId);

        return buildCourseResponse(course);
    }

    /**
     * 更新课程
     *
     * @param id      课程ID
     * @param request 更新课程请求
     * @param teacherUsername 教师用户名
     * @return 课程信息
     */
    public CourseResponse updateCourse(Long id, UpdateCourseRequest request, String teacherUsername) {
        // 查询课程是否存在
        Course course = getById(id);
        if (course == null) {
            throw new BusinessException(404, "课程不存在");
        }

        // 验证是否是课程创建者
        if (!course.getTeacherUsername().equals(teacherUsername)) {
            throw new BusinessException(403, "无权修改此课程");
        }

        // 更新课程信息
        if (request.getCourseName() != null && !request.getCourseName().trim().isEmpty()) {
            course.setCourseName(request.getCourseName().trim());
        }

        boolean updated = updateById(course);
        if (!updated) {
            throw new BusinessException(500, "更新课程失败");
        }

        log.info("教师 {} 更新课程 {} 成功", teacherUsername, course.getCourseId());

        return buildCourseResponse(course);
    }

    /**
     * 删除课程
     *
     * @param id              课程ID
     * @param teacherUsername 教师用户名
     * @return 是否删除成功
     */
    public boolean deleteCourse(Long id, String teacherUsername) {
        // 查询课程是否存在
        Course course = getById(id);
        if (course == null) {
            throw new BusinessException(404, "课程不存在");
        }

        // 验证是否是课程创建者
        if (!course.getTeacherUsername().equals(teacherUsername)) {
            throw new BusinessException(403, "无权删除此课程");
        }

        boolean removed = removeById(id);
        if (removed) {
            log.info("教师 {} 删除课程 {} 成功", teacherUsername, course.getCourseId());
        }

        return removed;
    }

    /**
     * 构建课程响应
     *
     * @param course 课程实体
     * @return 课程响应
     */
    private CourseResponse buildCourseResponse(Course course) {
        CourseResponse response = new CourseResponse();
        response.setId(course.getId());
        response.setCourseId(course.getCourseId());
        response.setCourseName(course.getCourseName());
        response.setTeacherUsername(course.getTeacherUsername());
        response.setTeacherEmployeeId(course.getTeacherEmployeeId());
        response.setCreateTime(course.getCreateTime());
        response.setUpdateTime(course.getUpdateTime());
        return response;
    }
}