package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.mapper.ClassMapper;
import com.example.demo.pojo.entity.Class;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 班级服务
 * 提供班级的业务逻辑处理
 */
@Slf4j
@Service
public class ClassService extends ServiceImpl<ClassMapper, Class> {

    /**
     * 根据班级代码查询班级
     */
    public Class getByClassCode(String classCode) {
        QueryWrapper<Class> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("class_code", classCode);
        return getOne(queryWrapper);
    }
}