package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.pojo.entity.DataCollection;
import org.apache.ibatis.annotations.Mapper;

/**
 * 数据收集Mapper接口
 */
@Mapper
public interface DataCollectionMapper extends BaseMapper<DataCollection> {
}
