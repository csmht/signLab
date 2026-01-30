package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.pojo.entity.TimedQuizProcedure;
import org.apache.ibatis.annotations.Mapper;

/**
 * 限时答题题目配置Mapper
 */
@Mapper
public interface TimedQuizProcedureMapper extends BaseMapper<TimedQuizProcedure> {
    // MyBatis-Plus 自动提供 CRUD 方法
}
