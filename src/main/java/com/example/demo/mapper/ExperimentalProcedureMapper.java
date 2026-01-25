package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.pojo.entity.ExperimentalProcedure;
import org.apache.ibatis.annotations.Mapper;

/**
 * 实验步骤Mapper接口
 * 提供实验步骤数据访问操作
 */
@Mapper
public interface ExperimentalProcedureMapper extends BaseMapper<ExperimentalProcedure> {
}