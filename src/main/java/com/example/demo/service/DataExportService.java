package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.AttendanceRecordMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.pojo.entity.AttendanceRecord;
import com.example.demo.pojo.entity.CourseGrade;
import com.example.demo.pojo.entity.User;
import com.example.demo.pojo.excel.AttendanceRecordExportExcel;
import com.example.demo.pojo.excel.CourseGradeExportExcel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
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
                return "正常";
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
}
