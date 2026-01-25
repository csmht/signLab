package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.mapper.AnswerFileMapper;
import com.example.demo.pojo.entity.AnswerFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 答题文件服务
 * 提供答题文件的业务逻辑处理
 */
@Slf4j
@Service
public class AnswerFileService extends ServiceImpl<AnswerFileMapper, AnswerFile> {

    /**
     * 根据学生用户名查询答题文件
     */
    public AnswerFile getByStudentUsername(String studentUsername) {
        QueryWrapper<AnswerFile> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("student_username", studentUsername);
        return getOne(queryWrapper);
    }
}