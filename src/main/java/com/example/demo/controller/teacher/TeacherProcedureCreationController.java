package com.example.demo.controller.teacher;

import com.example.demo.annotation.RequireRole;
import com.example.demo.enums.UserRole;
import com.example.demo.pojo.request.teacher.CreateDataCollectionProcedureRequest;
import com.example.demo.pojo.request.teacher.CreateTopicProcedureRequest;
import com.example.demo.pojo.request.teacher.CreateVideoProcedureRequest;
import com.example.demo.pojo.request.teacher.InsertDataCollectionProcedureRequest;
import com.example.demo.pojo.request.teacher.InsertTopicProcedureRequest;
import com.example.demo.pojo.request.teacher.InsertVideoProcedureRequest;
import com.example.demo.pojo.request.teacher.UpdateDataCollectionProcedureRequest;
import com.example.demo.pojo.request.teacher.UpdateTopicProcedureRequest;
import com.example.demo.pojo.request.teacher.UpdateVideoProcedureRequest;
import com.example.demo.pojo.response.ApiResponse;
import com.example.demo.pojo.response.TeacherProcedureDetailResponse;
import com.example.demo.service.TeacherProcedureCreationService;
import com.example.demo.service.TeacherProcedureQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    private final TeacherProcedureQueryService teacherProcedureQueryService;

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

    /**
     * 修改视频观看步骤
     * 步骤类型：type=1（观看视频）
     *
     * @param request 修改视频步骤请求
     * @return 是否修改成功
     */
    @PutMapping("/video")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<Void> updateVideoProcedure(@RequestBody UpdateVideoProcedureRequest request) {
        try {
            teacherProcedureCreationService.updateVideoProcedure(request);
            return ApiResponse.success(null, "修改成功");
        } catch (com.example.demo.exception.BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("修改视频观看步骤失败", e);
            return ApiResponse.error(500, "修改失败: " + e.getMessage());
        }
    }

    /**
     * 修改数据收集步骤
     * 步骤类型：type=2（数据收集）
     *
     * @param request 修改数据收集步骤请求
     * @return 是否修改成功
     */
    @PutMapping("/data-collection")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<Void> updateDataCollectionProcedure(@RequestBody UpdateDataCollectionProcedureRequest request) {
        try {
            teacherProcedureCreationService.updateDataCollectionProcedure(request);
            return ApiResponse.success(null, "修改成功");
        } catch (com.example.demo.exception.BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("修改数据收集步骤失败", e);
            return ApiResponse.error(500, "修改失败: " + e.getMessage());
        }
    }

    /**
     * 修改题库练习步骤
     * 步骤类型：type=3（题库答题）
     *
     * @param request 修改题库练习步骤请求
     * @return 是否修改成功
     */
    @PutMapping("/topic")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<Void> updateTopicProcedure(@RequestBody UpdateTopicProcedureRequest request) {
        try {
            teacherProcedureCreationService.updateTopicProcedure(request);
            return ApiResponse.success(null, "修改成功");
        } catch (com.example.demo.exception.BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("修改题库练习步骤失败", e);
            return ApiResponse.error(500, "修改失败: " + e.getMessage());
        }
    }

    /**
     * 插入视频观看步骤
     * 步骤类型：type=1（观看视频）
     *
     * @param request 插入视频步骤请求
     * @return 步骤ID
     */
    @PostMapping("/video/insert")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<Long> insertVideoProcedure(@RequestBody InsertVideoProcedureRequest request) {
        try {
            Long procedureId = teacherProcedureCreationService.insertVideoProcedure(request);
            return ApiResponse.success(procedureId, "插入成功");
        } catch (com.example.demo.exception.BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("插入视频观看步骤失败", e);
            return ApiResponse.error(500, "插入失败: " + e.getMessage());
        }
    }

    /**
     * 插入数据收集步骤
     * 步骤类型：type=2（数据收集）
     *
     * @param request 插入数据收集步骤请求
     * @return 步骤ID
     */
    @PostMapping("/data-collection/insert")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<Long> insertDataCollectionProcedure(@RequestBody InsertDataCollectionProcedureRequest request) {
        try {
            Long procedureId = teacherProcedureCreationService.insertDataCollectionProcedure(request);
            return ApiResponse.success(procedureId, "插入成功");
        } catch (com.example.demo.exception.BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("插入数据收集步骤失败", e);
            return ApiResponse.error(500, "插入失败: " + e.getMessage());
        }
    }

    /**
     * 插入题库练习步骤
     * 步骤类型：type=3（题库答题）
     *
     * @param request 插入题库练习步骤请求
     * @return 步骤ID
     */
    @PostMapping("/topic/insert")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<Long> insertTopicProcedure(@RequestBody InsertTopicProcedureRequest request) {
        try {
            Long procedureId = teacherProcedureCreationService.insertTopicProcedure(request);
            return ApiResponse.success(procedureId, "插入成功");
        } catch (com.example.demo.exception.BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("插入题库练习步骤失败", e);
            return ApiResponse.error(500, "插入失败: " + e.getMessage());
        }
    }

    /**
     * 删除指定步骤
     *
     * @param procedureId 步骤ID
     * @return 是否删除成功
     */
    @DeleteMapping("/{procedureId}")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<Void> deleteProcedure(@PathVariable("procedureId") Long procedureId) {
        try {
            teacherProcedureCreationService.deleteProcedure(procedureId);
            return ApiResponse.success(null, "删除成功");
        } catch (com.example.demo.exception.BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("删除步骤失败", e);
            return ApiResponse.error(500, "删除失败: " + e.getMessage());
        }
    }

    /**
     * 查询单个步骤详情
     *
     * @param procedureId       步骤ID
     * @param classExperimentId 班级实验ID(可选,用于查询时间配置)
     * @return 步骤详情
     */
    @GetMapping("/{procedureId}")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<TeacherProcedureDetailResponse> getProcedureDetail(
            @PathVariable("procedureId") Long procedureId,
            @RequestParam(value = "classExperimentId", required = false) Long classExperimentId) {
        try {
            TeacherProcedureDetailResponse response = teacherProcedureQueryService.getProcedureDetail(procedureId, classExperimentId);
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
     * @param experimentId      实验ID
     * @param classExperimentId 班级实验ID(可选,用于查询时间配置)
     * @return 步骤详情列表
     */
    @GetMapping("/experiment/{experimentId}")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<List<TeacherProcedureDetailResponse>> getExperimentProcedures(
            @PathVariable("experimentId") Long experimentId,
            @RequestParam(value = "classExperimentId", required = false) Long classExperimentId) {
        try {
            List<TeacherProcedureDetailResponse> responses = teacherProcedureQueryService.getExperimentProcedures(experimentId, classExperimentId);
            return ApiResponse.success(responses, "查询成功");
        } catch (com.example.demo.exception.BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("查询实验步骤列表失败", e);
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }
}
