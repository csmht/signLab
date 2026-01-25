package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.enums.AttendanceStatus;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.AttendanceRecordMapper;
import com.example.demo.pojo.dto.AttendanceRequest;
import com.example.demo.pojo.dto.AttendanceResponse;
import com.example.demo.pojo.entity.AttendanceRecord;
import com.example.demo.pojo.entity.ClassExperiment;
import com.example.demo.pojo.entity.StudentClassRelation;
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
    private final ClassExperimentService classExperimentService;
    private final StudentClassRelationService studentClassRelationService;

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
            List<StudentClassRelation> studentClasses = studentClassRelationService
                    .getByStudentUsername(studentUsername);
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
            ClassExperiment classExperiment = classExperimentService.getOne(classExperimentQuery);
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
}
