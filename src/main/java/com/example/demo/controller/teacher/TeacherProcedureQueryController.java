package com.example.demo.controller.teacher;

import com.example.demo.annotation.RequireRole;
import com.example.demo.enums.UserRole;
import com.example.demo.pojo.response.ApiResponse;
import com.example.demo.pojo.response.TeacherProcedureDetailResponse;
import com.example.demo.service.TeacherProcedureQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 教师查询实验步骤详情控制器
 * 提供教师查询实验步骤详情的接口
 */
@RequestMapping("/api/teacher/procedures")
@RestController
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class TeacherProcedureQueryController {

    private final TeacherProcedureQueryService teacherProcedureQueryService;

    /**
     * 查询单个步骤详情
     *
     * @param procedureId 步骤ID
     * @return 步骤详情
     */
    @GetMapping("/{procedureId}")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<TeacherProcedureDetailResponse> getProcedureDetail(@PathVariable("procedureId") Long procedureId) {
        try {
            TeacherProcedureDetailResponse response = teacherProcedureQueryService.getProcedureDetail(procedureId);
            return ApiResponse.success(response, "查询成功");
        } catch (com.example.demo.exception.BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("查询步骤详情失败", e);
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 查询实验的所有步骤详情
     *
     * @param experimentId 实验ID
     * @return 步骤详情列表
     */
    @GetMapping("/experiment/{experimentId}")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<List<TeacherProcedureDetailResponse>> getExperimentProcedures(
            @PathVariable("experimentId") Long experimentId) {
        try {
            List<TeacherProcedureDetailResponse> responses = teacherProcedureQueryService.getExperimentProcedures(experimentId);
            return ApiResponse.success(responses, "查询成功");
        } catch (com.example.demo.exception.BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("查询实验步骤列表失败", e);
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }
}
