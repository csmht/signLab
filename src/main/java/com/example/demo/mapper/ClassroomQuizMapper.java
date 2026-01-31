package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.pojo.entity.ClassroomQuiz;
import org.apache.ibatis.annotations.Mapper;

/**
 * 课堂小测Mapper
 */
@Mapper
public interface ClassroomQuizMapper extends BaseMapper<ClassroomQuiz> {
}
