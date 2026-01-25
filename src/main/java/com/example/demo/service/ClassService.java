package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.ClassMapper;
import com.example.demo.pojo.dto.BatchAddClassRequest;
import com.example.demo.pojo.dto.BatchAddClassResponse;
import com.example.demo.pojo.dto.ClassWithExperimentsResponse;
import com.example.demo.pojo.entity.Class;
import com.example.demo.pojo.entity.ClassExperiment;
import com.example.demo.pojo.entity.Experiment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 班级服务
 * 提供班级的业务逻辑处理
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClassService extends ServiceImpl<ClassMapper, Class> {

    private final ClassExperimentService classExperimentService;
    private final ExperimentService experimentService;

    /**
     * 根据班级代码查询班级
     */
    public Class getByClassCode(String classCode) {
        QueryWrapper<Class> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("class_code", classCode);
        return getOne(queryWrapper);
    }

    /**
     * 根据ID查询班级及其实验信息
     *
     * @param id 班级ID
     * @return 班级带实验信息响应
     */
    public ClassWithExperimentsResponse getClassWithExperimentsById(Long id) {
        // 查询班级信息
        Class clazz = getById(id);
        if (clazz == null) {
            throw new BusinessException(404, "班级不存在");
        }

        return buildClassWithExperimentsResponse(clazz);
    }

    /**
     * 根据班级代码查询班级及其实验信息
     *
     * @param classCode 班级代码
     * @return 班级带实验信息响应
     */
    public ClassWithExperimentsResponse getClassWithExperimentsByCode(String classCode) {
        // 查询班级信息
        Class clazz = getByClassCode(classCode);
        if (clazz == null) {
            throw new BusinessException(404, "班级不存在");
        }

        return buildClassWithExperimentsResponse(clazz);
    }

    /**
     * 构建班级带实验信息响应
     *
     * @param clazz 班级实体
     * @return 班级带实验信息响应
     */
    private ClassWithExperimentsResponse buildClassWithExperimentsResponse(Class clazz) {
        ClassWithExperimentsResponse response = new ClassWithExperimentsResponse();
        response.setId(clazz.getId());
        response.setClassCode(clazz.getClassCode());
        response.setClassName(clazz.getClassName());
        response.setVerificationCode(clazz.getVerificationCode());
        response.setStudentCount(clazz.getStudentCount());
        response.setCreateTime(clazz.getCreateTime());
        response.setUpdateTime(clazz.getUpdateTime());

        // 查询班级的实验列表
        QueryWrapper<ClassExperiment> classExperimentQuery = new QueryWrapper<>();
        classExperimentQuery.eq("class_code", clazz.getClassCode());
        List<ClassExperiment> classExperiments = classExperimentService.list(classExperimentQuery);

        // 构建实验信息列表
        List<ClassWithExperimentsResponse.ExperimentInfo> experimentInfos = new ArrayList<>();
        for (ClassExperiment classExperiment : classExperiments) {
            ClassWithExperimentsResponse.ExperimentInfo experimentInfo = new ClassWithExperimentsResponse.ExperimentInfo();
            experimentInfo.setClassExperimentId(classExperiment.getId());
            experimentInfo.setCourseId(classExperiment.getCourseId());
            experimentInfo.setExperimentId(classExperiment.getExperimentId());
            experimentInfo.setCourseTime(classExperiment.getCourseTime());
            experimentInfo.setStartTime(classExperiment.getStartTime());
            experimentInfo.setEndTime(classExperiment.getEndTime());
            experimentInfo.setExperimentLocation(classExperiment.getExperimentLocation());

            // 查询实验名称
            QueryWrapper<Experiment> experimentQuery = new QueryWrapper<>();
            experimentQuery.eq("id", classExperiment.getExperimentId());
            Experiment experiment = experimentService.getOne(experimentQuery);
            if (experiment != null) {
                experimentInfo.setExperimentName(experiment.getExperimentName());
            }

            experimentInfos.add(experimentInfo);
        }

        response.setExperiments(experimentInfos);

        return response;
    }

    /**
     * 批量添加班级
     *
     * @param request 批量添加班级请求
     * @return 批量添加班级响应
     */
    @Transactional(rollbackFor = Exception.class)
    public BatchAddClassResponse batchAddClasses(BatchAddClassRequest request) {
        BatchAddClassResponse response = new BatchAddClassResponse();
        response.setSuccessCount(0);
        response.setFailCount(0);
        response.setSuccessList(new ArrayList<>());
        response.setFailList(new ArrayList<>());

        if (request.getClasses() == null || request.getClasses().isEmpty()) {
            throw new BusinessException(400, "班级列表不能为空");
        }

        for (BatchAddClassRequest.ClassInfo classInfo : request.getClasses()) {
            BatchAddClassResponse.ClassResult result = new BatchAddClassResponse.ClassResult();
            result.setClassCode(classInfo.getClassCode());
            result.setClassName(classInfo.getClassName());

            try {
                // 检查班级是否已存在
                Class existingClass = getByClassCode(classInfo.getClassCode());
                if (existingClass != null) {
                    result.setSuccess(false);
                    result.setMessage("班级编号已存在");
                    response.getFailList().add(result);
                    response.setFailCount(response.getFailCount() + 1);
                    continue;
                }

                // 创建班级
                Class clazz = new Class();
                clazz.setClassCode(classInfo.getClassCode());
                clazz.setClassName(classInfo.getClassName());
                clazz.setVerificationCode(classInfo.getVerificationCode());
                clazz.setStudentCount(0);

                boolean saved = save(clazz);
                if (saved) {
                    result.setSuccess(true);
                    response.getSuccessList().add(result);
                    response.setSuccessCount(response.getSuccessCount() + 1);
                    log.info("班级 {} 添加成功", classInfo.getClassCode());
                } else {
                    result.setSuccess(false);
                    result.setMessage("班级添加失败");
                    response.getFailList().add(result);
                    response.setFailCount(response.getFailCount() + 1);
                }
            } catch (Exception e) {
                log.error("添加班级 {} 失败", classInfo.getClassCode(), e);
                result.setSuccess(false);
                result.setMessage(e.getMessage());
                response.getFailList().add(result);
                response.setFailCount(response.getFailCount() + 1);
            }
        }

        return response;
    }

    /**
     * 更新班级人数
     *
     * @param classCode 班级编号
     * @param delta 人数变化量
     */
    public void updateStudentCount(String classCode, int delta) {
        Class clazz = getByClassCode(classCode);
        if (clazz != null) {
            int newCount = clazz.getStudentCount() + delta;
            if (newCount < 0) {
                newCount = 0;
            }
            clazz.setStudentCount(newCount);
            updateById(clazz);
        }
    }
}