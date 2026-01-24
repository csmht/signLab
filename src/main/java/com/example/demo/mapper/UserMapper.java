package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.pojo.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户Mapper接口
 * 提供用户数据访问操作
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 查询指定班级未签到的学生
     * 根据班级代码和课程ID查询该班级中未签到的学生列表
     *
     * @param classCode 班级代码
     * @param className 课程ID
     * @return 未签到的学生列表
     */
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