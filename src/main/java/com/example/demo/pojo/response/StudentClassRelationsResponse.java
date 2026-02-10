package com.example.demo.pojo.response;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.pojo.entity.StudentClassRelation;
import com.example.demo.pojo.entity.User;
import com.tangzc.mpe.autotable.annotation.Column;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class StudentClassRelationsResponse {
    /** 主键ID */
    private Long id;

    /** 学生用户名(学号) */
    private String studentUsername;

    /** 班级编号 */
    private String classCode;

    /** 绑定时间 */
    private LocalDateTime bindTime;

    /** 学生姓名(学号) */
    private String studentName;



    public static List<StudentClassRelationsResponse> getStudentsByClassCodePage(List<StudentClassRelation> records, UserMapper userMapper) {
        List<String> ids = records.stream().map(StudentClassRelation::getStudentUsername).toList();
        List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>().in(User::getId, ids));
        Map<String,String> idNameMap = new HashMap<String,String>();

        users.forEach(user -> {
            idNameMap.put(user.getUsername(),user.getName());
        });

        List<StudentClassRelationsResponse> ans = new ArrayList<>(records.size());
        records.forEach(record -> {
            StudentClassRelationsResponse response = new StudentClassRelationsResponse();
            BeanUtils.copyProperties(record, response);
            response.setStudentName(idNameMap.get(record.getStudentUsername()));
            ans.add(response);
        });

        return ans;
    }
}
