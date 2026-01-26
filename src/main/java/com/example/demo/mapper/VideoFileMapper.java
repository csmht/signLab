package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.pojo.entity.VideoFile;
import org.apache.ibatis.annotations.Mapper;

/**
 * 视频文件Mapper接口
 */
@Mapper
public interface VideoFileMapper extends BaseMapper<VideoFile> {
}
