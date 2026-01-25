package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.mapper.StudentClassRelationMapper;
import com.example.demo.pojo.entity.StudentClassRelation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 学生班级关系服务
 * 提供学生班级关系的业务逻辑处理
 */
@Slf4j
@Service
public class StudentClassRelationService extends ServiceImpl<StudentClassRelationMapper, StudentClassRelation> {

    /**
     * 根据学生用户名查询班级关系列表
     */
    public java.util.List<StudentClassRelation> getByStudentUsername(String studentUsername) {
        QueryWrapper<StudentClassRelation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("student_username", studentUsername);
        return list(queryWrapper);
    }

    /**
     * 根据班级代码查询学生列表
     */
    public java.util.List<StudentClassRelation> getByClassCode(String classCode) {
        QueryWrapper<StudentClassRelation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("class_code", classCode);
        return list(queryWrapper);
    }
}