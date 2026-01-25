package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.pojo.entity.StudentClassRelation;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学生班级关系Mapper接口
 * 提供学生班级关系数据访问操作
 */
@Mapper
public interface StudentClassRelationMapper extends BaseMapper<StudentClassRelation> {
}