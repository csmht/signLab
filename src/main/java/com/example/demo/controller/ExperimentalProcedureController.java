package com.example.demo.controller;

import com.example.demo.annotation.RequireRole;
import com.example.demo.enums.UserRole;
import com.example.demo.pojo.dto.ApiResponse;
import com.example.demo.pojo.entity.ExperimentalProcedure;
import com.example.demo.service.ExperimentalProcedureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 实验步骤管理控制器
 * 提供实验步骤的查询、创建、更新、删除等接口
 */
@RequestMapping("/api/experimental-procedure")
@RestController
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ExperimentalProcedureController {

    private final ExperimentalProcedureService experimentalProcedureService;

    /**
     * 根据ID查询实验步骤
     * @param id 实验步骤ID
     * @return 实验步骤信息
     */
    @GetMapping("/{id}")
    public ApiResponse<ExperimentalProcedure> getById(@PathVariable Long id) {
        try {
            ExperimentalProcedure procedure = experimentalProcedureService.getById(id);
            if (procedure == null) {
                return ApiResponse.error(404, "实验步骤不存在");
            }
            return ApiResponse.success(procedure);
        } catch (Exception e) {
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据实验ID查询实验步骤列表
     * @param experimentId 实验ID
     * @return 实验步骤列表
     */
    @GetMapping("/experiment/{experimentId}")
    public ApiResponse<List<ExperimentalProcedure>> getByExperimentId(@PathVariable Long experimentId) {
        try {
            List<ExperimentalProcedure> procedures = experimentalProcedureService.getByExperimentId(experimentId);
            return ApiResponse.success(procedures);
        } catch (Exception e) {
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 创建实验步骤
     * 仅管理员可调用
     * @param procedure 实验步骤信息
     * @return 创建结果
     */
    @RequireRole(value = UserRole.ADMIN)
    @PostMapping
    public ApiResponse<ExperimentalProcedure> create(@RequestBody ExperimentalProcedure procedure) {
        try {
            boolean success = experimentalProcedureService.save(procedure);
            if (success) {
                return ApiResponse.success(procedure, "实验步骤创建成功");
            } else {
                return ApiResponse.error(500, "实验步骤创建失败");
            }
        } catch (Exception e) {
            return ApiResponse.error(500, "创建失败: " + e.getMessage());
        }
    }

    /**
     * 更新实验步骤信息
     * 仅管理员可调用
     * @param id 实验步骤ID
     * @param procedure 实验步骤信息
     * @return 更新结果
     */
    @RequireRole(value = UserRole.ADMIN)
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody ExperimentalProcedure procedure) {
        try {
            procedure.setId(id);
            boolean success = experimentalProcedureService.updateById(procedure);
            if (success) {
                return ApiResponse.success(null, "实验步骤更新成功");
            } else {
                return ApiResponse.error(404, "实验步骤不存在");
            }
        } catch (Exception e) {
            return ApiResponse.error(500, "更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除实验步骤
     * 仅管理员可调用
     * @param id 实验步骤ID
     * @return 删除结果
     */
    @RequireRole(value = UserRole.ADMIN)
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        try {
            boolean success = experimentalProcedureService.removeById(id);
            if (success) {
                return ApiResponse.success(null, "实验步骤删除成功");
            } else {
                return ApiResponse.error(404, "实验步骤不存在");
            }
        } catch (Exception e) {
            return ApiResponse.error(500, "删除失败: " + e.getMessage());
        }
    }
}