package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.pojo.entity.Class;
import org.apache.ibatis.annotations.Mapper;

/**
 * 班级Mapper接口
 * 提供班级数据访问操作
 */
@Mapper
public interface ClassMapper extends BaseMapper<Class> {
}