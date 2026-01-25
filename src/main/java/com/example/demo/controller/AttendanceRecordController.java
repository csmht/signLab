package com.example.demo.controller;

import com.example.demo.annotation.RequireRole;
import com.example.demo.enums.UserRole;
import com.example.demo.pojo.dto.ApiResponse;
import com.example.demo.pojo.dto.AttendanceListResponse;
import com.example.demo.pojo.dto.UpdateAttendanceRequest;
import com.example.demo.service.AttendanceRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 签到记录控制器
 * 提供签到记录的查询和修改接口
 */
@RequestMapping("/api/attendance")
@RestController
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AttendanceRecordController {

    private final AttendanceRecordService attendanceRecordService;

    /**
     * 查询指定班级实验的签到情况
     * @param classExperimentId 班级实验ID
     * @return 签到列表响应
     */
    @GetMapping("/list")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<AttendanceListResponse> getAttendanceList(
            @RequestParam("classExperimentId") Long classExperimentId) {
        AttendanceListResponse response = attendanceRecordService.getAttendanceList(classExperimentId);
        return ApiResponse.success(response);
    }

    /**
     * 修改签到状态
     * @param request 修改签到状态请求
     * @return 是否修改成功
     */
    @PostMapping("/update")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<Boolean> updateAttendanceStatus(@RequestBody UpdateAttendanceRequest request) {
        boolean success = attendanceRecordService.updateAttendanceStatus(request);
        return ApiResponse.success(success);
    }
}