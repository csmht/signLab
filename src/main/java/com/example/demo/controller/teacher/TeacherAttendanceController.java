package com.example.demo.controller.teacher;

import com.example.demo.annotation.RequireRole;
import com.example.demo.enums.UserRole;
import com.example.demo.pojo.request.UpdateAttendanceRequest;
import com.example.demo.pojo.response.ApiResponse;
import com.example.demo.pojo.response.AttendanceListResponse;
import com.example.demo.pojo.entity.AttendanceRecord;
import com.example.demo.service.AttendanceRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 教师签到记录控制器
 * 提供教师查询和管理学生签到记录的接口
 */
@RequestMapping("/api/teacher/attendance")
@RestController
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class TeacherAttendanceController {

    private final AttendanceRecordService attendanceRecordService;

    /**
     * 查询指定班级实验的签到情况
     * 返回分类后的签到列表：非跨班签到、跨班签到、未签到
     *
     * @param classExperimentId 班级实验ID
     * @return 签到列表响应
     */
    @GetMapping("/list")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<AttendanceListResponse> getAttendanceList(
            @RequestParam("classExperimentId") Long classExperimentId) {
        try {
            AttendanceListResponse response = attendanceRecordService.getAttendanceList(classExperimentId);
            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("查询签到列表失败", e);
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 修改签到状态
     * 支持修改已签到学生的状态，或为未签到学生添加签到记录
     *
     * @param request 修改签到状态请求
     * @return 是否修改成功
     */
    @PostMapping("/update")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<Boolean> updateAttendanceStatus(@RequestBody UpdateAttendanceRequest request) {
        try {
            boolean success = attendanceRecordService.updateAttendanceStatus(request);
            if (success) {
                return ApiResponse.success(success, "签到状态修改成功");
            } else {
                return ApiResponse.error(500, "签到状态修改失败");
            }
        } catch (Exception e) {
            log.error("修改签到状态失败", e);
            return ApiResponse.error(500, "修改失败: " + e.getMessage());
        }
    }

    /**
     * 根据课程ID和实验ID查询签到记录
     *
     * @param courseId 课程ID
     * @param experimentId 实验ID
     * @return 签到记录列表
     */
    @GetMapping("/records")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<List<AttendanceRecord>> getAttendanceRecords(
            @RequestParam("courseId") String courseId,
            @RequestParam("experimentId") String experimentId) {
        try {
            List<AttendanceRecord> records = attendanceRecordService.getByCourseAndExperiment(courseId, experimentId);
            return ApiResponse.success(records);
        } catch (Exception e) {
            log.error("查询签到记录失败", e);
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 统计指定课程和实验的已签到学生数量
     *
     * @param courseId 课程ID
     * @param experimentId 实验ID
     * @return 已签到学生数量
     */
    @GetMapping("/count")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<Long> countAttendance(
            @RequestParam("courseId") String courseId,
            @RequestParam("experimentId") String experimentId) {
        try {
            Long count = attendanceRecordService.countByCourseAndExperiment(courseId, experimentId);
            return ApiResponse.success(count);
        } catch (Exception e) {
            log.error("统计签到数量失败", e);
            return ApiResponse.error(500, "统计失败: " + e.getMessage());
        }
    }

    /**
     * 查询跨班签到学生
     * 查询指定班级实验中跨班签到的学生列表
     *
     * @param classExperimentId 班级实验ID
     * @return 跨班签到学生列表
     */
    @GetMapping("/cross-class-attendees")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<java.util.List<java.util.Map<String, Object>>> getCrossClassAttendees(
            @RequestParam("classExperimentId") Long classExperimentId) {
        try {
            java.util.List<java.util.Map<String, Object>> attendees =
                    attendanceRecordService.getCrossClassAttendees(classExperimentId);
            return ApiResponse.success(attendees);
        } catch (Exception e) {
            log.error("查询跨班签到学生失败", e);
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

}