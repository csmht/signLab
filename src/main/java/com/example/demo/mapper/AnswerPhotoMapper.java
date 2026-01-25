package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.pojo.entity.AnswerPhoto;
import org.apache.ibatis.annotations.Mapper;

/**
 * 答题照片Mapper接口
 * 提供答题照片数据访问操作
 */
@Mapper
public interface AnswerPhotoMapper extends BaseMapper<AnswerPhoto> {
}