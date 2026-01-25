package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.pojo.entity.Course;
import org.apache.ibatis.annotations.Mapper;

/**
 * 课程Mapper接口
 * 提供课程数据访问操作
 */
@Mapper
public interface CourseMapper extends BaseMapper<Course> {
}