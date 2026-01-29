package com.example.demo.controller.teacher;

import com.example.demo.annotation.RequireRole;
import com.example.demo.enums.UserRole;
import com.example.demo.pojo.entity.ClassExperiment;
import com.example.demo.pojo.entity.ExperimentalProcedure;
import com.example.demo.pojo.request.teacher.SetProcedureTimeRequest;
import com.example.demo.pojo.request.teacher.SetProcedureTimesBatchRequest;
import com.example.demo.pojo.response.ApiResponse;
import com.example.demo.pojo.response.ProcedureTimeResponse;
import com.example.demo.service.ClassExperimentProcedureTimeService;
import com.example.demo.service.ClassExperimentService;
import com.example.demo.service.ExperimentalProcedureService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 教师步骤时间配置控制器
 */
@RestController
@RequestMapping("/api/teacher/procedure-time")
@RequiredArgsConstructor
public class TeacherProcedureTimeConfigurationController {

    private final ClassExperimentProcedureTimeService timeService;
    private final ClassExperimentService classExperimentService;
    private final ExperimentalProcedureService procedureService;

    /**
     * 设置单个步骤的时间
     */
    @PutMapping
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<Void> setProcedureTime(@RequestBody SetProcedureTimeRequest request) {
        // 1. 验证班级实验是否存在
        ClassExperiment classExperiment = classExperimentService.getById(request.getClassExperimentId());
        if (classExperiment == null) {
            return ApiResponse.error(404, "班级实验不存在");
        }

        // 2. 验证实验步骤是否存在
        ExperimentalProcedure procedure = procedureService.getById(request.getProcedureId());
        if (procedure == null) {
            return ApiResponse.error(404, "实验步骤不存在");
        }

        // 3. 验证时间范围
        if (request.getStartTime().isAfter(request.getEndTime())) {
            return ApiResponse.error(400, "开始时间不能晚于结束时间");
        }

        // 4. 保存或更新时间配置
        timeService.saveOrUpdateTime(
                request.getClassExperimentId(),
                request.getProcedureId(),
                request.getStartTime(),
                request.getEndTime()
        );

        return ApiResponse.success(null, "设置成功");
    }

    /**
     * 批量设置班级实验的步骤时间
     */
    @PostMapping("/batch")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<Void> setProcedureTimesBatch(@RequestBody SetProcedureTimesBatchRequest request) {
        // 1. 验证班级实验是否存在
        ClassExperiment classExperiment = classExperimentService.getById(request.getClassExperimentId());
        if (classExperiment == null) {
            return ApiResponse.error(404, "班级实验不存在");
        }

        // 2. 验证并保存每个步骤的时间配置
        for (SetProcedureTimesBatchRequest.ProcedureTimeItem item : request.getProcedureTimes()) {
            // 验证实验步骤是否存在
            ExperimentalProcedure procedure = procedureService.getById(item.getProcedureId());
            if (procedure == null) {
                return ApiResponse.error(404, "实验步骤不存在: " + item.getProcedureId());
            }

            // 验证时间范围
            if (item.getStartTime().isAfter(item.getEndTime())) {
                return ApiResponse.error(400, "步骤 " + item.getProcedureId() + " 的开始时间不能晚于结束时间");
            }

            // 保存时间配置
            timeService.saveOrUpdateTime(
                    request.getClassExperimentId(),
                    item.getProcedureId(),
                    item.getStartTime(),
                    item.getEndTime()
            );
        }

        return ApiResponse.success(null, "批量设置成功");
    }

    /**
     * 删除步骤时间配置
     */
    @DeleteMapping("/{classExperimentId}/{procedureId}")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<Void> deleteProcedureTime(
            @PathVariable("classExperimentId") Long classExperimentId,
            @PathVariable("procedureId") Long procedureId) {
        timeService.deleteTime(classExperimentId, procedureId);
        return ApiResponse.success(null, "删除成功");
    }

    /**
     * 查询班级实验的所有步骤时间配置
     */
    @GetMapping("/{classExperimentId}")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<List<ProcedureTimeResponse>> getProcedureTimes(
            @PathVariable("classExperimentId") Long classExperimentId) {

        // 1. 查询班级实验信息
        ClassExperiment classExperiment = classExperimentService.getById(classExperimentId);
        if (classExperiment == null) {
            return ApiResponse.error(404, "班级实验不存在");
        }

        // 2. 查询实验的所有步骤
        Long experimentId = Long.parseLong(classExperiment.getExperimentId());
        List<ExperimentalProcedure> procedures = procedureService.getByExperimentId(experimentId);

        // 3. 查询所有步骤时间配置
        var procedureTimes = timeService.listByClassExperiment(classExperimentId)
                .stream()
                .collect(Collectors.toMap(
                        t -> t.getExperimentalProcedureId(),
                        t -> t
                ));

        // 4. 组装响应数据
        List<ProcedureTimeResponse> responses = new ArrayList<>();
        for (ExperimentalProcedure procedure : procedures) {
            ProcedureTimeResponse response = new ProcedureTimeResponse();
            response.setProcedureId(procedure.getId());
            response.setProcedureNumber(procedure.getNumber());
            response.setProcedureRemark(procedure.getRemark());

            // 获取时间配置
            var procedureTime = procedureTimes.get(procedure.getId());
            if (procedureTime != null) {
                response.setStartTime(procedureTime.getStartTime());
                response.setEndTime(procedureTime.getEndTime());
            } else {
                // 如果未配置时间，返回空值
                response.setStartTime(null);
                response.setEndTime(null);
            }

            responses.add(response);
        }

        return ApiResponse.success(responses, "查询成功");
    }
}
