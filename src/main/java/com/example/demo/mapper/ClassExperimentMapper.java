package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.pojo.entity.ClassExperiment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 班级实验Mapper接口
 * 提供班级实验数据访问操作
 */
@Mapper
public interface ClassExperimentMapper extends BaseMapper<ClassExperiment> {
}