package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.pojo.entity.ProcedureTopic;
import org.apache.ibatis.annotations.Mapper;

/**
 * 实验步骤题库Mapper接口
 */
@Mapper
public interface ProcedureTopicMapper extends BaseMapper<ProcedureTopic> {
}
