package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.pojo.entity.AssignmentSubmission;
import org.apache.ibatis.annotations.Mapper;

/**
 * 作业提交Mapper接口
 */
@Mapper
public interface AssignmentSubmissionMapper extends BaseMapper<AssignmentSubmission> {
}
