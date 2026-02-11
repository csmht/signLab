package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.ClassExperimentMapper;
import com.example.demo.mapper.ClassMapper;
import com.example.demo.mapper.CourseMapper;
import com.example.demo.mapper.ExperimentMapper;
import com.example.demo.mapper.StudentClassRelationMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.pojo.entity.StudentClassRelation;
import com.example.demo.pojo.request.BatchBindClassesToExperimentRequest;
import com.example.demo.pojo.request.ClassExperimentQueryRequest;
import com.example.demo.pojo.request.CourseSessionQueryRequest;
import com.example.demo.pojo.response.BatchBindClassesToExperimentResponse;
import com.example.demo.pojo.response.ClassExperimentDetailResponse;
import com.example.demo.pojo.response.ClassExperimentMapResponse;
import com.example.demo.pojo.response.CourseExperimentsDetail;
import com.example.demo.pojo.response.CourseSessionResponse;
import com.example.demo.pojo.response.PageResponse;
import com.example.demo.pojo.entity.Class;
import com.example.demo.pojo.entity.ClassExperiment;
import com.example.demo.pojo.entity.Course;
import com.example.demo.pojo.entity.Experiment;
import com.example.demo.pojo.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
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
    private final CourseMapper courseMapper;
    private final UserMapper userMapper;

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

            LambdaQueryWrapper<Class> classQuery = new LambdaQueryWrapper<>();
            classQuery.eq(Class::getClassCode, classCode);
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
            LambdaQueryWrapper<ClassExperiment> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ClassExperiment::getExperimentId, experimentId);
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
        LambdaQueryWrapper<ClassExperiment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(ClassExperiment::getId, experimentIds);

        // 支持按班级编号过滤
        if (request.getClassCode() != null && !request.getClassCode().trim().isEmpty()) {
            List<Long> filteredExperimentIds =
                classExperimentClassRelationService.getExperimentIdsByClassCode(request.getClassCode().trim());
            queryWrapper.in(ClassExperiment::getId, filteredExperimentIds);
        }

        // 支持按课程ID过滤
        if (request.getCourseId() != null && !request.getCourseId().trim().isEmpty()) {
            queryWrapper.eq(ClassExperiment::getCourseId, request.getCourseId().trim());
        }

        if(studentUsername != null && !studentUsername.trim().isEmpty()) {
            List<String> list = studentClassRelationMapper.selectList(new LambdaQueryWrapper<StudentClassRelation>().eq(StudentClassRelation::getStudentUsername, studentUsername)).stream().map(StudentClassRelation::getClassCode).toList();
            List<Long> actualExperimentIds =
                classExperimentClassRelationService.getExperimentIdsByClassCodes(list);
            queryWrapper.in(ClassExperiment::getId, actualExperimentIds);
        }

        queryWrapper.orderByDesc(ClassExperiment::getStartTime);

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
        LambdaQueryWrapper<ClassExperiment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ClassExperiment::getUserName, teacherUsername);

        // 支持按班级编号过滤
        if (request.getClassCode() != null && !request.getClassCode().trim().isEmpty()) {
            List<Long> filteredExperimentIds =
                classExperimentClassRelationService.getExperimentIdsByClassCode(request.getClassCode().trim());
            queryWrapper.in(ClassExperiment::getId, filteredExperimentIds);
        }

        // 支持按课程ID过滤
        if (request.getCourseId() != null && !request.getCourseId().trim().isEmpty()) {
            queryWrapper.eq(ClassExperiment::getCourseId, request.getCourseId().trim());
        }

        queryWrapper.orderByDesc(ClassExperiment::getStartTime);

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
            LambdaQueryWrapper<Class> classQuery = new LambdaQueryWrapper<>();
            classQuery.eq(Class::getClassCode, code);
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

    /**
     * 教师查询班级实验列表（分页）
     *
     * @param teacherUsername 教师用户名
     * @param request         查询请求
     * @return 班级实验列表（按实验开始时间倒序）
     */
    public PageResponse<ClassExperimentDetailResponse> queryClassExperimentsForTeacher(
            String teacherUsername, ClassExperimentQueryRequest request) {

        // 构建查询条件
        LambdaQueryWrapper<ClassExperiment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ClassExperiment::getUserName, teacherUsername);

        // 支持按班级编号过滤
        if (request.getClassCode() != null && !request.getClassCode().trim().isEmpty()) {
            List<Long> filteredExperimentIds =
                classExperimentClassRelationService.getExperimentIdsByClassCode(request.getClassCode().trim());
            if (!filteredExperimentIds.isEmpty()) {
                queryWrapper.in(ClassExperiment::getId, filteredExperimentIds);
            } else {
                // 如果没有匹配的班级实验，返回空结果
                return PageResponse.of(1L, request.getSize(), 0L, new ArrayList<>());
            }
        }

        // 支持按课程ID过滤
        if (request.getCourseId() != null && !request.getCourseId().trim().isEmpty()) {
            queryWrapper.eq(ClassExperiment::getCourseId, request.getCourseId().trim());
        }

        // 支持按时间段过滤
        addDateTimeFilter(queryWrapper, request);

        queryWrapper.orderByDesc(ClassExperiment::getStartTime);

        // 分页查询
        if (request.getPageable() != null && request.getPageable()) {
            Page<ClassExperiment> page = new Page<>(request.getCurrent(), request.getSize());
            Page<ClassExperiment> result = page(page, queryWrapper);

            // 构建响应列表
            List<ClassExperimentDetailResponse> records = result.getRecords().stream()
                    .map(this::buildClassExperimentDetailResponse)
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
            List<ClassExperimentDetailResponse> records = classExperiments.stream()
                    .map(this::buildClassExperimentDetailResponse)
                    .collect(Collectors.toList());

            return PageResponse.of(1L, (long) records.size(), (long) records.size(), records);
        }
    }

    /**
     * 学生查询班级实验列表（分页）
     *
     * @param classCodeList 学生所属的班级列表
     * @param request       查询请求
     * @return 班级实验列表（按实验开始时间倒序）
     */
    public PageResponse<ClassExperimentDetailResponse> queryClassExperimentsForStudent(
            List<String> classCodeList, ClassExperimentQueryRequest request) {

        if (classCodeList == null || classCodeList.isEmpty()) {
            return PageResponse.of(1L, request.getSize(), 0L, new ArrayList<>());
        }

        // 通过关联表查询学生所属班级对应的所有班级实验ID
        List<Long> experimentIds = classExperimentClassRelationService.getExperimentIdsByClassCodes(classCodeList);

        if (experimentIds.isEmpty()) {
            return PageResponse.of(1L, request.getSize(), 0L, new ArrayList<>());
        }

        // 构建查询条件
        LambdaQueryWrapper<ClassExperiment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(ClassExperiment::getId, experimentIds);

        // 支持按班级编号过滤
        if (request.getClassCode() != null && !request.getClassCode().trim().isEmpty()) {
            List<Long> filteredExperimentIds =
                classExperimentClassRelationService.getExperimentIdsByClassCode(request.getClassCode().trim());
            if (!filteredExperimentIds.isEmpty()) {
                queryWrapper.in(ClassExperiment::getId, filteredExperimentIds);
            } else {
                return PageResponse.of(1L, request.getSize(), 0L, new ArrayList<>());
            }
        }

        // 支持按课程ID过滤
        if (request.getCourseId() != null && !request.getCourseId().trim().isEmpty()) {
            queryWrapper.eq(ClassExperiment::getCourseId, request.getCourseId().trim());
        }

        // 支持按时间段过滤
        addDateTimeFilter(queryWrapper, request);

        queryWrapper.orderByDesc(ClassExperiment::getStartTime);

        // 分页查询
        if (request.getPageable() != null && request.getPageable()) {
            Page<ClassExperiment> page = new Page<>(request.getCurrent(), request.getSize());
            Page<ClassExperiment> result = page(page, queryWrapper);

            // 构建响应列表
            List<ClassExperimentDetailResponse> records = result.getRecords().stream()
                    .map(this::buildClassExperimentDetailResponse)
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
            List<ClassExperimentDetailResponse> records = classExperiments.stream()
                    .map(this::buildClassExperimentDetailResponse)
                    .collect(Collectors.toList());

            return PageResponse.of(1L, (long) records.size(), (long) records.size(), records);
        }
    }

    /**
     * 添加时间段过滤条件
     *
     * @param queryWrapper 查询条件包装器
     * @param request      查询请求
     */
    private void addDateTimeFilter(LambdaQueryWrapper<ClassExperiment> queryWrapper,
                                   ClassExperimentQueryRequest request) {
        if (request.getStartDate() != null && !request.getStartDate().trim().isEmpty()) {
            try {
                LocalDateTime startDateTime = LocalDateTime.parse(
                    request.getStartDate() + " 00:00:00",
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                );
                queryWrapper.ge(ClassExperiment::getStartTime, startDateTime);
            } catch (Exception e) {
                log.warn("startDate 格式错误: {}", request.getStartDate());
            }
        }
        if (request.getEndDate() != null && !request.getEndDate().trim().isEmpty()) {
            try {
                LocalDateTime endDateTime = LocalDateTime.parse(
                    request.getEndDate() + " 23:59:59",
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                );
                queryWrapper.le(ClassExperiment::getStartTime, endDateTime);
            } catch (Exception e) {
                log.warn("endDate 格式错误: {}", request.getEndDate());
            }
        }
    }

    /**
     * 构建班级实验详情响应
     *
     * @param classExperiment 班级实验实体
     * @return 班级实验详情响应
     */
    private ClassExperimentDetailResponse buildClassExperimentDetailResponse(ClassExperiment classExperiment) {
        ClassExperimentDetailResponse response = new ClassExperimentDetailResponse();
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
            LambdaQueryWrapper<Class> classQuery = new LambdaQueryWrapper<>();
            classQuery.eq(Class::getClassCode, code);
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

        // 查询课程名称
        Course course = courseMapper.selectOne(
            new LambdaQueryWrapper<Course>().eq(Course::getCourseId, classExperiment.getCourseId())
        );
        if (course != null) {
            response.setCourseName(course.getCourseName());
        }

        // 查询教师姓名
        User teacher = userMapper.selectOne(
            new LambdaQueryWrapper<User>().eq(User::getUsername, classExperiment.getUserName())
        );
        if (teacher != null) {
            response.setTeacherName(teacher.getName());
        }

        return response;
    }

    /**
     * 根据班级代码查询该班级关联的所有实验详情（按课程分组）
     *
     * @param classCode 班级代码
     * @return 按课程分组的实验详情
     */
    public ClassExperimentMapResponse getClassExperimentsGroupByCourse(String classCode) {
        ClassExperimentMapResponse response = new ClassExperimentMapResponse();
        response.setCourseExperiments(new LinkedHashMap<>());

        // 1. 获取班级关联的所有班级实验ID
        List<Long> classExperimentIds =
            classExperimentClassRelationService.getExperimentIdsByClassCode(classCode);

        if (classExperimentIds.isEmpty()) {
            return response;
        }

        // 2. 批量查询班级实验记录
        List<ClassExperiment> classExperiments = listByIds(classExperimentIds);

        if (classExperiments.isEmpty()) {
            return response;
        }

        // 3. 提取所有courseId并批量查询课程信息
        List<String> courseIds = classExperiments.stream()
                .map(ClassExperiment::getCourseId)
                .distinct()
                .collect(Collectors.toList());

        Map<String, Course> courseMap = courseMapper.selectList(
                new LambdaQueryWrapper<Course>().in(Course::getCourseId, courseIds)
        ).stream().collect(Collectors.toMap(Course::getCourseId, Function.identity()));

        // 4. 提取所有experimentId并批量查询实验信息
        List<Long> experimentIds = classExperiments.stream()
                .map(ce -> Long.parseLong(ce.getExperimentId()))
                .distinct()
                .collect(Collectors.toList());

        Map<Long, Experiment> experimentMap = experimentMapper.selectList(
                new LambdaQueryWrapper<Experiment>().in(Experiment::getId, experimentIds)
        ).stream().collect(Collectors.toMap(Experiment::getId, Function.identity()));

        // 5. 提取所有教师用户名并批量查询教师信息
        List<String> teacherUsernames = classExperiments.stream()
                .map(ClassExperiment::getUserName)
                .filter(username -> username != null && !username.trim().isEmpty())
                .distinct()
                .collect(Collectors.toList());

        final Map<String, User> teacherMap;
        if (!teacherUsernames.isEmpty()) {
            teacherMap = userMapper.selectList(
                    new LambdaQueryWrapper<User>().in(User::getUsername, teacherUsernames)
            ).stream().collect(Collectors.toMap(User::getUsername, Function.identity()));
        } else {
            teacherMap = new LinkedHashMap<>();
        }

        // 6. 按courseId分组组装响应数据
        Map<String, List<ClassExperiment>> groupedByCourse = classExperiments.stream()
                .collect(Collectors.groupingBy(ClassExperiment::getCourseId));

        for (Map.Entry<String, List<ClassExperiment>> entry : groupedByCourse.entrySet()) {
            String courseId = entry.getKey();
            List<ClassExperiment> experimentsForCourse = entry.getValue();

            CourseExperimentsDetail detail = new CourseExperimentsDetail();

            // 设置课程信息
            CourseExperimentsDetail.CourseInfo courseInfo = new CourseExperimentsDetail.CourseInfo();
            Course course = courseMap.get(courseId);
            if (course != null) {
                courseInfo.setCourseId(course.getCourseId());
                courseInfo.setCourseName(course.getCourseName());
                courseInfo.setTeacherUsername(course.getTeacherUsername());
            }
            detail.setCourseInfo(courseInfo);

            // 设置实验列表
            List<CourseExperimentsDetail.ExperimentDetailItem> experimentItems =
                experimentsForCourse.stream()
                    .map(ce -> buildExperimentDetailItem(ce, experimentMap, teacherMap))
                    .collect(Collectors.toList());
            detail.setExperiments(experimentItems);

            response.getCourseExperiments().put(courseId, detail);
        }

        return response;
    }

    /**
     * 构建实验详情项
     *
     * @param classExperiment 班级实验实体
     * @param experimentMap 实验信息Map
     * @param teacherMap 教师信息Map
     * @return 实验详情项
     */
    private CourseExperimentsDetail.ExperimentDetailItem buildExperimentDetailItem(
            ClassExperiment classExperiment,
            Map<Long, Experiment> experimentMap,
            Map<String, User> teacherMap) {

        CourseExperimentsDetail.ExperimentDetailItem item =
            new CourseExperimentsDetail.ExperimentDetailItem();

        item.setClassExperimentId(classExperiment.getId());
        item.setExperimentId(Long.parseLong(classExperiment.getExperimentId()));
        item.setCourseTime(classExperiment.getCourseTime());
        item.setStartTime(classExperiment.getStartTime());
        item.setEndTime(classExperiment.getEndTime());
        item.setExperimentLocation(classExperiment.getExperimentLocation());
        item.setUserName(classExperiment.getUserName());

        // 查询实验详细信息
        Experiment experiment = experimentMap.get(Long.parseLong(classExperiment.getExperimentId()));
        if (experiment != null) {
            item.setExperimentName(experiment.getExperimentName());
            item.setPercentage(experiment.getPercentage());
        }

        // 查询教师姓名
        if (classExperiment.getUserName() != null) {
            User teacher = teacherMap.get(classExperiment.getUserName());
            if (teacher != null) {
                item.setTeacherName(teacher.getName());
            }
        }

        return item;
    }
}