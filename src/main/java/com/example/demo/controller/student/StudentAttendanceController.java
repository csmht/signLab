package com.example.demo.controller.student;

import com.example.demo.annotation.RequireRole;
import com.example.demo.enums.UserRole;
import com.example.demo.pojo.request.AttendanceRequest;
import com.example.demo.pojo.response.ApiResponse;
import com.example.demo.pojo.response.AttendanceResponse;
import com.example.demo.service.AttendanceRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 学生签到记录控制器
 * 提供学生签到相关的接口
 */
@RequestMapping("/api/student/attendance")
@RestController
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class StudentAttendanceController {

    private final AttendanceRecordService attendanceRecordService;

    /**
     * 查询学生的签到记录
     * 返回当前登录学生的所有签到记录
     *
     * @return 签到记录列表
     */
    @GetMapping("/my-records")
    @RequireRole(value = UserRole.STUDENT)
    public ApiResponse<com.example.demo.pojo.entity.AttendanceRecord> getMyAttendanceRecord() {
        try {
            String studentUsername = com.example.demo.util.SecurityUtil.getCurrentUsername()
                    .orElseThrow(() -> new com.example.demo.exception.BusinessException(401, "未登录"));

            com.example.demo.pojo.entity.AttendanceRecord record =
                    attendanceRecordService.getByStudentUsername(studentUsername);

            if (record == null) {
                return ApiResponse.error(404, "未找到签到记录");
            }

            return ApiResponse.success(record);
        } catch (Exception e) {
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

}