package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.mapper.ClassExperimentClassRelationMapper;
import com.example.demo.pojo.entity.ClassExperimentClassRelation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 班级实验-班级关联服务
 * 提供班级实验与班级关联关系的业务逻辑处理
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClassExperimentClassRelationService
    extends ServiceImpl<ClassExperimentClassRelationMapper, ClassExperimentClassRelation> {

    /**
     * 根据班级实验ID查询关联的班级列表
     *
     * @param classExperimentId 班级实验ID
     * @return 班级编号列表
     */
    public List<String> getClassCodesByExperimentId(Long classExperimentId) {
        LambdaQueryWrapper<ClassExperimentClassRelation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ClassExperimentClassRelation::getClassExperimentId, classExperimentId);
        return list(queryWrapper).stream()
                .map(ClassExperimentClassRelation::getClassCode)
                .collect(Collectors.toList());
    }

    /**
     * 根据班级编号查询关联的班级实验ID列表
     *
     * @param classCode 班级编号
     * @return 班级实验ID列表
     */
    public List<Long> getExperimentIdsByClassCode(String classCode) {
        LambdaQueryWrapper<ClassExperimentClassRelation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ClassExperimentClassRelation::getClassCode, classCode);
        return list(queryWrapper).stream()
                .map(ClassExperimentClassRelation::getClassExperimentId)
                .collect(Collectors.toList());
    }

    /**
     * 根据班级编号列表查询关联的班级实验ID列表
     *
     * @param classCodes 班级编号列表
     * @return 班级实验ID列表（去重）
     */
    public List<Long> getExperimentIdsByClassCodes(List<String> classCodes) {
        if (classCodes == null || classCodes.isEmpty()) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<ClassExperimentClassRelation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(ClassExperimentClassRelation::getClassCode, classCodes);
        return list(queryWrapper).stream()
                .map(ClassExperimentClassRelation::getClassExperimentId)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 批量绑定班级到班级实验（合班）
     *
     * @param classExperimentId 班级实验ID
     * @param classCodes 班级编号列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchBindClassesToExperiment(Long classExperimentId, List<String> classCodes) {
        if (classCodes == null || classCodes.isEmpty()) {
            return;
        }
        for (String classCode : classCodes) {
            LambdaQueryWrapper<ClassExperimentClassRelation> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ClassExperimentClassRelation::getClassExperimentId, classExperimentId)
                    .eq(ClassExperimentClassRelation::getClassCode, classCode);
            if (getOne(queryWrapper) == null) {
                ClassExperimentClassRelation relation = new ClassExperimentClassRelation();
                relation.setClassExperimentId(classExperimentId);
                relation.setClassCode(classCode);
                save(relation);
                log.info("班级 {} 绑定到班级实验 {} 成功", classCode, classExperimentId);
            }
        }
    }

    /**
     * 批量解绑班级
     *
     * @param classExperimentId 班级实验ID
     * @param classCodes 班级编号列表
     * @return 解绑成功的数量
     */
    @Transactional(rollbackFor = Exception.class)
    public int batchUnbindClasses(Long classExperimentId, List<String> classCodes) {
        if (classCodes == null || classCodes.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (String classCode : classCodes) {
            LambdaQueryWrapper<ClassExperimentClassRelation> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ClassExperimentClassRelation::getClassExperimentId, classExperimentId)
                    .eq(ClassExperimentClassRelation::getClassCode, classCode);
            if (remove(queryWrapper)) {
                count++;
                log.info("班级 {} 从班级实验 {} 解绑成功", classCode, classExperimentId);
            }
        }
        return count;
    }

    /**
     * 删除班级实验的所有班级关联
     *
     * @param classExperimentId 班级实验ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeAllByExperimentId(Long classExperimentId) {
        LambdaQueryWrapper<ClassExperimentClassRelation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ClassExperimentClassRelation::getClassExperimentId, classExperimentId);
        remove(queryWrapper);
    }

    /**
     * 根据班级实验ID列表查询关联的班级编号列表（去重）
     *
     * @param classExperimentIds 班级实验ID列表
     * @return 班级编号列表（去重）
     */
    public List<String> getClassCodesByExperimentIds(List<Long> classExperimentIds) {
        if (classExperimentIds == null || classExperimentIds.isEmpty()) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<ClassExperimentClassRelation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(ClassExperimentClassRelation::getClassExperimentId, classExperimentIds);
        return list(queryWrapper).stream()
                .map(ClassExperimentClassRelation::getClassCode)
                .distinct()
                .collect(Collectors.toList());
    }
}
