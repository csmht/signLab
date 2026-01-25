package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.mapper.CourseMapper;
import com.example.demo.pojo.entity.Course;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 课程服务
 * 提供课程的业务逻辑处理
 */
@Slf4j
@Service
public class CourseService extends ServiceImpl<CourseMapper, Course> {

    /**
     * 根据课程代码查询课程
     */
    public Course getByCourseCode(String courseCode) {
        QueryWrapper<Course> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("course_code", courseCode);
        return getOne(queryWrapper);
    }
}