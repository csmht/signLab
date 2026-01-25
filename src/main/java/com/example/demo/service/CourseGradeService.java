package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.mapper.CourseGradeMapper;
import com.example.demo.pojo.entity.CourseGrade;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 课程成绩服务
 * 提供课程成绩的业务逻辑处理
 */
@Slf4j
@Service
public class CourseGradeService extends ServiceImpl<CourseGradeMapper, CourseGrade> {

    /**
     * 根据学生用户名查询课程成绩
     */
    public CourseGrade getByStudentUsername(String studentUsername) {
        QueryWrapper<CourseGrade> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("student_username", studentUsername);
        return getOne(queryWrapper);
    }
}