package com.example.demo.controller;

import com.example.demo.annotation.RequireRole;
import com.example.demo.enums.UserRole;
import com.example.demo.pojo.dto.ApiResponse;
import com.example.demo.pojo.entity.Experiment;
import com.example.demo.service.ExperimentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 实验管理控制器
 * 提供实验的查询、创建、更新、删除等接口
 */
@RequestMapping("/api/experiment")
@RestController
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ExperimentController {

    private final ExperimentService experimentService;

    /**
     * 根据ID查询实验
     * @param id 实验ID
     * @return 实验信息
     */
    @GetMapping("/{id}")
    public ApiResponse<Experiment> getById(@PathVariable Long id) {
        try {
            Experiment experiment = experimentService.getById(id);
            if (experiment == null) {
                return ApiResponse.error(404, "实验不存在");
            }
            return ApiResponse.success(experiment);
        } catch (Exception e) {
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据实验代码查询实验
     * @param experimentCode 实验代码
     * @return 实验信息
     */
    @GetMapping("/code/{experimentCode}")
    public ApiResponse<Experiment> getByExperimentCode(@PathVariable String experimentCode) {
        try {
            Experiment experiment = experimentService.getByExperimentCode(experimentCode);
            if (experiment == null) {
                return ApiResponse.error(404, "实验不存在");
            }
            return ApiResponse.success(experiment);
        } catch (Exception e) {
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 创建实验
     * 仅管理员可调用
     * @param experiment 实验信息
     * @return 创建结果
     */
    @RequireRole(value = UserRole.ADMIN)
    @PostMapping
    public ApiResponse<Experiment> create(@RequestBody Experiment experiment) {
        try {
            boolean success = experimentService.save(experiment);
            if (success) {
                return ApiResponse.success(experiment, "实验创建成功");
            } else {
                return ApiResponse.error(500, "实验创建失败");
            }
        } catch (Exception e) {
            return ApiResponse.error(500, "创建失败: " + e.getMessage());
        }
    }

    /**
     * 更新实验信息
     * 仅管理员可调用
     * @param id 实验ID
     * @param experiment 实验信息
     * @return 更新结果
     */
    @RequireRole(value = UserRole.ADMIN)
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody Experiment experiment) {
        try {
            experiment.setId(id);
            boolean success = experimentService.updateById(experiment);
            if (success) {
                return ApiResponse.success(null, "实验更新成功");
            } else {
                return ApiResponse.error(404, "实验不存在");
            }
        } catch (Exception e) {
            return ApiResponse.error(500, "更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除实验
     * 仅管理员可调用
     * @param id 实验ID
     * @return 删除结果
     */
    @RequireRole(value = UserRole.ADMIN)
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        try {
            boolean success = experimentService.removeById(id);
            if (success) {
                return ApiResponse.success(null, "实验删除成功");
            } else {
                return ApiResponse.error(404, "实验不存在");
            }
        } catch (Exception e) {
            return ApiResponse.error(500, "删除失败: " + e.getMessage());
        }
    }
}