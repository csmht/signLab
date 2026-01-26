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
     * 扫码签到
     * 学生扫描二维码进行签到
     *
     * @param request 签到请求（包含加密的二维码数据）
     * @return 签到结果
     */
    @PostMapping("/scan")
    @RequireRole(value = UserRole.STUDENT)
    public ApiResponse<AttendanceResponse> scanAttendance(@RequestBody AttendanceRequest request) {
        try {
            AttendanceResponse response = attendanceRecordService.scanAttendance(request);
            if (response.isSuccess()) {
                return ApiResponse.success(response, response.getMessage());
            } else {
                return ApiResponse.error(400, response.getMessage());
            }
        } catch (com.example.demo.exception.BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("签到失败", e);
            return ApiResponse.error(500, "签到失败: " + e.getMessage());
        }
    }

    /**
     * 查询学生的签到记录列表
     * 返回当前登录学生的所有签到记录
     *
     * @return 签到记录列表
     */
    @GetMapping("/records")
    @RequireRole(value = UserRole.STUDENT)
    public ApiResponse<java.util.List<com.example.demo.pojo.entity.AttendanceRecord>> getAttendanceRecords() {
        try {
            String studentUsername = com.example.demo.util.SecurityUtil.getCurrentUsername()
                    .orElseThrow(() -> new com.example.demo.exception.BusinessException(401, "未登录"));

            java.util.List<com.example.demo.pojo.entity.AttendanceRecord> records =
                    attendanceRecordService.getStudentAttendanceRecords(studentUsername);

            return ApiResponse.success(records, "查询签到记录成功");
        } catch (Exception e) {
            log.error("查询签到记录失败", e);
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 查询学生的签到统计
     * 返回当前登录学生的签到统计信息
     *
     * @return 签到统计信息
     */
    @GetMapping("/stats")
    @RequireRole(value = UserRole.STUDENT)
    public ApiResponse<java.util.Map<String, Object>> getAttendanceStats() {
        try {
            String studentUsername = com.example.demo.util.SecurityUtil.getCurrentUsername()
                    .orElseThrow(() -> new com.example.demo.exception.BusinessException(401, "未登录"));

            java.util.Map<String, Object> stats = attendanceRecordService.getStudentAttendanceStats(studentUsername);

            return ApiResponse.success(stats, "查询签到统计成功");
        } catch (Exception e) {
            log.error("查询签到统计失败", e);
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 查询单个签到记录
     * 返回当前登录学生的最近一次签到记录
     *
     * @return 签到记录
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