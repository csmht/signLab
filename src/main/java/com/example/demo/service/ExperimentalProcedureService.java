package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.mapper.ExperimentalProcedureMapper;
import com.example.demo.pojo.entity.ExperimentalProcedure;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 实验步骤服务
 * 提供实验步骤的业务逻辑处理
 */
@Slf4j
@Service
public class ExperimentalProcedureService extends ServiceImpl<ExperimentalProcedureMapper, ExperimentalProcedure> {

    /**
     * 根据实验ID查询实验步骤列表
     */
    public java.util.List<ExperimentalProcedure> getByExperimentId(Long experimentId) {
        QueryWrapper<ExperimentalProcedure> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("experiment_id", experimentId);
        queryWrapper.orderByAsc("step_order");
        return list(queryWrapper);
    }
}