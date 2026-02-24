package com.example.demo.service;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demo.exception.BusinessException;
import com.example.demo.pojo.dto.mapvo.ExperimentResultItem;
import com.example.demo.mapper.ClassMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.pojo.entity.AttendanceRecord;
import com.example.demo.pojo.entity.Class;
import com.example.demo.pojo.entity.CourseGrade;
import com.example.demo.pojo.entity.Experiment;
import com.example.demo.pojo.entity.StudentClassRelation;
import com.example.demo.pojo.entity.User;
import com.example.demo.pojo.excel.AttendanceRecordExportExcel;
import com.example.demo.pojo.excel.CourseGradeExportExcel;
import com.example.demo.pojo.vo.CourseGradeResult;
import com.example.demo.pojo.vo.ExperimentGradeResult;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 数据导出服务
 * 提供各类数据的导出功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataExportService {

    private final CourseGradeService courseGradeService;
    private final AttendanceRecordService attendanceRecordService;
    private final UserMapper userMapper;
    private final ClassMapper classMapper;
    private final ExperimentService experimentService;
    private final StudentClassRelationService studentClassRelationService;
    private final GradeCalculationService gradeCalculationService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 导出课程成绩
     *
     * @param courseId 课程ID
     * @param semester 学期（可选）
     * @return 成绩导出数据列表
     */
    public List<CourseGradeExportExcel> exportCourseGrades(String courseId, String semester) {
        LambdaQueryWrapper<CourseGrade> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseGrade::getCourseId, courseId);

        if (semester != null && !semester.trim().isEmpty()) {
            queryWrapper.eq(CourseGrade::getSemester, semester);
        }

        queryWrapper.orderByDesc(CourseGrade::getGradeTime);

        List<CourseGrade> grades = courseGradeService.list(queryWrapper);

        return grades.stream().map(grade -> {
            CourseGradeExportExcel excel = new CourseGradeExportExcel();
            excel.setStudentUsername(grade.getStudentUsername());
            excel.setCourseId(grade.getCourseId());
            excel.setGrade(grade.getGrade());
            excel.setGradeNumeric(grade.getGradeNumeric());
            excel.setGradeType(grade.getGradeType());
            excel.setTeacherUsername(grade.getTeacherUsername());
            excel.setTeacherComment(grade.getTeacherComment());
            excel.setSemester(grade.getSemester());
            excel.setIsApproved(grade.getIsApproved() != null && grade.getIsApproved() ? "是" : "否");

            if (grade.getGradeTime() != null) {
                excel.setGradeTime(grade.getGradeTime().format(DATE_FORMATTER));
            }

            // 查询学生姓名
            User student = userMapper.selectOne(
                    new LambdaQueryWrapper<User>().eq(User::getUsername, grade.getStudentUsername())
            );
            excel.setStudentName(student != null ? student.getName() : grade.getStudentUsername());

            // 查询教师姓名
            User teacher = userMapper.selectOne(
                    new LambdaQueryWrapper<User>().eq(User::getUsername, grade.getTeacherUsername())
            );
            excel.setTeacherName(teacher != null ? teacher.getName() : grade.getTeacherUsername());

            // 查询课程名称（如果有的话）
            // 这里可以添加课程名称查询逻辑

            return excel;
        }).collect(Collectors.toList());
    }

    /**
     * 导出考勤记录
     *
     * @param courseId 课程ID
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 考勤记录导出数据列表
     */
    public List<AttendanceRecordExportExcel> exportAttendanceRecords(String courseId, String startDate, String endDate) {
        LambdaQueryWrapper<AttendanceRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AttendanceRecord::getCourseId, courseId);

        if (startDate != null && !startDate.trim().isEmpty()) {
            queryWrapper.ge(AttendanceRecord::getAttendanceTime, startDate);
        }

        if (endDate != null && !endDate.trim().isEmpty()) {
            queryWrapper.le(AttendanceRecord::getAttendanceTime, endDate);
        }

        queryWrapper.orderByDesc(AttendanceRecord::getAttendanceTime);

        List<AttendanceRecord> records = attendanceRecordService.list(queryWrapper);

        return records.stream().map(record -> {
            AttendanceRecordExportExcel excel = new AttendanceRecordExportExcel();
            excel.setStudentUsername(record.getStudentUsername());
            excel.setCourseId(record.getCourseId());
            excel.setExperimentId(record.getExperimentId());
            excel.setAttendanceStatus(getAttendanceStatusText(record.getAttendanceStatus()));
            excel.setIpAddress(record.getIpAddress());
            excel.setStudentActualClassCode(record.getStudentActualClassCode());

            if (record.getAttendanceTime() != null) {
                excel.setAttendanceTime(record.getAttendanceTime().format(DATE_FORMATTER));
            }

            // 查询学生姓名
            User student = userMapper.selectOne(
                    new LambdaQueryWrapper<User>().eq(User::getUsername, record.getStudentUsername())
            );
            excel.setStudentName(student != null ? student.getName() : record.getStudentUsername());

            return excel;
        }).collect(Collectors.toList());
    }

    /**
     * 获取考勤状态文本
     */
    private String getAttendanceStatusText(Integer status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 1:
                return "签到";
            case 2:
                return "补签";
            case 3:
                return "迟到";
            case 4:
                return "跨班签到";
            default:
                return "未知";
        }
    }

    /**
     * 获取考勤状态文本（用于导出）
     */
    private String getAttendanceStatusTextForExport(Integer status) {
        if (status == null) {
            return "未签到";
        }
        switch (status) {
            case 1:
                return "签到";
            case 2:
                return "补签";
            case 3:
                return "迟到";
            case 4:
                return "跨班签到";
            default:
                return "未签到";
        }
    }

    /**
     * 导出课程实验成绩表
     *
     * @param courseId   课程ID
     * @param classCodes 班级编号列表
     * @param response   HTTP响应
     */
    public void exportCourseExperimentGrades(String courseId, List<String> classCodes, HttpServletResponse response)
            throws IOException {
        // 1. 查询课程下所有实验
        LambdaQueryWrapper<Experiment> experimentQuery = new LambdaQueryWrapper<>();
        experimentQuery.eq(Experiment::getCourseId, courseId)
                .eq(Experiment::getIsDeleted, false)
                .orderByAsc(Experiment::getCreatedTime);
        List<Experiment> experiments = experimentService.list(experimentQuery);

        if (experiments.isEmpty()) {
            throw new BusinessException(404, "该课程下没有实验");
        }

        // 2. 查询指定班级的所有学生
        List<StudentInfo> studentList = getStudentsByClassCodes(classCodes);

        if (studentList.isEmpty()) {
            throw new BusinessException(404, "指定班级下没有学生");
        }

        // 3. 构建表头
        List<List<String>> heads = new ArrayList<>();
        heads.add(List.of("班级"));
        heads.add(List.of("用户名"));
        heads.add(List.of("姓名"));
        for (Experiment exp : experiments) {
            heads.add(List.of(exp.getExperimentName()));
        }
        heads.add(List.of("课程成绩"));

        // 4. 构建数据
        List<List<Object>> dataList = new ArrayList<>();
        for (StudentInfo student : studentList) {
            String studentUsername = student.studentUsername();
            String studentName = student.studentName();
            String classCode = student.classCode();

            // 获取班级名称
            String className = getClassName(classCode);

            List<Object> row = new ArrayList<>();
            row.add(className);
            row.add(studentUsername);
            row.add(studentName);

            // 计算课程成绩
            CourseGradeResult courseResult = gradeCalculationService.calculateCourseGrade(
                    courseId, studentUsername, classCode);

            // 将实验结果列表转换为Map以便查找
            Map<Long, ExperimentGradeResult> expResultMap =
                ExperimentResultItem.toMap(courseResult.getExperimentResults());

            // 添加各实验成绩
            for (Experiment exp : experiments) {
                ExperimentGradeResult expResult = expResultMap.get(exp.getId());
                if (expResult != null) {
                    row.add(expResult.getDisplayText());
                } else {
                    row.add("未批改");
                }
            }

            // 添加课程成绩
            row.add(courseResult.getDisplayText());

            dataList.add(row);
        }

        // 5. 写入响应
        writeDynamicExcel(response, "课程实验成绩_" + courseId, "成绩表", heads, dataList);

        log.info("导出课程实验成绩成功，课程：{}，班级：{}，学生数：{}", courseId, classCodes, studentList.size());
    }

    /**
     * 导出课程考勤表
     *
     * @param courseId   课程ID
     * @param classCodes 班级编号列表
     * @param response   HTTP响应
     */
    public void exportCourseAttendance(String courseId, List<String> classCodes, HttpServletResponse response)
            throws IOException {
        // 1. 查询课程下所有实验
        LambdaQueryWrapper<Experiment> experimentQuery = new LambdaQueryWrapper<>();
        experimentQuery.eq(Experiment::getCourseId, courseId)
                .eq(Experiment::getIsDeleted, false)
                .orderByAsc(Experiment::getCreatedTime);
        List<Experiment> experiments = experimentService.list(experimentQuery);

        if (experiments.isEmpty()) {
            throw new BusinessException(404, "该课程下没有实验");
        }

        // 2. 查询指定班级的所有学生
        List<StudentInfo> studentList = getStudentsByClassCodes(classCodes);

        if (studentList.isEmpty()) {
            throw new BusinessException(404, "指定班级下没有学生");
        }

        // 3. 查询所有签到记录
        List<String> experimentIds = experiments.stream()
                .map(e -> String.valueOf(e.getId()))
                .collect(Collectors.toList());

        LambdaQueryWrapper<AttendanceRecord> attendanceQuery = new LambdaQueryWrapper<>();
        attendanceQuery.eq(AttendanceRecord::getCourseId, courseId)
                .in(AttendanceRecord::getExperimentId, experimentIds);
        List<AttendanceRecord> attendanceRecords = attendanceRecordService.list(attendanceQuery);

        // 构建签到记录映射：(学生用户名_实验ID) -> 签到状态
        Map<String, Integer> attendanceMap = new LinkedHashMap<>();
        for (AttendanceRecord record : attendanceRecords) {
            String key = record.getStudentUsername() + "_" + record.getExperimentId();
            attendanceMap.put(key, record.getAttendanceStatus());
        }

        // 4. 构建表头
        List<List<String>> heads = new ArrayList<>();
        heads.add(List.of("班级"));
        heads.add(List.of("用户名"));
        heads.add(List.of("姓名"));
        for (Experiment exp : experiments) {
            heads.add(List.of(exp.getExperimentName()));
        }

        // 5. 构建数据
        List<List<Object>> dataList = new ArrayList<>();
        for (StudentInfo student : studentList) {
            String studentUsername = student.studentUsername();
            String studentName = student.studentName();
            String classCode = student.classCode();

            // 获取班级名称
            String className = getClassName(classCode);

            List<Object> row = new ArrayList<>();
            row.add(className);
            row.add(studentUsername);
            row.add(studentName);

            // 添加各实验考勤状态
            for (Experiment exp : experiments) {
                String key = studentUsername + "_" + exp.getId();
                Integer status = attendanceMap.get(key);
                row.add(getAttendanceStatusTextForExport(status));
            }

            dataList.add(row);
        }

        // 6. 写入响应
        writeDynamicExcel(response, "课程考勤表_" + courseId, "考勤表", heads, dataList);

        log.info("导出课程考勤表成功，课程：{}，班级：{}，学生数：{}", courseId, classCodes, studentList.size());
    }

    /**
     * 根据班级编号列表获取学生列表
     */
    private List<StudentInfo> getStudentsByClassCodes(List<String> classCodes) {
        List<StudentInfo> studentList = new ArrayList<>();

        for (String classCode : classCodes) {
            // 查询班级下的学生
            List<StudentClassRelation> relations = studentClassRelationService.getByClassCode(classCode);

            for (StudentClassRelation relation : relations) {
                User student = userMapper.selectOne(
                        new LambdaQueryWrapper<User>().eq(User::getUsername, relation.getStudentUsername())
                );

                if (student != null) {
                    StudentInfo studentInfo = new StudentInfo(
                            student.getUsername(),
                            student.getName(),
                            classCode
                    );

                    studentList.add(studentInfo);
                }
            }
        }

        return studentList;
    }


        private record StudentInfo(String studentUsername, String studentName, String classCode) {
    }

    /**
     * 获取班级名称
     */
    private String getClassName(String classCode) {
        Class clazz = classMapper.selectOne(
                new LambdaQueryWrapper<Class>().eq(Class::getClassCode, classCode)
        );
        return clazz != null ? clazz.getClassName() : classCode;
    }

    /**
     * 写入动态列Excel
     */
    private void writeDynamicExcel(HttpServletResponse response, String fileName, String sheetName,
                                   List<List<String>> heads, List<List<Object>> dataList) throws IOException {
        // 设置响应头
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String encodedFileName = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + encodedFileName + ".xlsx");

        // 写入 Excel
        EasyExcel.write(response.getOutputStream())
                .head(heads)
                .sheet(sheetName)
                .doWrite(dataList);
    }
}
