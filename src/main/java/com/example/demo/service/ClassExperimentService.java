package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.ClassExperimentMapper;
import com.example.demo.mapper.ClassMapper;
import com.example.demo.mapper.ExperimentMapper;
import com.example.demo.pojo.request.BatchBindClassesToExperimentRequest;
import com.example.demo.pojo.response.BatchBindClassesToExperimentResponse;
import com.example.demo.pojo.response.CourseSessionResponse;
import com.example.demo.pojo.entity.Class;
import com.example.demo.pojo.entity.ClassExperiment;
import com.example.demo.pojo.entity.Experiment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 班级实验服务
 * 提供班级实验的业务逻辑处理
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClassExperimentService extends ServiceImpl<ClassExperimentMapper, ClassExperiment> {

    private final ClassMapper classMapper;
    private final ExperimentMapper experimentMapper;

    /**
     * 根据班级代码查询班级实验
     */
    public ClassExperiment getByClassCode(String classCode) {
        QueryWrapper<ClassExperiment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("class_code", classCode);
        return getOne(queryWrapper);
    }

    /**
     * 根据班级代码和实验ID查询班级实验
     */
    public ClassExperiment getByClassCodeAndExperimentId(String classCode, String experimentId) {
        QueryWrapper<ClassExperiment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("class_code", classCode)
                .eq("experiment_id", experimentId);
        return getOne(queryWrapper);
    }

    /**
     * 批量绑定班级到实验
     *
     * @param request 批量绑定班级到实验请求
     * @return 批量绑定班级到实验响应
     */
    @Transactional(rollbackFor = Exception.class)
    public BatchBindClassesToExperimentResponse batchBindClassesToExperiment(BatchBindClassesToExperimentRequest request) {
        BatchBindClassesToExperimentResponse response = new BatchBindClassesToExperimentResponse();
        response.setCourseId(request.getCourseId());
        response.setExperimentId(request.getExperimentId());
        response.setSuccessCount(0);
        response.setFailCount(0);
        response.setSuccessList(new ArrayList<>());
        response.setFailList(new ArrayList<>());

        if (request.getClassCodes() == null || request.getClassCodes().isEmpty()) {
            throw new BusinessException(400, "班级列表不能为空");
        }

        for (String classCode : request.getClassCodes()) {
            BatchBindClassesToExperimentResponse.ClassResult result = new BatchBindClassesToExperimentResponse.ClassResult();
            result.setClassCode(classCode);

            try {
                // 查询班级信息
                QueryWrapper<Class> classQuery = new QueryWrapper<>();
                classQuery.eq("class_code", classCode);
                Class clazz = classMapper.selectOne(classQuery);
                if (clazz == null) {
                    result.setSuccess(false);
                    result.setMessage("班级不存在");
                    response.getFailList().add(result);
                    response.setFailCount(response.getFailCount() + 1);
                    continue;
                }
                result.setClassName(clazz.getClassName());

                // 检查班级是否已绑定到该实验
                ClassExperiment existingClassExperiment = getByClassCodeAndExperimentId(classCode, request.getExperimentId());
                if (existingClassExperiment != null) {
                    result.setSuccess(false);
                    result.setMessage("班级已绑定到该实验");
                    response.getFailList().add(result);
                    response.setFailCount(response.getFailCount() + 1);
                    continue;
                }

                // 创建班级实验关系
                ClassExperiment classExperiment = new ClassExperiment();
                classExperiment.setClassCode(classCode);
                classExperiment.setCourseId(request.getCourseId());
                classExperiment.setExperimentId(request.getExperimentId());
                classExperiment.setCourseTime(request.getCourseTime());
                classExperiment.setStartTime(request.getStartTime());
                classExperiment.setEndTime(request.getEndTime());
                classExperiment.setExperimentLocation(request.getExperimentLocation());

                // 设置授课老师：如果传参不为空则使用传参，否则使用当前登录用户
                String userName = request.getUserName();
                if (userName == null || userName.trim().isEmpty()) {
                    userName = com.example.demo.util.SecurityUtil.getCurrentUsername().orElse(null);
                }
                classExperiment.setUserName(userName);

                boolean saved = save(classExperiment);
                if (saved) {
                    result.setSuccess(true);
                    response.getSuccessList().add(result);
                    response.setSuccessCount(response.getSuccessCount() + 1);
                    log.info("班级 {} 绑定到实验 {} 成功", classCode, request.getExperimentId());
                } else {
                    result.setSuccess(false);
                    result.setMessage("绑定失败");
                    response.getFailList().add(result);
                    response.setFailCount(response.getFailCount() + 1);
                }
            } catch (Exception e) {
                log.error("绑定班级 {} 到实验 {} 失败", classCode, request.getExperimentId(), e);
                result.setSuccess(false);
                result.setMessage(e.getMessage());
                response.getFailList().add(result);
                response.setFailCount(response.getFailCount() + 1);
            }
        }

        return response;
    }

    /**
     * 批量解绑班级
     *
     * @param experimentId 实验ID
     * @param classCodes 班级编号列表
     * @return 解绑的班级数量
     */
    @Transactional(rollbackFor = Exception.class)
    public int batchUnbindClasses(String experimentId, List<String> classCodes) {
        if (classCodes == null || classCodes.isEmpty()) {
            return 0;
        }

        int count = 0;
        for (String classCode : classCodes) {
            QueryWrapper<ClassExperiment> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("class_code", classCode)
                    .eq("experiment_id", experimentId);
            boolean removed = remove(queryWrapper);
            if (removed) {
                count++;
                log.info("班级 {} 从实验 {} 解绑成功", classCode, experimentId);
            }
        }

        return count;
    }

    /**
     * 查询学生的课次列表
     *
     * @param studentUsername 学生用户名
     @param classCodeList 学生所属的班级列表
     * @return 课次列表（按实验开始时间倒序）
     */
    public List<CourseSessionResponse> getCourseSessionsForStudent(
            String studentUsername, List<String> classCodeList) {

        if (classCodeList == null || classCodeList.isEmpty()) {
            return new ArrayList<>();
        }

        // 查询这些班级的所有课次
        QueryWrapper<ClassExperiment> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("class_code", classCodeList);
        queryWrapper.orderByDesc("start_time");
        List<ClassExperiment> classExperiments = list(queryWrapper);

        // 构建响应列表
        return classExperiments.stream()
                .map(this::buildCourseSessionResponse)
                .collect(Collectors.toList());
    }

    /**
     * 查询教师的课次列表
     *
     * @param teacherUsername 教师用户名
     * @return 课次列表（按实验开始时间倒序）
     */
    public List<CourseSessionResponse> getCourseSessionsForTeacher(String teacherUsername) {
        // 查询该教师授课的所有课次
        QueryWrapper<ClassExperiment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_name", teacherUsername);
        queryWrapper.orderByDesc("start_time");
        List<ClassExperiment> classExperiments = list(queryWrapper);

        // 构建响应列表
        return classExperiments.stream()
                .map(this::buildCourseSessionResponse)
                .collect(Collectors.toList());
    }

    /**
     * 构建课次响应
     *
     * @param classExperiment 班级实验实体
     * @return 课次响应
     */
    private CourseSessionResponse buildCourseSessionResponse(ClassExperiment classExperiment) {
        CourseSessionResponse response = new CourseSessionResponse();
        response.setClassExperimentId(classExperiment.getId());
        response.setClassCode(classExperiment.getClassCode());
        response.setCourseId(classExperiment.getCourseId());
        response.setExperimentId(Long.parseLong(classExperiment.getExperimentId()));
        response.setCourseTime(classExperiment.getCourseTime());
        response.setStartTime(classExperiment.getStartTime());
        response.setEndTime(classExperiment.getEndTime());
        response.setExperimentLocation(classExperiment.getExperimentLocation());
        response.setUserName(classExperiment.getUserName());

        // 查询班级名称
        QueryWrapper<Class> classQuery = new QueryWrapper<>();
        classQuery.eq("class_code", classExperiment.getClassCode());
        Class clazz = classMapper.selectOne(classQuery);
        if (clazz != null) {
            response.setClassName(clazz.getClassName());
        }

        // 查询实验名称
        Experiment experiment = experimentMapper.selectById(
            Long.parseLong(classExperiment.getExperimentId()));
        if (experiment != null) {
            response.setExperimentName(experiment.getExperimentName());
        }

        return response;
    }
}