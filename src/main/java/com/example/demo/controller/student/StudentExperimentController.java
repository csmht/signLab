package com.example.demo.controller.student;

import com.example.demo.annotation.RequireRole;
import com.example.demo.enums.UserRole;
import com.example.demo.pojo.response.ApiResponse;
import com.example.demo.pojo.response.StudentExperimentDetailResponse;
import com.example.demo.service.StudentExperimentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 学生实验控制器
 * 提供学生查询实验详情的接口
 */
@RequestMapping("/api/student/experiments")
@RestController
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class StudentExperimentController {

    private final StudentExperimentService studentExperimentService;

    /**
     * 查询学生实验详情
     * 返回实验基本信息及步骤列表（包含是否可做标识）
     *
     * @param experimentId 实验ID
     * @param classCode    班级编号
     * @return 实验详情
     */
    @GetMapping("/{experimentId}")
    @RequireRole(value = UserRole.STUDENT)
    public ApiResponse<StudentExperimentDetailResponse> getExperimentDetail(
            @PathVariable("experimentId") Long experimentId,
            @RequestParam("classCode") String classCode) {
        try {
            // 获取当前登录学生用户名
            String studentUsername = com.example.demo.util.SecurityUtil.getCurrentUsername()
                    .orElseThrow(() -> new com.example.demo.exception.BusinessException(401, "未登录"));

            StudentExperimentDetailResponse response = studentExperimentService.getStudentExperimentDetail(
                    experimentId, classCode, studentUsername);

            return ApiResponse.success(response, "查询成功");
        } catch (com.example.demo.exception.BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("查询实验详情失败", e);
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }
}
