package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.mapper.ExperimentalProcedureMapper;
import com.example.demo.pojo.entity.ExperimentalProcedure;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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
    public List<ExperimentalProcedure> getByExperimentId(Long experimentId) {
        LambdaQueryWrapper<ExperimentalProcedure> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ExperimentalProcedure::getExperimentId, experimentId);
        queryWrapper.orderByAsc(ExperimentalProcedure::getNumber);
        return list(queryWrapper);
    }

    /**
     * 根据实验ID获取所有步骤ID列表
     *
     * @param experimentId 实验ID
     * @return 步骤ID列表（按步骤序号升序）
     */
    public List<Long> getProcedureIdsByExperimentId(Long experimentId) {
        LambdaQueryWrapper<ExperimentalProcedure> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ExperimentalProcedure::getExperimentId, experimentId)
                    .select(ExperimentalProcedure::getId)
                    .orderByAsc(ExperimentalProcedure::getNumber);
        return list(queryWrapper).stream()
                .map(ExperimentalProcedure::getId)
                .collect(Collectors.toList());
    }
}