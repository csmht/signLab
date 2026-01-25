package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.pojo.entity.Topic;
import org.apache.ibatis.annotations.Mapper;

/**
 * 题目Mapper接口
 * 提供主题数据访问操作
 */
@Mapper
public interface TopicMapper extends BaseMapper<Topic> {
}