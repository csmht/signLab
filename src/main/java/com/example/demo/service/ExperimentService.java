package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.mapper.ExperimentMapper;
import com.example.demo.pojo.entity.Experiment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 实验服务
 * 提供实验的业务逻辑处理
 */
@Slf4j
@Service
public class ExperimentService extends ServiceImpl<ExperimentMapper, Experiment> {

    /**
     * 根据实验代码查询实验
     */
    public Experiment getByExperimentCode(String experimentCode) {
        QueryWrapper<Experiment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("experiment_code", experimentCode);
        return getOne(queryWrapper);
    }
}