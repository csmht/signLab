package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.ClassExperimentMapper;
import com.example.demo.mapper.ClassMapper;
import com.example.demo.mapper.ExperimentMapper;
import com.example.demo.mapper.StudentClassRelationMapper;
import com.example.demo.pojo.entity.StudentClassRelation;
import com.example.demo.pojo.request.BatchBindClassesToExperimentRequest;
import com.example.demo.pojo.request.CourseSessionQueryRequest;
import com.example.demo.pojo.response.BatchBindClassesToExperimentResponse;
import com.example.demo.pojo.response.CourseSessionResponse;
import com.example.demo.pojo.response.PageResponse;
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
    private final StudentClassRelationMapper studentClassRelationMapper;
    private final ClassExperimentClassRelationService classExperimentClassRelationService;

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

        // 检查所有班级是否存在
        List<BatchBindClassesToExperimentResponse.ClassResult> validClassResults = new ArrayList<>();
        for (String classCode : request.getClassCodes()) {
            BatchBindClassesToExperimentResponse.ClassResult result =
                new BatchBindClassesToExperimentResponse.ClassResult();
            result.setClassCode(classCode);

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
            validClassResults.add(result);
        }

        if (validClassResults.isEmpty()) {
            throw new BusinessException(400, "没有有效的班级可以绑定");
        }

        try {
            // 创建一个 ClassExperiment 记录（合班上课）
            ClassExperiment classExperiment = new ClassExperiment();
            // 不再设置 classCode
            classExperiment.setCourseId(request.getCourseId());
            classExperiment.setExperimentId(request.getExperimentId());
            classExperiment.setCourseTime(request.getCourseTime());
            classExperiment.setStartTime(request.getStartTime());
            classExperiment.setEndTime(request.getEndTime());
            classExperiment.setExperimentLocation(request.getExperimentLocation());

            String userName = request.getUserName();
            if (userName == null || userName.trim().isEmpty()) {
                userName = com.example.demo.util.SecurityUtil.getCurrentUsername().orElse(null);
            }
            classExperiment.setUserName(userName);

            boolean saved = save(classExperiment);
            if (saved) {
                // 批量绑定班级
                List<String> validClassCodes = validClassResults.stream()
                        .map(e -> e.getClassCode())
                        .collect(java.util.stream.Collectors.toList());

                classExperimentClassRelationService.batchBindClassesToExperiment(
                        classExperiment.getId(), validClassCodes);

                // 所有班级都成功
                for (BatchBindClassesToExperimentResponse.ClassResult result : validClassResults) {
                    result.setSuccess(true);
                    response.getSuccessList().add(result);
                }
                response.setSuccessCount(validClassResults.size());
                log.info("创建合班上课实验课次 {} 成功，绑定班级: {}",
                    classExperiment.getId(), validClassCodes);
            } else {
                for (BatchBindClassesToExperimentResponse.ClassResult result : validClassResults) {
                    result.setSuccess(false);
                    result.setMessage("创建实验课次失败");
                    response.getFailList().add(result);
                }
                response.setFailCount(validClassResults.size());
            }
        } catch (Exception e) {
            log.error("批量绑定班级到实验失败", e);
            for (BatchBindClassesToExperimentResponse.ClassResult result : validClassResults) {
                result.setSuccess(false);
                result.setMessage(e.getMessage());
                response.getFailList().add(result);
            }
            response.setFailCount(validClassResults.size());
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
     * 查询学生的课次列表（分页）
     *
     * @param studentUsername 学生用户名
     * @param classCodeList   学生所属的班级列表
     * @param request         查询请求
     * @return 课次列表（按实验开始时间倒序）
     */
    public PageResponse<CourseSessionResponse> getCourseSessionsForStudent(
            String studentUsername, List<String> classCodeList, CourseSessionQueryRequest request) {

        if (classCodeList == null || classCodeList.isEmpty()) {
            return PageResponse.of(1L,  request.getSize(), 0L, new ArrayList<>());
        }

        // 通过关联表查询学生所属班级对应的所有班级实验ID
        List<Long> experimentIds = classExperimentClassRelationService.getExperimentIdsByClassCodes(classCodeList);

        if (experimentIds.isEmpty()) {
            return PageResponse.of(1L,  request.getSize(), 0L, new ArrayList<>());
        }

        // 构建查询条件
        QueryWrapper<ClassExperiment> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", experimentIds);

        // 支持按班级编号过滤
        if (request.getClassCode() != null && !request.getClassCode().trim().isEmpty()) {
            List<Long> filteredExperimentIds =
                classExperimentClassRelationService.getExperimentIdsByClassCode(request.getClassCode().trim());
            queryWrapper.in("id", filteredExperimentIds);
        }

        // 支持按课程ID过滤
        if (request.getCourseId() != null && !request.getCourseId().trim().isEmpty()) {
            queryWrapper.eq("course_id", request.getCourseId().trim());
        }

        if(studentUsername != null && !studentUsername.trim().isEmpty()) {
            List<String> list = studentClassRelationMapper.selectList(new QueryWrapper<StudentClassRelation>().eq("student_id", studentUsername)).stream().map(StudentClassRelation::getClassCode).toList();
            List<Long> actualExperimentIds =
                classExperimentClassRelationService.getExperimentIdsByClassCodes(list);
            queryWrapper.in("id", actualExperimentIds);
        }

        queryWrapper.orderByDesc("start_time");

        // 分页查询
        if (request.getPageable() != null && request.getPageable()) {
            Page<ClassExperiment> page = new Page<>(request.getCurrent(), request.getSize());
            Page<ClassExperiment> result = page(page, queryWrapper);

            // 构建响应列表
            List<CourseSessionResponse> records = result.getRecords().stream()
                    .map(this::buildCourseSessionResponse)
                    .collect(Collectors.toList());

            return PageResponse.of(
                    result.getCurrent(),
                    result.getSize(),
                    result.getTotal(),
                    records
            );
        } else {
            // 不分页，返回全部数据
            List<ClassExperiment> classExperiments = list(queryWrapper);
            List<CourseSessionResponse> records = classExperiments.stream()
                    .map(this::buildCourseSessionResponse)
                    .collect(Collectors.toList());

            return PageResponse.of(1L, (long) records.size(), (long) records.size(), records);
        }
    }

    /**
     * 查询教师的课次列表（分页）
     *
     * @param teacherUsername 教师用户名
     * @param request         查询请求
     * @return 课次列表（按实验开始时间倒序）
     */
    public PageResponse<CourseSessionResponse> getCourseSessionsForTeacher(
            String teacherUsername, CourseSessionQueryRequest request) {

        // 构建查询条件
        QueryWrapper<ClassExperiment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_name", teacherUsername);

        // 支持按班级编号过滤
        if (request.getClassCode() != null && !request.getClassCode().trim().isEmpty()) {
            List<Long> filteredExperimentIds =
                classExperimentClassRelationService.getExperimentIdsByClassCode(request.getClassCode().trim());
            queryWrapper.in("id", filteredExperimentIds);
        }

        // 支持按课程ID过滤
        if (request.getCourseId() != null && !request.getCourseId().trim().isEmpty()) {
            queryWrapper.eq("course_id", request.getCourseId().trim());
        }

        queryWrapper.orderByDesc("start_time");

        // 分页查询
        if (request.getPageable() != null && request.getPageable()) {
            Page<ClassExperiment> page = new Page<>(request.getCurrent(), request.getSize());
            Page<ClassExperiment> result = page(page, queryWrapper);

            // 构建响应列表
            List<CourseSessionResponse> records = result.getRecords().stream()
                    .map(this::buildCourseSessionResponse)
                    .collect(Collectors.toList());

            return PageResponse.of(
                    result.getCurrent(),
                    result.getSize(),
                    result.getTotal(),
                    records
            );
        } else {
            // 不分页，返回全部数据
            List<ClassExperiment> classExperiments = list(queryWrapper);
            List<CourseSessionResponse> records = classExperiments.stream()
                    .map(this::buildCourseSessionResponse)
                    .collect(Collectors.toList());

            return PageResponse.of(1L, (long) records.size(), (long) records.size(), records);
        }
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
        response.setCourseId(classExperiment.getCourseId());
        response.setExperimentId(Long.parseLong(classExperiment.getExperimentId()));
        response.setCourseTime(classExperiment.getCourseTime());
        response.setStartTime(classExperiment.getStartTime());
        response.setEndTime(classExperiment.getEndTime());
        response.setExperimentLocation(classExperiment.getExperimentLocation());
        response.setUserName(classExperiment.getUserName());

        // 查询关联的班级列表
        List<String> classCodes =
            classExperimentClassRelationService.getClassCodesByExperimentId(classExperiment.getId());

        response.setClassCodes(classCodes);
        response.setIsMergedClass(classCodes.size() > 1);

        // 查询班级名称
        List<String> classNames = new ArrayList<>();
        for (String code : classCodes) {
            QueryWrapper<Class> classQuery = new QueryWrapper<>();
            classQuery.eq("class_code", code);
            Class clazz = classMapper.selectOne(classQuery);
            if (clazz != null) {
                classNames.add(clazz.getClassName());
            }
        }
        response.setClassNames(classNames);

        // 主班级（兼容单班级场景）
        if (!classCodes.isEmpty()) {
            response.setClassCode(classCodes.get(0));
            if (!classNames.isEmpty()) {
                response.setClassName(classNames.get(0));
            }
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