package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.ClassExperimentMapper;
import com.example.demo.mapper.ClassMapper;
import com.example.demo.mapper.ExperimentMapper;
import com.example.demo.pojo.request.BatchAddClassRequest;
import com.example.demo.pojo.request.ClassQueryRequest;
import com.example.demo.pojo.response.BatchAddClassResponse;
import com.example.demo.pojo.response.ClassResponse;
import com.example.demo.pojo.response.ClassWithExperimentsResponse;
import com.example.demo.pojo.response.PageResponse;
import com.example.demo.pojo.entity.Class;
import com.example.demo.pojo.entity.ClassExperiment;
import com.example.demo.pojo.entity.Experiment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 班级服务
 * 提供班级的业务逻辑处理
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClassService extends ServiceImpl<ClassMapper, Class> {

    private final ClassExperimentMapper classExperimentMapper;
    private final ExperimentMapper experimentMapper;

    /**
     * 根据班级代码查询班级
     */
    public Class getByClassCode(String classCode) {
        QueryWrapper<Class> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("class_code", classCode);
        return getOne(queryWrapper);
    }

    /**
     * 生成班级编号
     * 格式: CLASS + 6位数字(如 CLASS000001)
     *
     * @return 班级编号
     */
    public String generateClassCode() {
        // 查询最大的班级编号
        QueryWrapper<Class> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("class_code");
        queryWrapper.last("LIMIT 1");
        Class lastClass = getOne(queryWrapper);

        int nextNum = 1;
        if (lastClass != null && lastClass.getClassCode() != null) {
            String lastCode = lastClass.getClassCode();
            // 提取数字部分(CLASS000001 -> 000001 -> 1)
            if (lastCode.matches("CLASS\\d{6}")) {
                String numStr = lastCode.substring(5); // 去掉 "CLASS"
                try {
                    nextNum = Integer.parseInt(numStr) + 1;
                } catch (NumberFormatException e) {
                    log.warn("解析班级编号失败: {}", lastCode);
                }
            }
        }

        // 格式化为6位数字
        return String.format("CLASS%06d", nextNum);
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
        response.setClassCode(clazz.getClassCode());
        response.setClassName(clazz.getClassName());
        response.setStudentCount(clazz.getStudentCount());
        response.setCreateTime(clazz.getCreateTime());
        response.setUpdateTime(clazz.getUpdateTime());

        // 查询班级的实验列表
        QueryWrapper<ClassExperiment> classExperimentQuery = new QueryWrapper<>();
        classExperimentQuery.eq("class_code", clazz.getClassCode());
        List<ClassExperiment> classExperiments = classExperimentMapper.selectList(classExperimentQuery);

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
            experimentInfo.setUserName(classExperiment.getUserName());

            // 查询实验名称
            QueryWrapper<Experiment> experimentQuery = new QueryWrapper<>();
            experimentQuery.eq("id", classExperiment.getExperimentId());
            Experiment experiment = experimentMapper.selectOne(experimentQuery);
            if (experiment != null) {
                experimentInfo.setExperimentName(experiment.getExperimentName());
            }

            experimentInfos.add(experimentInfo);
        }

        response.setExperiments(experimentInfos);

        return response;
    }

    /**
     * 查询班级列表（分页或列表）
     *
     * @param request 查询请求
     * @return 查询结果
     */
    public PageResponse<ClassResponse> queryClasses(ClassQueryRequest request) {
        // 构建查询条件
        QueryWrapper<Class> queryWrapper = new QueryWrapper<>();

        // 班级代码（精确查询）
        if (StringUtils.hasText(request.getClassCode())) {
            queryWrapper.eq("class_code", request.getClassCode());
        }

        // 班级名称（模糊查询）
        if (StringUtils.hasText(request.getClassName())) {
            queryWrapper.like("class_name", request.getClassName());
        }

        // 创建者（如果 Class 实体有 creator 字段）
        if (StringUtils.hasText(request.getCreator())) {
            queryWrapper.eq("creator", request.getCreator());
        }

        // 排序：按创建时间倒序
        queryWrapper.orderByDesc("create_time");

        // 判断是否分页查询
        if (Boolean.TRUE.equals(request.getPageable())) {
            // 分页查询
            Page<Class> page = new Page<>(request.getCurrent(), request.getSize());
            Page<Class> resultPage = page(page, queryWrapper);

            // 转换为响应DTO
            List<ClassResponse> records = resultPage.getRecords().stream()
                    .map(this::convertToClassResponse)
                    .collect(Collectors.toList());

            return PageResponse.of(
                    resultPage.getCurrent(),
                    resultPage.getSize(),
                    resultPage.getTotal(),
                    records
            );
        } else {
            // 列表查询
            List<Class> list = list(queryWrapper);
            List<ClassResponse> records = list.stream()
                    .map(this::convertToClassResponse)
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
     * 转换为班级响应DTO
     */
    private ClassResponse convertToClassResponse(Class clazz) {
        ClassResponse response = new ClassResponse();
        response.setClassCode(clazz.getClassCode());
        response.setClassName(clazz.getClassName());
        response.setStudentCount(clazz.getStudentCount());
        response.setCreator(clazz.getCreator());
        response.setCreateTime(clazz.getCreateTime());
        response.setUpdateTime(clazz.getUpdateTime());
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

        // 获取当前登录用户作为创建者
        String currentUsername = com.example.demo.util.SecurityUtil.getCurrentUsername().orElse(null);

        for (BatchAddClassRequest.ClassInfo classInfo : request.getClasses()) {
            BatchAddClassResponse.ClassResult result = new BatchAddClassResponse.ClassResult();

            // 确定班级编号:如果未传入则自动生成
            String classCode = classInfo.getClassCode();
            if (!StringUtils.hasText(classCode)) {
                classCode = generateClassCode();
            }

            result.setClassCode(classCode);
            result.setClassName(classInfo.getClassName());

            try {
                // 如果传入了班级编号,检查是否已存在
                if (StringUtils.hasText(classInfo.getClassCode())) {
                    Class existingClass = getByClassCode(classCode);
                    if (existingClass != null) {
                        result.setSuccess(false);
                        result.setMessage("班级编号已存在");
                        response.getFailList().add(result);
                        response.setFailCount(response.getFailCount() + 1);
                        continue;
                    }
                }

                // 创建班级
                Class clazz = new Class();
                clazz.setClassCode(classCode);
                clazz.setClassName(classInfo.getClassName());
                clazz.setStudentCount(0);
                clazz.setCreator(currentUsername);

                boolean saved = save(clazz);
                if (saved) {
                    result.setSuccess(true);
                    response.getSuccessList().add(result);
                    response.setSuccessCount(response.getSuccessCount() + 1);
                    log.info("班级 {} 添加成功", classCode);
                } else {
                    result.setSuccess(false);
                    result.setMessage("班级添加失败");
                    response.getFailList().add(result);
                    response.setFailCount(response.getFailCount() + 1);
                }
            } catch (Exception e) {
                log.error("添加班级 {} 失败", classCode, e);
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