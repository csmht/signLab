package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.pojo.entity.CourseGrade;
import org.apache.ibatis.annotations.Mapper;

/**
 * 课程成绩Mapper接口
 * 提供课程成绩数据访问操作
 */
@Mapper
public interface CourseGradeMapper extends BaseMapper<CourseGrade> {
}