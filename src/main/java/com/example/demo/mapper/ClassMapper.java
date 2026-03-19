package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.pojo.entity.Class;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 班级Mapper接口
 * 提供班级数据访问操作
 */
@Mapper
public interface ClassMapper extends BaseMapper<Class> {

    /**
     * 获取最大的班级ID（包括已逻辑删除的记录）
     *
     * @return 最大ID，如果没有记录则返回null
     */
    @Select("SELECT MAX(id) FROM classes")
    Long selectMaxId();

    /**
     * 获取最大的班级编号（包括已逻辑删除的记录）
     *
     * @return 最大班级编号，如果没有记录则返回null
     */
    @Select("SELECT class_code FROM classes ORDER BY id DESC LIMIT 1")
    String selectLastClassCode();
}