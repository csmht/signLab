package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.pojo.entity.AnswerFile;
import org.apache.ibatis.annotations.Mapper;

/**
 * 答题文件Mapper接口
 * 提供答题文件数据访问操作
 */
@Mapper
public interface AnswerFileMapper extends BaseMapper<AnswerFile> {
}