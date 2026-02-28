package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.mapper.ExperimentMapper;
import com.example.demo.pojo.entity.Experiment;
import com.example.demo.pojo.request.ExperimentQueryRequest;
import com.example.demo.pojo.response.ExperimentResponse;
import com.example.demo.pojo.response.PageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

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
        LambdaQueryWrapper<Experiment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Experiment::getId, experimentCode);
        return getOne(queryWrapper);
    }

    /**
     * 查询实验列表（分页或列表）
     *
     * @param request 查询请求
     * @return 查询结果
     */
    public PageResponse<ExperimentResponse> queryExperiments(ExperimentQueryRequest request) {
        // 构建查询条件
        LambdaQueryWrapper<Experiment> queryWrapper = new LambdaQueryWrapper<>();

        // 课程ID（精确查询）
        if (StringUtils.hasText(request.getCourseId())) {
            queryWrapper.eq(Experiment::getCourseId, request.getCourseId());
        }

        // 实验名称（模糊查询）
        if (StringUtils.hasText(request.getExperimentName())) {
            queryWrapper.like(Experiment::getExperimentName, request.getExperimentName());
        }

        // 排序：按创建时间倒序
        queryWrapper.orderByDesc(Experiment::getCreatedTime);

        // 判断是否分页查询
        if (Boolean.TRUE.equals(request.getPageable())) {
            // 分页查询
            Page<Experiment> page = new Page<>(request.getCurrent(), request.getSize());
            Page<Experiment> resultPage = page(page, queryWrapper);

            // 转换为响应DTO
            List<ExperimentResponse> records = resultPage.getRecords().stream()
                    .map(this::convertToExperimentResponse)
                    .collect(Collectors.toList());

            return PageResponse.of(
                    resultPage.getCurrent(),
                    resultPage.getSize(),
                    resultPage.getTotal(),
                    records
            );
        } else {
            // 列表查询
            List<Experiment> list = list(queryWrapper);
            List<ExperimentResponse> records = list.stream()
                    .map(this::convertToExperimentResponse)
                    .collect(Collectors.toList());

            return PageResponse.of(
                    1L,
                    (long) records.size(),
                    (long) records.size(),
                    records
            );
        }
    }

    /**
     * 转换为实验响应DTO
     */
    private ExperimentResponse convertToExperimentResponse(Experiment experiment) {
        ExperimentResponse response = new ExperimentResponse();
        BeanUtils.copyProperties(experiment, response);
        return response;
    }
}