package com.example.demo.controller.teacher;

import com.example.demo.annotation.RequireRole;
import com.example.demo.enums.UserRole;
import com.example.demo.pojo.request.teacher.CreateDataCollectionProcedureRequest;
import com.example.demo.pojo.request.teacher.CreateTopicProcedureRequest;
import com.example.demo.pojo.request.teacher.CreateVideoProcedureRequest;
import com.example.demo.pojo.response.ApiResponse;
import com.example.demo.service.TeacherProcedureCreationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 教师创建实验步骤控制器
 * 提供教师创建不同类型实验步骤的接口
 */
@RequestMapping("/api/teacher/procedures")
@RestController
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class TeacherProcedureCreationController {

    private final TeacherProcedureCreationService teacherProcedureCreationService;

    /**
     * 创建视频观看步骤
     * 步骤类型：type=1（观看视频）
     *
     * @param request 创建视频步骤请求
     * @return 步骤ID
     */
    @PostMapping("/video")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<Long> createVideoProcedure(@RequestBody CreateVideoProcedureRequest request) {
        try {
            Long procedureId = teacherProcedureCreationService.createVideoProcedure(request);
            return ApiResponse.success(procedureId, "创建成功");
        } catch (com.example.demo.exception.BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("创建视频观看步骤失败", e);
            return ApiResponse.error(500, "创建失败: " + e.getMessage());
        }
    }

    /**
     * 创建数据收集步骤
     * 步骤类型：type=2（数据收集）
     *
     * @param request 创建数据收集步骤请求
     * @return 步骤ID
     */
    @PostMapping("/data-collection")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<Long> createDataCollectionProcedure(@RequestBody CreateDataCollectionProcedureRequest request) {
        try {
            Long procedureId = teacherProcedureCreationService.createDataCollectionProcedure(request);
            return ApiResponse.success(procedureId, "创建成功");
        } catch (com.example.demo.exception.BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("创建数据收集步骤失败", e);
            return ApiResponse.error(500, "创建失败: " + e.getMessage());
        }
    }

    /**
     * 创建题库练习步骤
     * 步骤类型：type=3（题库答题）
     *
     * @param request 创建题库练习步骤请求
     * @return 步骤ID
     */
    @PostMapping("/topic")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<Long> createTopicProcedure(@RequestBody CreateTopicProcedureRequest request) {
        try {
            Long procedureId = teacherProcedureCreationService.createTopicProcedure(request);
            return ApiResponse.success(procedureId, "创建成功");
        } catch (com.example.demo.exception.BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("创建题库练习步骤失败", e);
            return ApiResponse.error(500, "创建失败: " + e.getMessage());
        }
    }
}
