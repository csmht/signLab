package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.pojo.entity.ProcedureSubmission;
import org.apache.ibatis.annotations.Mapper;

/**
 * 实验步骤提交Mapper接口
 */
@Mapper
public interface ProcedureSubmissionMapper extends BaseMapper<ProcedureSubmission> {
}
