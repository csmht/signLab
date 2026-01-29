package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.mapper.ClassExperimentProcedureTimeMapper;
import com.example.demo.pojo.entity.ClassExperimentProcedureTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 班级实验步骤时间配置服务
 */
@Service
@RequiredArgsConstructor
public class ClassExperimentProcedureTimeService
        extends ServiceImpl<ClassExperimentProcedureTimeMapper, ClassExperimentProcedureTime> {

    /**
     * 查询指定班级实验和步骤的时间配置
     *
     * @param classExperimentId       班级实验ID
     * @param experimentalProcedureId 实验步骤ID
     * @return 时间配置记录
     */
    public ClassExperimentProcedureTime getByClassExperimentAndProcedure(
            Long classExperimentId, Long experimentalProcedureId) {
        QueryWrapper<ClassExperimentProcedureTime> wrapper = new QueryWrapper<>();
        wrapper.eq("class_experiment_id", classExperimentId);
        wrapper.eq("experimental_procedure_id", experimentalProcedureId);
        return getOne(wrapper);
    }

    /**
     * 批量查询指定班级实验的所有步骤时间配置
     *
     * @param classExperimentId 班级实验ID
     * @return 时间配置记录列表
     */
    public List<ClassExperimentProcedureTime> listByClassExperiment(Long classExperimentId) {
        QueryWrapper<ClassExperimentProcedureTime> wrapper = new QueryWrapper<>();
        wrapper.eq("class_experiment_id", classExperimentId);
        return list(wrapper);
    }

    /**
     * 保存或更新时间配置
     *
     * @param classExperimentId       班级实验ID
     * @param experimentalProcedureId 实验步骤ID
     * @param startTime               步骤开始时间
     * @param endTime                 步骤结束时间
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdateTime(Long classExperimentId, Long experimentalProcedureId,
                                 LocalDateTime startTime, LocalDateTime endTime) {
        ClassExperimentProcedureTime existing = getByClassExperimentAndProcedure(
                classExperimentId, experimentalProcedureId);

        if (existing != null) {
            existing.setStartTime(startTime);
            existing.setEndTime(endTime);
            updateById(existing);
        } else {
            ClassExperimentProcedureTime newTime = new ClassExperimentProcedureTime();
            newTime.setClassExperimentId(classExperimentId);
            newTime.setExperimentalProcedureId(experimentalProcedureId);
            newTime.setStartTime(startTime);
            newTime.setEndTime(endTime);
            save(newTime);
        }
    }

    /**
     * 删除时间配置
     *
     * @param classExperimentId       班级实验ID
     * @param experimentalProcedureId 实验步骤ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteTime(Long classExperimentId, Long experimentalProcedureId) {
        QueryWrapper<ClassExperimentProcedureTime> wrapper = new QueryWrapper<>();
        wrapper.eq("class_experiment_id", classExperimentId);
        wrapper.eq("experimental_procedure_id", experimentalProcedureId);
        remove(wrapper);
    }
}
