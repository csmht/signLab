package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.CourseGradeMapper;
import com.example.demo.mapper.CourseMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.pojo.entity.Course;
import com.example.demo.pojo.entity.CourseGrade;
import com.example.demo.pojo.entity.User;
import com.example.demo.pojo.response.CourseGradeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 课程成绩服务
 * 提供课程成绩的业务逻辑处理
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CourseGradeService extends ServiceImpl<CourseGradeMapper, CourseGrade> {

    private final UserMapper userMapper;
    private final CourseMapper courseMapper;

    /**
     * 根据学生用户名查询课程成绩
     */
    public CourseGrade getByStudentUsername(String studentUsername) {
        LambdaQueryWrapper<CourseGrade> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseGrade::getStudentUsername, studentUsername);
        return getOne(queryWrapper);
    }

    /**
     * 查询学生的所有课程成绩
     *
     * @param studentUsername 学生用户名
     * @param semester 学期（可选）
     * @return 成绩列表
     */
    public List<CourseGradeResponse> getStudentGrades(String studentUsername, String semester) {
        LambdaQueryWrapper<CourseGrade> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseGrade::getStudentUsername, studentUsername);

        if (semester != null && !semester.trim().isEmpty()) {
            queryWrapper.eq(CourseGrade::getSemester, semester);
        }

        queryWrapper.orderByDesc(CourseGrade::getGradeTime);

        List<CourseGrade> grades = list(queryWrapper);
        return grades.stream().map(this::buildResponse).collect(Collectors.toList());
    }

    /**
     * 查询课程的所有学生成绩
     *
     * @param courseId 课程ID
     * @param semester 学期（可选）
     * @return 成绩列表
     */
    public List<CourseGradeResponse> getCourseGrades(String courseId, String semester) {
        LambdaQueryWrapper<CourseGrade> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseGrade::getCourseId, courseId);

        if (semester != null && !semester.trim().isEmpty()) {
            queryWrapper.eq(CourseGrade::getSemester, semester);
        }

        queryWrapper.orderByDesc(CourseGrade::getGradeTime);

        List<CourseGrade> grades = list(queryWrapper);
        return grades.stream().map(this::buildResponse).collect(Collectors.toList());
    }

    /**
     * 根据ID查询成绩详情
     *
     * @param gradeId 成绩ID
     * @return 成绩详情
     */
    public CourseGradeResponse getGradeById(Long gradeId) {
        CourseGrade grade = getById(gradeId);
        if (grade == null) {
            throw new BusinessException(404, "成绩不存在");
        }
        return buildResponse(grade);
    }

    /**
     * 创建或更新课程成绩
     *
     * @param courseId 课程ID
     * @param studentUsername 学生用户名
     * @param teacherUsername 教师用户名
     * @param grade 成绩
     * @param gradeNumeric 数字成绩
     * @param gradeType 成绩类型
     * @param teacherComment 教师评语
     * @param semester 学期
     * @return 成绩信息
     */
    @Transactional(rollbackFor = Exception.class)
    public CourseGradeResponse saveGrade(String courseId, String studentUsername, String teacherUsername,
                                         String grade, BigDecimal gradeNumeric, String gradeType,
                                         String teacherComment, String semester) {
        // 1. 验证学生是否存在
        User student = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, studentUsername)
        );
        if (student == null) {
            throw new BusinessException(404, "学生不存在");
        }

        // 2. 验证课程是否存在
        Course course = courseMapper.selectOne(
                new LambdaQueryWrapper<Course>().eq(Course::getId, courseId)
        );
        if (course == null) {
            throw new BusinessException(404, "课程不存在");
        }

        // 3. 查询是否已有成绩记录
        LambdaQueryWrapper<CourseGrade> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseGrade::getStudentUsername, studentUsername)
                .eq(CourseGrade::getCourseId, courseId);
        CourseGrade existingGrade = getOne(queryWrapper);

        CourseGrade gradeEntity;
        if (existingGrade != null) {
            // 更新已有记录
            gradeEntity = existingGrade;
            log.info("更新课程成绩，学生：{}，课程：{}", studentUsername, courseId);
        } else {
            // 创建新记录
            gradeEntity = new CourseGrade();
            gradeEntity.setStudentUsername(studentUsername);
            gradeEntity.setCourseId(courseId);
            gradeEntity.setCreatedTime(LocalDateTime.now());
            log.info("创建课程成绩，学生：{}，课程：{}", studentUsername, courseId);
        }

        // 4. 设置成绩信息
        gradeEntity.setGrade(grade);
        gradeEntity.setGradeNumeric(gradeNumeric);
        gradeEntity.setGradeType(gradeType);
        gradeEntity.setMaxScore(BigDecimal.valueOf(100.00)); // 默认满分100
        gradeEntity.setTeacherUsername(teacherUsername);
        gradeEntity.setTeacherComment(teacherComment);
        gradeEntity.setSemester(semester);
        gradeEntity.setGradeTime(LocalDateTime.now());
        gradeEntity.setUpdatedTime(LocalDateTime.now());
        gradeEntity.setIsApproved(false); // 新成绩默认未审核

        // 5. 保存或更新
        boolean saved = saveOrUpdate(gradeEntity);
        if (!saved) {
            throw new BusinessException(500, "保存成绩失败");
        }

        return buildResponse(gradeEntity);
    }

    /**
     * 审核成绩
     *
     * @param gradeId 成绩ID
     * @param approvedBy 审核人用户名
     */
    @Transactional(rollbackFor = Exception.class)
    public void approveGrade(Long gradeId, String approvedBy) {
        CourseGrade grade = getById(gradeId);
        if (grade == null) {
            throw new BusinessException(404, "成绩不存在");
        }

        grade.setIsApproved(true);
        grade.setApprovedBy(approvedBy);
        grade.setApprovedTime(LocalDateTime.now());
        grade.setUpdatedTime(LocalDateTime.now());

        boolean updated = updateById(grade);
        if (!updated) {
            throw new BusinessException(500, "审核成绩失败");
        }

        log.info("审核成绩成功，ID：{}，审核人：{}", gradeId, approvedBy);
    }

    /**
     * 删除成绩
     *
     * @param gradeId 成绩ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteGrade(Long gradeId) {
        CourseGrade grade = getById(gradeId);
        if (grade == null) {
            throw new BusinessException(404, "成绩不存在");
        }

        boolean deleted = removeById(gradeId);
        if (!deleted) {
            throw new BusinessException(500, "删除成绩失败");
        }

        log.info("删除成绩成功，ID：{}", gradeId);
    }

    /**
     * 构建响应对象
     */
    private CourseGradeResponse buildResponse(CourseGrade grade) {
        CourseGradeResponse response = new CourseGradeResponse();
        response.setId(grade.getId());
        response.setStudentUsername(grade.getStudentUsername());
        response.setCourseId(grade.getCourseId());
        response.setGrade(grade.getGrade());
        response.setGradeNumeric(grade.getGradeNumeric());
        response.setGradeType(grade.getGradeType());
        response.setMaxScore(grade.getMaxScore());
        response.setTeacherUsername(grade.getTeacherUsername());
        response.setTeacherComment(grade.getTeacherComment());
        response.setGradeTime(grade.getGradeTime());
        response.setSemester(grade.getSemester());
        response.setIsApproved(grade.getIsApproved());
        response.setApprovedBy(grade.getApprovedBy());
        response.setApprovedTime(grade.getApprovedTime());
        response.setCreatedTime(grade.getCreatedTime());

        // 查询学生姓名
        User student = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, grade.getStudentUsername())
        );
        response.setStudentName(student != null ? student.getName() : grade.getStudentUsername());

        // 查询教师姓名
        User teacher = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, grade.getTeacherUsername())
        );
        response.setTeacherName(teacher != null ? teacher.getName() : grade.getTeacherUsername());

        // 查询课程名称
        Course course = courseMapper.selectOne(
                new LambdaQueryWrapper<Course>().eq(Course::getId, grade.getCourseId())
        );
        response.setCourseName(course != null ? course.getCourseName() : grade.getCourseId());

        return response;
    }
}