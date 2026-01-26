package com.example.demo.controller.teacher;

import com.alibaba.excel.EasyExcel;
import com.example.demo.annotation.RequireRole;
import com.example.demo.enums.UserRole;
import com.example.demo.pojo.excel.AttendanceRecordExportExcel;
import com.example.demo.pojo.excel.CourseGradeExportExcel;
import com.example.demo.service.DataExportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

/**
 * 教师数据导出控制器
 * 提供教师导出各类数据的接口
 */
@RequestMapping("/api/teacher/export")
@RestController
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class TeacherExportController {

    private final DataExportService dataExportService;

    /**
     * 导出课程成绩
     *
     * @param courseId 课程ID
     * @param semester 学期（可选）
     * @param response HTTP响应
     */
    @GetMapping("/course-grades/{courseId}")
    @RequireRole(value = UserRole.TEACHER)
    public void exportCourseGrades(
            @PathVariable("courseId") String courseId,
            @RequestParam(value = "semester", required = false) String semester,
            HttpServletResponse response) throws IOException {
        try {
            // 查询成绩数据
            List<CourseGradeExportExcel> data = dataExportService.exportCourseGrades(courseId, semester);

            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("课程成绩_" + courseId, "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

            // 写入 Excel
            EasyExcel.write(response.getOutputStream(), CourseGradeExportExcel.class)
                    .sheet("课程成绩")
                    .doWrite(data);

            log.info("导出课程成绩成功，课程：{}，记录数：{}", courseId, data.size());

        } catch (Exception e) {
            log.error("导出课程成绩失败", e);
            response.setStatus(500);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":500,\"message\":\"导出失败: " + e.getMessage() + "\"}");
        }
    }

    /**
     * 导出考勤记录
     *
     * @param courseId 课程ID
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param response HTTP响应
     */
    @GetMapping("/attendance-records/{courseId}")
    @RequireRole(value = UserRole.TEACHER)
    public void exportAttendanceRecords(
            @PathVariable("courseId") String courseId,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            HttpServletResponse response) throws IOException {
        try {
            // 查询考勤数据
            List<AttendanceRecordExportExcel> data = dataExportService.exportAttendanceRecords(
                    courseId, startDate, endDate
            );

            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("考勤记录_" + courseId, "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

            // 写入 Excel
            EasyExcel.write(response.getOutputStream(), AttendanceRecordExportExcel.class)
                    .sheet("考勤记录")
                    .doWrite(data);

            log.info("导出考勤记录成功，课程：{}，记录数：{}", courseId, data.size());

        } catch (Exception e) {
            log.error("导出考勤记录失败", e);
            response.setStatus(500);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":500,\"message\":\"导出失败: " + e.getMessage() + "\"}");
        }
    }
}
