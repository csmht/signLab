package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.mapper.ClassExperimentClassRelationMapper;
import com.example.demo.mapper.ClassExperimentMapper;
import com.example.demo.pojo.entity.Class;
import com.example.demo.pojo.entity.ClassExperiment;
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

    private final ClassExperimentMapper classExperimentMapper;
    private final ClassService classService;

    /**
     * 根据班级实验ID查询关联的班级列表
     *
     * @param classExperimentId 班级实验ID
     * @return 班级编号列表
     */
    public List<String> getClassCodesByExperimentId(Long classExperimentId) {
        QueryWrapper<ClassExperimentClassRelation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("class_experiment_id", classExperimentId);
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
        QueryWrapper<ClassExperimentClassRelation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("class_code", classCode);
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
        QueryWrapper<ClassExperimentClassRelation> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("class_code", classCodes);
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
            QueryWrapper<ClassExperimentClassRelation> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("class_experiment_id", classExperimentId)
                    .eq("class_code", classCode);
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
            QueryWrapper<ClassExperimentClassRelation> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("class_experiment_id", classExperimentId)
                    .eq("class_code", classCode);
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
        QueryWrapper<ClassExperimentClassRelation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("class_experiment_id", classExperimentId);
        remove(queryWrapper);
    }

    /**
     * 根据实验ID查询所有参与的班级列表
     *
     * @param experimentId 实验ID
     * @return 班级详细信息列表
     */
    public List<Class> getClassesByExperimentId(String experimentId) {
        // 1. 根据实验ID查询所有相关的班级实验ID
        QueryWrapper<ClassExperiment> classExperimentQuery = new QueryWrapper<>();
        classExperimentQuery.eq("experiment_id", experimentId);
        List<ClassExperiment> classExperiments = classExperimentMapper.selectList(classExperimentQuery);

        if (classExperiments == null || classExperiments.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. 获取所有班级实验ID
        List<Long> classExperimentIds = classExperiments.stream()
                .map(ClassExperiment::getId)
                .collect(Collectors.toList());

        // 3. 根据班级实验ID查询所有班级编号
        QueryWrapper<ClassExperimentClassRelation> relationQuery = new QueryWrapper<>();
        relationQuery.in("class_experiment_id", classExperimentIds);
        List<ClassExperimentClassRelation> relations = list(relationQuery);

        if (relations == null || relations.isEmpty()) {
            return new ArrayList<>();
        }

        // 4. 获取所有班级编号并去重
        List<String> classCodes = relations.stream()
                .map(ClassExperimentClassRelation::getClassCode)
                .distinct()
                .collect(Collectors.toList());

        // 5. 查询所有班级详细信息
        List<Class> classes = new ArrayList<>();
        for (String classCode : classCodes) {
            Class clazz = classService.getByClassCode(classCode);
            if (clazz != null) {
                classes.add(clazz);
            }
        }

        return classes;
    }
}
