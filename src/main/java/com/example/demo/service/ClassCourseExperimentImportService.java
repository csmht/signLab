package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.UserMapper;
import com.example.demo.pojo.entity.*;
import com.example.demo.pojo.excel.ClassCourseExperimentExcel;
import com.example.demo.pojo.response.BatchImportClassCourseExperimentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 班级课程实验导入服务
 * 提供班级、课程、实验、班级实验的批量导入业务逻辑处理
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClassCourseExperimentImportService {

    private final ClassService classService;
    private final CourseService courseService;
    private final ExperimentService experimentService;
    private final ClassExperimentService classExperimentService;
    private final ClassExperimentClassRelationService relationService;
    private final UserMapper userMapper;

    /**
     * 批量导入班级课程实验数据
     *
     * @param dataList Excel数据列表
     * @return 导入结果统计
     */
    @Transactional(rollbackFor = Exception.class)
    public BatchImportClassCourseExperimentResponse batchImport(List<ClassCourseExperimentExcel> dataList) {
        BatchImportClassCourseExperimentResponse response = new BatchImportClassCourseExperimentResponse();

        log.info("开始批量导入班级课程实验，共 {} 条数据", dataList.size());

        // ========== 第一步：处理班�� ==========
        Map<String, String> classNameToCodeMap = processClasses(dataList, response);

        // ========== 第二步：处理课程 ==========
        Map<String, String> courseNameToIdMap = processCourses(dataList, response);

        // ========== 第三步：处理实验 ==========
        Map<String, Long> experimentKeyToIdMap = processExperiments(dataList, courseNameToIdMap, response);

        // ========== 第四步：处理班级实验（课次）==========
        processClassExperiments(dataList, classNameToCodeMap, courseNameToIdMap, experimentKeyToIdMap, response);

        log.info("批量导入完成 - 班级[成功:{}/重复:{}/失败:{}] 课程[成功:{}/重复:{}/失败:{}] 实验[成功:{}/重复:{}/失败:{}] 课次[成功:{}/重复:{}/失败:{}]",
                response.getClassSuccessCount(), response.getClassDuplicateCount(), response.getClassFailCount(),
                response.getCourseSuccessCount(), response.getCourseDuplicateCount(), response.getCourseFailCount(),
                response.getExperimentSuccessCount(), response.getExperimentDuplicateCount(), response.getExperimentFailCount(),
                response.getClassExperimentSuccessCount(), response.getClassExperimentDuplicateCount(), response.getClassExperimentFailCount());

        return response;
    }

    /**
     * 处理班级数据
     */
    private Map<String, String> processClasses(List<ClassCourseExperimentExcel> dataList,
                                                BatchImportClassCourseExperimentResponse response) {
        // 提取唯一班级名称
        Set<String> uniqueClassNames = dataList.stream()
                .map(ClassCourseExperimentExcel::getClassName)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Map<String, String> classNameToCodeMap = new LinkedHashMap<>();

        for (String className : uniqueClassNames) {
            try {
                // 检查班级是否已存在
                LambdaQueryWrapper<com.example.demo.pojo.entity.Class> query = new LambdaQueryWrapper<>();
                query.eq(com.example.demo.pojo.entity.Class::getClassName, className);
                com.example.demo.pojo.entity.Class existingClass = classService.getOne(query);

                if (existingClass != null) {
                    classNameToCodeMap.put(className, existingClass.getClassCode());
                    response.setClassDuplicateCount(response.getClassDuplicateCount() + 1);
                    log.debug("班级 [{}] 已存在，使用编号: {}", className, existingClass.getClassCode());
                } else {
                    // 创建新班级
                    String classCode = classService.generateClassCode();
                    com.example.demo.pojo.entity.Class newClass = new com.example.demo.pojo.entity.Class();
                    newClass.setClassCode(classCode);
                    newClass.setClassName(className);
                    classService.save(newClass);
                    classNameToCodeMap.put(className, classCode);
                    response.setClassSuccessCount(response.getClassSuccessCount() + 1);
                    log.debug("班级 [{}] 创建成功，编号: {}", className, classCode);
                }
            } catch (Exception e) {
                log.error("处理班级 [{}] 失败", className, e);
                response.setClassFailCount(response.getClassFailCount() + 1);
                response.getErrorMessages().add("班级处理失败 [" + className + "]: 系统错误，请联系管理员");
            }
        }

        return classNameToCodeMap;
    }

    /**
     * 处理课程数据
     */
    private Map<String, String> processCourses(List<ClassCourseExperimentExcel> dataList,
                                               BatchImportClassCourseExperimentResponse response) {
        // 提取唯一课程（按课程名称+教师名称组合）
        Map<String, ClassCourseExperimentExcel> uniqueCourses = new LinkedHashMap<>();
        for (ClassCourseExperimentExcel data : dataList) {
            if (data.getCourseName() == null || data.getCourseName().trim().isEmpty()) {
                continue;
            }
            String teacherName = data.getTeacherName() != null ? data.getTeacherName().trim() : "";
            String key = data.getCourseName().trim() + "_" + teacherName;
            uniqueCourses.putIfAbsent(key, data);
        }

        Map<String, String> courseNameToIdMap = new LinkedHashMap<>();

        for (Map.Entry<String, ClassCourseExperimentExcel> entry : uniqueCourses.entrySet()) {
            ClassCourseExperimentExcel data = entry.getValue();
            String courseName = data.getCourseName().trim();
            String teacherName = data.getTeacherName() != null ? data.getTeacherName().trim() : null;

            try {
                // 根据教师名字查询教师用户名
                String teacherUsername = findTeacherUsername(teacherName);

                // 教师不存在时跳过导入并记录错误
                if (teacherUsername == null) {
                    response.setCourseFailCount(response.getCourseFailCount() + 1);
                    if (teacherName == null || teacherName.isEmpty()) {
                        response.getErrorMessages().add("课程处理失败: 教师名称为空 [" + courseName + "]");
                    } else {
                        response.getErrorMessages().add("课程处理失败: 教师不存在 [" + teacherName + "]");
                    }
                    continue;
                }

                // 检查课程是否已存在（按课程名称+教师）
                LambdaQueryWrapper<Course> query = new LambdaQueryWrapper<>();
                query.eq(Course::getCourseName, courseName);
                query.eq(Course::getTeacherUsername, teacherUsername);
                Course existingCourse = courseService.getOne(query);

                if (existingCourse != null) {
                    courseNameToIdMap.put(courseName, existingCourse.getCourseId());
                    response.setCourseDuplicateCount(response.getCourseDuplicateCount() + 1);
                    log.debug("课程 [{}] 已存在，使用ID: {}", courseName, existingCourse.getCourseId());
                } else {
                    // 创建新课程
                    String courseId = courseService.generateCourseId();
                    Course newCourse = new Course();
                    newCourse.setCourseId(courseId);
                    newCourse.setCourseName(courseName);
                    newCourse.setTeacherUsername(teacherUsername);
                    courseService.save(newCourse);
                    courseNameToIdMap.put(courseName, courseId);
                    response.setCourseSuccessCount(response.getCourseSuccessCount() + 1);
                    log.debug("课程 [{}] 创建成功，ID: {}", courseName, courseId);
                }
            } catch (Exception e) {
                log.error("处理课程 [{}] 失败", courseName, e);
                response.setCourseFailCount(response.getCourseFailCount() + 1);
                response.getErrorMessages().add("课程处理失败 [" + courseName + "]: 系统错误，请联系管理员");
            }
        }

        return courseNameToIdMap;
    }

    /**
     * 处理实验数据
     */
    private Map<String, Long> processExperiments(List<ClassCourseExperimentExcel> dataList,
                                                 Map<String, String> courseNameToIdMap,
                                                 BatchImportClassCourseExperimentResponse response) {
        // 提取唯一实验（按课程ID+实验名称组合）
        Map<String, ClassCourseExperimentExcel> uniqueExperiments = new LinkedHashMap<>();
        for (ClassCourseExperimentExcel data : dataList) {
            if (data.getCourseName() == null || data.getExperimentName() == null) {
                continue;
            }
            String courseId = courseNameToIdMap.get(data.getCourseName().trim());
            if (courseId == null) {
                continue;
            }
            String key = courseId + "_" + data.getExperimentName().trim();
            uniqueExperiments.putIfAbsent(key, data);
        }

        Map<String, Long> experimentKeyToIdMap = new LinkedHashMap<>();

        for (Map.Entry<String, ClassCourseExperimentExcel> entry : uniqueExperiments.entrySet()) {
            String key = entry.getKey();
            ClassCourseExperimentExcel data = entry.getValue();
            String courseId = courseNameToIdMap.get(data.getCourseName().trim());
            String experimentName = data.getExperimentName().trim();

            try {
                // 检查实验是否已存在
                LambdaQueryWrapper<Experiment> query = new LambdaQueryWrapper<>();
                query.eq(Experiment::getCourseId, courseId);
                query.eq(Experiment::getExperimentName, experimentName);
                Experiment existingExperiment = experimentService.getOne(query);

                if (existingExperiment != null) {
                    experimentKeyToIdMap.put(key, existingExperiment.getId());
                    response.setExperimentDuplicateCount(response.getExperimentDuplicateCount() + 1);
                    log.debug("实验 [{}] 已存在，ID: {}", experimentName, existingExperiment.getId());
                } else {
                    // 创建新实验
                    Experiment newExperiment = new Experiment();
                    newExperiment.setCourseId(courseId);
                    newExperiment.setExperimentName(experimentName);
                    newExperiment.setPercentage(0);
                    newExperiment.setIsDeleted(false);
                    experimentService.save(newExperiment);
                    experimentKeyToIdMap.put(key, newExperiment.getId());
                    response.setExperimentSuccessCount(response.getExperimentSuccessCount() + 1);
                    log.debug("实验 [{}] 创建成功，ID: {}", experimentName, newExperiment.getId());
                }
            } catch (Exception e) {
                log.error("处理实验 [{}] 失败", experimentName, e);
                response.setExperimentFailCount(response.getExperimentFailCount() + 1);
                response.getErrorMessages().add("实验处理失败 [" + experimentName + "]: 系统错误，请联系管理员");
            }
        }

        return experimentKeyToIdMap;
    }

    /**
     * 处理班级实验（课次）数据
     */
    private void processClassExperiments(List<ClassCourseExperimentExcel> dataList,
                                         Map<String, String> classNameToCodeMap,
                                         Map<String, String> courseNameToIdMap,
                                         Map<String, Long> experimentKeyToIdMap,
                                         BatchImportClassCourseExperimentResponse response) {
        // 按 课程+实验+日期+时间 分组，支持合班上课
        Map<String, List<ClassCourseExperimentExcel>> groupedData = dataList.stream()
                .filter(data -> data.getCourseName() != null && data.getExperimentName() != null
                        && data.getData() != null && data.getTime() != null)
                .collect(Collectors.groupingBy(data ->
                        data.getCourseName().trim() + "_" +
                        data.getExperimentName().trim() + "_" +
                        data.getData().trim() + "_" +
                        data.getTime().trim(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        for (Map.Entry<String, List<ClassCourseExperimentExcel>> entry : groupedData.entrySet()) {
            List<ClassCourseExperimentExcel> group = entry.getValue();
            ClassCourseExperimentExcel first = group.get(0);

            try {
                String courseId = courseNameToIdMap.get(first.getCourseName().trim());
                if (courseId == null) {
                    response.setClassExperimentFailCount(response.getClassExperimentFailCount() + 1);
                    response.getErrorMessages().add("课次处理失败: 课程不存在 [" + first.getCourseName() + "]");
                    continue;
                }

                String experimentKey = courseId + "_" + first.getExperimentName().trim();
                Long experimentId = experimentKeyToIdMap.get(experimentKey);
                if (experimentId == null) {
                    response.setClassExperimentFailCount(response.getClassExperimentFailCount() + 1);
                    response.getErrorMessages().add("课次处理失败: 实验不存在 [" + first.getExperimentName() + "]");
                    continue;
                }

                // 解析日期和时间
                LocalDateTime startTime = parseStartTime(first.getData(), first.getTime());

                // 组装实验地点
                String experimentLocation = buildExperimentLocation(first.getLaboratory(), first.getLaboratory2());

                // 获取教师用户名
                String teacherUsername = findTeacherUsername(first.getTeacherName());

                // 教师不存在时跳过导入并记录错误
                if (teacherUsername == null) {
                    response.setClassExperimentFailCount(response.getClassExperimentFailCount() + 1);
                    String teacherName = first.getTeacherName();
                    if (teacherName == null || teacherName.trim().isEmpty()) {
                        response.getErrorMessages().add("课次处理失败: 教师名称为空 [课程: " + first.getCourseName() + "]");
                    } else {
                        response.getErrorMessages().add("课次处理失败: 教师不存在 [" + teacherName + "]");
                    }
                    continue;
                }

                // 收集所有班级编号
                List<String> classCodes = group.stream()
                        .map(data -> classNameToCodeMap.get(data.getClassName().trim()))
                        .filter(Objects::nonNull)
                        .distinct()
                        .collect(Collectors.toList());

                if (classCodes.isEmpty()) {
                    response.setClassExperimentFailCount(response.getClassExperimentFailCount() + 1);
                    response.getErrorMessages().add("课次处理失败: 没有有效的班级");
                    continue;
                }

                // 检查是否已存在相同的课次
                if (isClassExperimentExists(courseId, experimentId, startTime, classCodes)) {
                    response.setClassExperimentDuplicateCount(response.getClassExperimentDuplicateCount() + 1);
                    log.debug("课次已存在: 课程={}, 实验={}, 时间={}, 班级={}",
                            courseId, experimentId, startTime, classCodes);
                    continue;
                }

                // 创建班级实验
                ClassExperiment classExperiment = new ClassExperiment();
                classExperiment.setCourseId(courseId);
                classExperiment.setExperimentId(String.valueOf(experimentId));
                classExperiment.setCourseTime(formatCourseTime(first.getData(), first.getTime()));
                classExperiment.setStartTime(startTime);
                classExperiment.setEndTime(null); // endTime 设为与 startTime 相同
                classExperiment.setExperimentLocation(experimentLocation);
                classExperiment.setUserName(teacherUsername);

                classExperimentService.save(classExperiment);

                // 绑定班级关系
                relationService.batchBindClassesToExperiment(classExperiment.getId(), classCodes);

                response.setClassExperimentSuccessCount(response.getClassExperimentSuccessCount() + 1);
                log.debug("课次创建成功: ID={}, 班级={}", classExperiment.getId(), classCodes);

            } catch (Exception e) {
                log.error("处理课次失��", e);
                response.setClassExperimentFailCount(response.getClassExperimentFailCount() + 1);
                response.getErrorMessages().add("课次处理失败: 系统错误，请联系管理员");
            }
        }
    }

    /**
     * 根据教师名字查询教师用户名
     */
    private String findTeacherUsername(String teacherName) {
        if (teacherName == null || teacherName.trim().isEmpty()) {
            return null;
        }
        LambdaQueryWrapper<User> query = new LambdaQueryWrapper<>();
        query.eq(User::getName, teacherName.trim());
        query.eq(User::getRole, "teacher");
        User teacher = userMapper.selectOne(query);
        return teacher != null ? teacher.getUsername() : null;
    }

    /**
     * 解析开始时间
     * 日期格式: YYYY-MM-dd（例如：2024-03-15）
     * 时间格式: H:mm-H:mm（例如：8:00-10:00）
     * 只解析开始时间，不解析结束时间
     */
    private LocalDateTime parseStartTime(String dateStr, String timeStr) {
        try {
            // 标准化日期
            String normalizedDate = dateStr.trim().replace("/", "-");

            // 解析时间段 "8:00-10:00"，取开始时间
            String startTimePart = timeStr.split("-")[0].trim();

            // 补齐小时位 "8:00" -> "08:00"
            startTimePart = padHour(startTimePart);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            return LocalDateTime.parse(normalizedDate + " " + startTimePart, formatter);
        } catch (Exception e) {
            throw new BusinessException(400, "日期时间格式错误: " + dateStr + " " + timeStr);
        }
    }

    /**
     * 补齐小时位
     * "8:00" -> "08:00"
     * "10:00" -> "10:00"
     */
    private String padHour(String time) {
        String[] parts = time.split(":");
        if (parts.length == 2) {
            String hour = parts[0].trim();
            String minute = parts[1].trim();
            if (hour.length() == 1) {
                hour = "0" + hour;
            }
            return hour + ":" + minute;
        }
        return time;
    }

    /**
     * 组装实验地点
     * 拼接规则: 实验楼 + "-" + 实验室
     */
    private String buildExperimentLocation(String laboratory, String laboratory2) {
        boolean hasLab1 = laboratory != null && !laboratory.trim().isEmpty();
        boolean hasLab2 = laboratory2 != null && !laboratory2.trim().isEmpty();

        if (hasLab1 && hasLab2) {
            return laboratory.trim() + "-" + laboratory2.trim();
        } else if (hasLab1) {
            return laboratory.trim();
        } else if (hasLab2) {
            return laboratory2.trim();
        }
        return "";
    }

    /**
     * 检查是否已存在相同的课次
     */
    private boolean isClassExperimentExists(String courseId, Long experimentId,
                                            LocalDateTime startTime, List<String> classCodes) {
        // 查询相同课程、实验、时间的课次
        LambdaQueryWrapper<ClassExperiment> query = new LambdaQueryWrapper<>();
        query.eq(ClassExperiment::getCourseId, courseId);
        query.eq(ClassExperiment::getExperimentId, String.valueOf(experimentId));
        query.eq(ClassExperiment::getStartTime, startTime);

        List<ClassExperiment> existingExperiments = classExperimentService.list(query);

        if (existingExperiments.isEmpty()) {
            return false;
        }

        // 检查是否有完全相同班级组合的课次
        for (ClassExperiment ce : existingExperiments) {
            List<String> existingClassCodes = relationService.getClassCodesByExperimentId(ce.getId());
            if (new HashSet<>(existingClassCodes).equals(new HashSet<>(classCodes))) {
                return true;
            }
        }

        return false;
    }

    /**
     * 格式化课程时间
     * 输入格式：日期 "2024-03-15"，时间 "8:00-10:00"
     * 输出格式："2024/03/15 08:00 - 10:00"
     */
    private String formatCourseTime(String dateStr, String timeStr) {
        try {
            // 标准化日期：将 "-" 替换为 "/"
            String normalizedDate = dateStr.trim().replace("-", "/");

            // 解析时间段 "8:00-10:00"
            String[] timeParts = timeStr.split("-");
            String startTime = padHour(timeParts[0].trim());
            String endTime = timeParts.length > 1 ? padHour(timeParts[1].trim()) : startTime;

            return normalizedDate + " " + startTime + " - " + endTime;
        } catch (Exception e) {
            // 解析失败时返回原始值
            return dateStr + " " + timeStr;
        }
    }
}
