package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.pojo.entity.StudentProcedureAttachment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学生步骤附件Mapper接口
 */
@Mapper
public interface StudentProcedureAttachmentMapper extends BaseMapper<StudentProcedureAttachment> {
}
