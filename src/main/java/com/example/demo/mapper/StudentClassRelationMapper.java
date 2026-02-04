package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.pojo.entity.StudentClassRelation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 学生班级关系Mapper接口
 * 提供学生班级关系数据访问操作
 */
@Mapper
public interface StudentClassRelationMapper extends BaseMapper<StudentClassRelation> {

    /**
     * 统计班级学生数量
     *
     * @param classCode 班级编号
     * @return 学生数量
     */
    @Select("SELECT COUNT(*) FROM student_class_relations WHERE class_code = #{classCode} AND is_deleted = 0")
    int countStudentsByClassCode(@Param("classCode") String classCode);
}