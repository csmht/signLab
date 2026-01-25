package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.pojo.entity.AttendanceRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 签到记录Mapper接口
 * 提供签到记录数据访问操作
 */
@Mapper
public interface AttendanceRecordMapper extends BaseMapper<AttendanceRecord> {
}