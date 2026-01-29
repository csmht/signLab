package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.pojo.entity.Tag;
import org.apache.ibatis.annotations.Mapper;

/**
 * 标签Mapper接口
 * 提供标签数据访问操作
 */
@Mapper
public interface TagMapper extends BaseMapper<Tag> {
}
