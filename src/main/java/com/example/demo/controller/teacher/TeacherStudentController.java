package com.example.demo.controller.teacher;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.annotation.RequireRole;
import com.example.demo.enums.UserRole;
import com.example.demo.enums.AttendanceStatus;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.AttendanceRecordMapper;
import com.example.demo.mapper.ClassExperimentMapper;
import com.example.demo.mapper.StudentClassRelationMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.mapper.ClassMapper;
import com.example.demo.pojo.entity.AttendanceRecord;
import com.example.demo.pojo.entity.ClassExperiment;
import com.example.demo.pojo.entity.StudentClassRelation;
import com.example.demo.pojo.entity.User;
import com.example.demo.pojo.entity.Class;
import com.example.demo.pojo.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 教师学生查询控制器
 * 提供教师查询学生信息的接口
 */
@RequestMapping("/api/teacher/students")
@RestController
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class TeacherStudentController {

    private final StudentClassRelationMapper studentClassRelationMapper;
    private final UserMapper userMapper;
    private final ClassMapper classMapper;
    private final AttendanceRecordMapper attendanceRecordMapper;
    private final ClassExperimentMapper classExperimentMapper;

    /**
     * 查询学生列表（支持过滤）
     * 可以按班级代码和学生类型过滤
     *
     * @param classCode 班级代码（可选）
     * @param studentType 学生类型（可选：CLASS_STUDENT, CROSS_CLASS_ATTENDEE）
     * @param classExperimentId 班级实验ID（可选，用于查询签到统计）
     * @return 学生列表和统计信息
     */
    @GetMapping
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<Map<String, Object>> getStudents(
            @RequestParam(value = "classCode", required = false) String classCode,
            @RequestParam(value = "studentType", required = false) String studentType,
            @RequestParam(value = "classExperimentId", required = false) Long classExperimentId) {
        try {
            List<Map<String, Object>> studentList = new ArrayList<>();

            // 如果指定了班级实验，查询该实验的签到信息
            String courseId = null;
            String experimentId = null;
            if (classExperimentId != null) {
                ClassExperiment classExperiment = classExperimentMapper.selectById(classExperimentId);
                if (classExperiment == null) {
                    return ApiResponse.error(404, "班级实验不存在");
                }
                courseId = classExperiment.getCourseId();
                experimentId = classExperiment.getExperimentId();
                classCode = classExperiment.getClassCode(); // 使用班级实验的班级代码
            }

            // 查询班级的所有学生
            QueryWrapper<StudentClassRelation> relationQuery = new QueryWrapper<>();
            if (StringUtils.hasText(classCode)) {
                relationQuery.eq("class_code", classCode);
            }
            List<StudentClassRelation> relations = studentClassRelationMapper.selectList(relationQuery);

            // 查询所有签到记录（如果指定了班级实验）
            Map<String, AttendanceRecord> attendanceMap = new HashMap<>();
            if (courseId != null && experimentId != null) {
                QueryWrapper<AttendanceRecord> attendanceQuery = new QueryWrapper<>();
                attendanceQuery.eq("course_id", courseId)
                        .eq("experiment_id", experimentId);
                List<AttendanceRecord> attendanceRecords = attendanceRecordMapper.selectList(attendanceQuery);
                attendanceMap = attendanceRecords.stream()
                        .collect(Collectors.toMap(AttendanceRecord::getStudentUsername, r -> r, (r1, r2) -> r1));
            }

            // 构建学生信息列表
            for (StudentClassRelation relation : relations) {
                String studentUsername = relation.getStudentUsername();

                // 查询学生信息
                User student = userMapper.selectOne(
                        new QueryWrapper<User>().eq("username", studentUsername)
                );
                if (student == null) {
                    continue;
                }

                // 查询班级信息
                Class studentClass = classMapper.selectOne(
                        new QueryWrapper<Class>().eq("class_code", relation.getClassCode())
                );

                Map<String, Object> studentInfo = new HashMap<>();
                studentInfo.put("studentCode", studentUsername);
                studentInfo.put("studentName", student.getName());
                studentInfo.put("classCode", relation.getClassCode());
                studentInfo.put("className", studentClass != null ? studentClass.getClassName() : relation.getClassCode());

                // 确定学生类型
                AttendanceRecord record = attendanceMap.get(studentUsername);
                String currentStudentType;
                if (record != null && AttendanceStatus.CROSS_CLASS.getCode().equals(record.getAttendanceStatus())) {
                    currentStudentType = "CROSS_CLASS_ATTENDEE";
                } else {
                    currentStudentType = "CLASS_STUDENT";
                }
                studentInfo.put("studentType", currentStudentType);

                // 添加签到信息
                if (record != null) {
                    studentInfo.put("attendanceStatus", record.getAttendanceStatus());
                    studentInfo.put("attendanceTime", record.getAttendanceTime());
                }

                // 根据学生类型过滤
                if (StringUtils.hasText(studentType)) {
                    if (!studentType.equals(currentStudentType)) {
                        continue;
                    }
                }

                studentList.add(studentInfo);
            }

            // 构建统计信息
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalStudents", studentList.size());

            if (courseId != null && experimentId != null) {
                // 统计班级学生数
                long classStudentCount = studentList.stream()
                        .filter(s -> "CLASS_STUDENT".equals(s.get("studentType")))
                        .count();
                stats.put("classStudentCount", classStudentCount);

                // 统计跨班学生数
                long crossClassCount = studentList.stream()
                        .filter(s -> "CROSS_CLASS_ATTENDEE".equals(s.get("studentType")))
                        .count();
                stats.put("crossClassAttendeeCount", crossClassCount);

                // 统计已签到数
                long attendedCount = studentList.stream()
                        .filter(s -> s.get("attendanceStatus") != null)
                        .count();
                stats.put("attendedCount", attendedCount);

                // 计算签到率
                if (studentList.size() > 0) {
                    double attendanceRate = attendedCount * 100.0 / studentList.size();
                    stats.put("attendanceRate", Math.round(attendanceRate * 100.0) / 100.0);
                } else {
                    stats.put("attendanceRate", 0.0);
                }
            }

            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("data", studentList);
            result.put("stats", stats);

            return ApiResponse.success(result, "查询成功");
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("查询学生列表失败", e);
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }
}
