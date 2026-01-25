package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.pojo.entity.Experiment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 实验Mapper接口
 * 提供实验数据访问操作
 */
@Mapper
public interface ExperimentMapper extends BaseMapper<Experiment> {
}