package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.mapper.AnswerPhotoMapper;
import com.example.demo.pojo.entity.AnswerPhoto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 答题照片服务
 * 提供答题照片的业务逻辑处理
 */
@Slf4j
@Service
public class AnswerPhotoService extends ServiceImpl<AnswerPhotoMapper, AnswerPhoto> {

    /**
     * 根据学生用户名查询答题照片
     */
    public AnswerPhoto getByStudentUsername(String studentUsername) {
        QueryWrapper<AnswerPhoto> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("student_username", studentUsername);
        return getOne(queryWrapper);
    }
}