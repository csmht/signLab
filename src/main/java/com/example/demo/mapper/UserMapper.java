package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.pojo.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("select * from users where " +
            "username in " +
                "(select student_username from student_class_relations where " +
                    "class_code = #{classCode} " +
                    "AND is_deleted = 0) " +
            "AND role = 'student' " +
            "AND is_deleted = 0 " +
            "AND username not in " +
                "(select student_username from attendance_records " +
            "where course_id = #{className} " +
            "AND is_deleted = 0)")
    List<User> selectNoAbsentStudentByClassCode(String classCode, String className);
}