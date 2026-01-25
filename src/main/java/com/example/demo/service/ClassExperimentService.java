package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.mapper.ClassExperimentMapper;
import com.example.demo.pojo.entity.ClassExperiment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 班级实验服务
 * 提供班级实验的业务逻辑处理
 */
@Slf4j
@Service
public class ClassExperimentService extends ServiceImpl<ClassExperimentMapper, ClassExperiment> {

    /**
     * 根据班级代码查询班级实验
     */
    public ClassExperiment getByClassCode(String classCode) {
        QueryWrapper<ClassExperiment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("class_code", classCode);
        return getOne(queryWrapper);
    }
}