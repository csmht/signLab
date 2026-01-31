package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.pojo.entity.ClassroomQuizAnswer;
import org.apache.ibatis.annotations.Mapper;

/**
 * 课堂小测答案Mapper
 */
@Mapper
public interface ClassroomQuizAnswerMapper extends BaseMapper<ClassroomQuizAnswer> {
}
