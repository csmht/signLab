package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.enums.AttendanceStatus;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.AttendanceRecordMapper;
import com.example.demo.mapper.ClassExperimentMapper;
import com.example.demo.mapper.ClassMapper;
import com.example.demo.mapper.StudentClassRelationMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.pojo.request.AttendanceRequest;
import com.example.demo.pojo.request.UpdateAttendanceRequest;
import com.example.demo.pojo.response.AttendanceListResponse;
import com.example.demo.pojo.response.AttendanceResponse;
import com.example.demo.pojo.entity.AttendanceRecord;
import com.example.demo.pojo.entity.Class;
import com.example.demo.pojo.entity.ClassExperiment;
import com.example.demo.pojo.entity.StudentClassRelation;
import com.example.demo.pojo.entity.User;
import com.example.demo.util.CryptoUtil;
import com.example.demo.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 签到记录服务
 * 提供签到记录的业务逻辑处理
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceRecordService extends ServiceImpl<AttendanceRecordMapper, AttendanceRecord> {

    private final CryptoUtil cryptoUtil;
    private final ClassExperimentMapper classExperimentMapper;
    private final StudentClassRelationMapper studentClassRelationMapper;
    private final UserMapper userMapper;
    private final ClassMapper classMapper;

    @Value("${slz.late.time:5}")
    private Long lateTime;

    /**
     * 根据学生用户名查询签到记录
     */
    public AttendanceRecord getByStudentUsername(String studentUsername) {
        QueryWrapper<AttendanceRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("student_username", studentUsername);
        return getOne(queryWrapper);
    }

    /**
     * 学生扫码签到
     *
     * @param request 签到请求
     * @return 签到响应
     */
    public AttendanceResponse scanAttendance(AttendanceRequest request) {
        AttendanceResponse response = new AttendanceResponse();

        try {
            // 1. 获取当前登录学生
            String studentUsername = SecurityUtil.getCurrentUsername()
                    .orElseThrow(() -> new BusinessException(401, "未登录，请先登录"));

            // 2. 解密二维码数据
            String decryptedData;
            try {
                decryptedData = cryptoUtil.decrypt(request.getEncryptedData());
            } catch (Exception e) {
                log.error("二维码解密失败", e);
                response.setSuccess(false);
                response.setMessage("二维码无效");
                return response;
            }

            // 3. 解析二维码数据
            String[] parts = decryptedData.split("\\|");
            if (parts.length < 5) {
                response.setSuccess(false);
                response.setMessage("二维码格式错误");
                return response;
            }

            String teacherName = parts[0];
            String classCode = parts[1];
            String experimentId = parts[2];
            LocalDateTime endTime = LocalDateTime.parse(parts[3]);

            // 4. 验证二维码是否过期
            if (endTime.isBefore(LocalDateTime.now())) {
                response.setSuccess(false);
                response.setMessage("二维码已过期，请重新扫描");
                return response;
            }

            // 5. 查询学生所在班级
            QueryWrapper<StudentClassRelation> studentClassQuery = new QueryWrapper<>();
            studentClassQuery.eq("student_username", studentUsername);
            List<StudentClassRelation> studentClasses = studentClassRelationMapper.selectList(studentClassQuery);
            if (studentClasses.isEmpty()) {
                response.setSuccess(false);
                response.setMessage("未找到班级信息，请联系管理员");
                return response;
            }

            // 6. 验证学生是否属于该班级
            boolean isInClass = studentClasses.stream()
                    .anyMatch(sc -> sc.getClassCode().equals(classCode));

            // 获取学生实际所在班级代码
            String studentActualClassCode = isInClass ? classCode : studentClasses.get(0).getClassCode();

            // 7. 查询班级实验信息
            QueryWrapper<ClassExperiment> classExperimentQuery = new QueryWrapper<>();
            classExperimentQuery.eq("class_code", classCode)
                    .eq("experiment_id", experimentId);
            ClassExperiment classExperiment = classExperimentMapper.selectOne(classExperimentQuery);
            if (classExperiment == null) {
                response.setSuccess(false);
                response.setMessage("未找到班级实验信息");
                return response;
            }

            // 8. 检查是否已经签到该课次
            QueryWrapper<AttendanceRecord> existingQuery = new QueryWrapper<>();
            existingQuery.eq("course_id", classExperiment.getCourseId())
                    .eq("experiment_id", experimentId)
                    .eq("student_username", studentUsername);
            AttendanceRecord existingRecord = getOne(existingQuery);
            if (existingRecord != null) {
                response.setSuccess(false);
                response.setMessage("您已签到该课次，签到时间：" + existingRecord.getAttendanceTime());
                response.setAttendanceTime(existingRecord.getAttendanceTime());
                response.setAttendanceStatus(existingRecord.getAttendanceStatus());
                return response;
            }

            // 9. 判断签到状态
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startTime = classExperiment.getStartTime();
            AttendanceStatus attendanceStatus;

            if (!isInClass) {
                // 跨班签到
                attendanceStatus = AttendanceStatus.CROSS_CLASS;
            } else if (now.isAfter(startTime.plusMinutes(lateTime))) {
                // 迟到判断：超过开始时间
                attendanceStatus = AttendanceStatus.LATE;
            } else {
                // 正常签到
                attendanceStatus = AttendanceStatus.NORMAL;
            }

            // 10. 创建签到记录
            AttendanceRecord record = new AttendanceRecord();
            record.setCourseId(classExperiment.getCourseId());
            record.setExperimentId(experimentId);
            record.setStudentUsername(studentUsername);
            record.setAttendanceTime(now);
            record.setAttendanceStatus(attendanceStatus.getCode());
            record.setStudentActualClassCode(studentActualClassCode);
            record.setIpAddress(request.getIpAddress());

            boolean saved = save(record);
            if (!saved) {
                response.setSuccess(false);
                response.setMessage("签到失败，请重试");
                return response;
            }

            // 11. 返回签到成功信息
            response.setSuccess(true);
            response.setAttendanceStatus(attendanceStatus.getCode());
            response.setAttendanceTime(now);
            response.setClassCode(classCode);
            response.setExperimentId(experimentId);
            response.setCourseId(classExperiment.getCourseId());
            response.setMessage("签到成功");

            log.info("学生 {} 签到成功，班级：{}，实验：{}，状态：{}",
                    studentUsername, classCode, experimentId, attendanceStatus);

            return response;

        } catch (BusinessException e) {
            log.error("签到业务异常", e);
            response.setSuccess(false);
            response.setMessage(e.getMessage());
            return response;
        } catch (Exception e) {
            log.error("签到系统异常", e);
            response.setSuccess(false);
            response.setMessage("系统异常，请稍后重试");
            return response;
        }
    }

    /**
     * 根据课程ID和实验ID查询签到记录
     */
    public List<AttendanceRecord> getByCourseAndExperiment(String courseId, String experimentId) {
        QueryWrapper<AttendanceRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("course_id", courseId)
                .eq("experiment_id", experimentId);
        return list(queryWrapper);
    }

    /**
     * 根据课程ID和实验ID查询已签到学生数量
     */
    public Long countByCourseAndExperiment(String courseId, String experimentId) {
        QueryWrapper<AttendanceRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("course_id", courseId)
                .eq("experiment_id", experimentId);
        return count(queryWrapper);
    }

    /**
     * 查询指定班级实验的签到情况
     *
     * @param classExperimentId 班级实验ID
     * @return 签到列表响应
     */
    public com.example.demo.pojo.dto.AttendanceListResponse getAttendanceList(Long classExperimentId) {
        // 1. 查询班级实验信息
        ClassExperiment classExperiment = classExperimentMapper.selectById(classExperimentId);
        if (classExperiment == null) {
            throw new BusinessException(404, "班级实验不存在");
        }

        String classCode = classExperiment.getClassCode();
        String courseId = classExperiment.getCourseId();
        String experimentId = classExperiment.getExperimentId();

        // 2. 查询该班级的所有学生
        QueryWrapper<StudentClassRelation> studentClassQuery = new QueryWrapper<>();
        studentClassQuery.eq("class_code", classCode);
        List<StudentClassRelation> studentClassRelations =
                studentClassRelationMapper.selectList(studentClassQuery);

        // 3. 查询该课次的所有签到记录
        QueryWrapper<AttendanceRecord> attendanceQuery = new QueryWrapper<>();
        attendanceQuery.eq("course_id", courseId)
                .eq("experiment_id", experimentId);
        List<AttendanceRecord> attendanceRecords = list(attendanceQuery);

        // 4. 构建签到信息映射（学生用户名 -> 签到记录）
        java.util.Map<String, AttendanceRecord> attendanceMap = attendanceRecords.stream()
                .collect(java.util.stream.Collectors.toMap(
                        AttendanceRecord::getStudentUsername,
                        record -> record
                ));

        // 5. 构建返回结果
        com.example.demo.pojo.dto.AttendanceListResponse response = new com.example.demo.pojo.dto.AttendanceListResponse();
        response.setNormalAttendanceList(new java.util.ArrayList<>());
        response.setCrossClassAttendanceList(new java.util.ArrayList<>());
        response.setNotAttendanceList(new java.util.ArrayList<>());

        // 6. 遍历班级学生，分类处理
        for (StudentClassRelation relation : studentClassRelations) {
            String studentUsername = relation.getStudentUsername();
            AttendanceRecord record = attendanceMap.get(studentUsername);

            // 查询学生信息
            User student = userMapper.selectOne(
                    new QueryWrapper<User>()
                            .eq("username", studentUsername)
            );

            // 查询班级信息
            QueryWrapper<Class> classQuery = new QueryWrapper<>();
            classQuery.eq("class_code", classCode);
            Class studentClass = classMapper.selectOne(classQuery);

            com.example.demo.pojo.dto.AttendanceListResponse.StudentAttendanceInfo info =
                    new com.example.demo.pojo.dto.AttendanceListResponse.StudentAttendanceInfo();
            info.setStudentUsername(studentUsername);
            info.setStudentName(student != null ? student.getName() : studentUsername);
            info.setClassName(studentClass != null ? studentClass.getClassName() : classCode);

            if (record == null) {
                // 未签到
                info.setAttendanceId(null);
                info.setAttendanceStatus(null);
                info.setAttendanceTime(null);
                response.getNotAttendanceList().add(info);
            } else if (AttendanceStatus.CROSS_CLASS.getCode().equals(record.getAttendanceStatus())) {
                // 跨班签到
                info.setAttendanceId(record.getId());
                info.setAttendanceStatus(record.getAttendanceStatus());
                info.setAttendanceTime(record.getAttendanceTime());
                response.getCrossClassAttendanceList().add(info);
            } else {
                // 非跨班签到（正常、迟到、补签）
                info.setAttendanceId(record.getId());
                info.setAttendanceStatus(record.getAttendanceStatus());
                info.setAttendanceTime(record.getAttendanceTime());
                response.getNormalAttendanceList().add(info);
            }
        }

        return response;
    }

    /**
     * 修改签到状态
     *
     * @param request 修改签到状态请求
     * @return 是否修改成功
     */
    public boolean updateAttendanceStatus(com.example.demo.pojo.dto.UpdateAttendanceRequest request) {
        // 1. 查询班级实验信息
        ClassExperiment classExperiment = classExperimentMapper.selectById(request.getClassExperimentId());
        if (classExperiment == null) {
            throw new BusinessException(404, "班级实验不存在");
        }

        String courseId = classExperiment.getCourseId();
        String experimentId = classExperiment.getExperimentId();
        String studentUsername = request.getStudentUsername();

        // 2. 查询学生是否已签到
        QueryWrapper<AttendanceRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("course_id", courseId)
                .eq("experiment_id", experimentId)
                .eq("student_username", studentUsername);
        AttendanceRecord record = getOne(queryWrapper);

        // 3. 验证签到状态是否有效
        try {
            AttendanceStatus.fromCode(request.getAttendanceStatus());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(400, "无效的签到状态: " + request.getAttendanceStatus());
        }

        // 4. 查询学生实际所在班级
        QueryWrapper<StudentClassRelation> studentClassQuery = new QueryWrapper<>();
        studentClassQuery.eq("student_username", studentUsername);
        List<StudentClassRelation> studentClasses = studentClassRelationMapper.selectList(studentClassQuery);
        if (studentClasses.isEmpty()) {
            throw new BusinessException(404, "未找到学生班级信息");
        }
        String studentActualClassCode = studentClasses.get(0).getClassCode();

        if (record == null) {
            // 学生未签到，创建新的签到记录
            record = new AttendanceRecord();
            record.setCourseId(courseId);
            record.setExperimentId(experimentId);
            record.setStudentUsername(studentUsername);
            record.setAttendanceTime(LocalDateTime.now());
            record.setAttendanceStatus(request.getAttendanceStatus());
            record.setStudentActualClassCode(studentActualClassCode);
            record.setIpAddress(null);

            boolean saved = save(record);
            log.info("为学生 {} 创建签到记录，状态：{}", studentUsername, request.getAttendanceStatus());
            return saved;
        } else {
            // 学生已签到，更新签到状态
            record.setAttendanceStatus(request.getAttendanceStatus());
            record.setUpdateTime(LocalDateTime.now());

            boolean updated = updateById(record);
            log.info("更新学生 {} 的签到状态为：{}", studentUsername, request.getAttendanceStatus());
            return updated;
        }
    }
}
