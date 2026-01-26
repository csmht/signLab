package com.example.demo.controller;

import com.example.demo.annotation.RequireRole;
import com.example.demo.enums.UserRole;
import com.example.demo.pojo.request.AttendanceRequest;
import com.example.demo.pojo.response.ApiResponse;
import com.example.demo.pojo.response.AttendanceResponse;
import com.example.demo.pojo.vo.TeacherQrVO;
import com.example.demo.service.AttendanceRecordService;
import com.example.demo.service.QrService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;



@RequestMapping("/api/qr")
@RestController
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class QrController {

    private final QrService qrService;
    private final AttendanceRecordService attendanceRecordService;

    /**
     * 获取签到二维码
     * @return 二维码数据
     */
    @GetMapping("/teacher")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<TeacherQrVO> getQr(Long classExperimentId) {
        TeacherQrVO teacherQrVO = qrService.getTeacherQrVO(classExperimentId);
        return ApiResponse.success(teacherQrVO);
    }

    /**
     * 根据班级代码和实验ID获取签到二维码
     *
     * @param classCode 班级代码
     * @param experimentId 实验ID
     * @return 二维码数据
     */
    @GetMapping("/teacher/by-class-experiment")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<TeacherQrVO> getQrByClassAndExperiment(
            @RequestParam("classCode") String classCode,
            @RequestParam("experimentId") String experimentId) {
        TeacherQrVO teacherQrVO = qrService.getTeacherQrVOByClassAndExperiment(classCode, experimentId);
        return ApiResponse.success(teacherQrVO);
    }

    /**
     * 学生扫码签到（GET请求）
     * 学生扫码后自动跳转到此接口
     * @param key 加密的二维码数据
     * @param httpRequest HTTP请求
     * @return 签到响应
     */
    @GetMapping("/student/scan/{key}")
    @RequireRole(value = UserRole.STUDENT)
    public ApiResponse<AttendanceResponse> scanAttendance( @PathVariable String key,
            HttpServletRequest httpRequest) {

        // 获取客户端IP地址
        String ipAddress = getClientIpAddress(httpRequest);

        // 构建签到请求
        AttendanceRequest request = new AttendanceRequest();
        request.setEncryptedData(key);
        request.setIpAddress(ipAddress);

        AttendanceResponse response = attendanceRecordService.scanAttendance(request);
        return ApiResponse.success(response);
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 处理多个IP的情况，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
