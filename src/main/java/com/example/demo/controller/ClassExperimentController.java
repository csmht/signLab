package com.example.demo.controller;

import com.example.demo.annotation.RequireRole;
import com.example.demo.enums.UserRole;
import com.example.demo.pojo.dto.ApiResponse;
import com.example.demo.pojo.entity.ClassExperiment;
import com.example.demo.service.ClassExperimentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 班级实验管理控制器
 * 提供班级实验的查询、创建、更新、删除等接口
 */
@RequestMapping("/api/class-experiment")
@RestController
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ClassExperimentController {

    private final ClassExperimentService classExperimentService;

    /**
     * 根据ID查询班级实验
     * @param id 班级实验ID
     * @return 班级实验信息
     */
    @GetMapping("/{id}")
    public ApiResponse<ClassExperiment> getById(@PathVariable Long id) {
        try {
            ClassExperiment classExperiment = classExperimentService.getById(id);
            if (classExperiment == null) {
                return ApiResponse.error(404, "班级实验不存在");
            }
            return ApiResponse.success(classExperiment);
        } catch (Exception e) {
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据班级代码查询班级实验
     * @param classCode 班级代码
     * @return 班级实验信息
     */
    @GetMapping("/class/{classCode}")
    public ApiResponse<ClassExperiment> getByClassCode(@PathVariable String classCode) {
        try {
            ClassExperiment classExperiment = classExperimentService.getByClassCode(classCode);
            if (classExperiment == null) {
                return ApiResponse.error(404, "班级实验不存在");
            }
            return ApiResponse.success(classExperiment);
        } catch (Exception e) {
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 创建班级实验
     * 仅管理员可调用
     * @param classExperiment 班级实验信息
     * @return 创建结果
     */
    @RequireRole(value = UserRole.ADMIN)
    @PostMapping
    public ApiResponse<ClassExperiment> create(@RequestBody ClassExperiment classExperiment) {
        try {
            boolean success = classExperimentService.save(classExperiment);
            if (success) {
                return ApiResponse.success(classExperiment, "班级实验创建成功");
            } else {
                return ApiResponse.error(500, "班级实验创建失败");
            }
        } catch (Exception e) {
            return ApiResponse.error(500, "创建失败: " + e.getMessage());
        }
    }

    /**
     * 更新班级实验信息
     * 仅管理员可调用
     * @param id 班级实验ID
     * @param classExperiment 班级实验信息
     * @return 更新结果
     */
    @RequireRole(value = UserRole.ADMIN)
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody ClassExperiment classExperiment) {
        try {
            classExperiment.setId(id);
            boolean success = classExperimentService.updateById(classExperiment);
            if (success) {
                return ApiResponse.success(null, "班级实验更新成功");
            } else {
                return ApiResponse.error(404, "班级实验不存在");
            }
        } catch (Exception e) {
            return ApiResponse.error(500, "更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除班级实验
     * 仅管理员可调用
     * @param id 班级实验ID
     * @return 删除结果
     */
    @RequireRole(value = UserRole.ADMIN)
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        try {
            boolean success = classExperimentService.removeById(id);
            if (success) {
                return ApiResponse.success(null, "班级实验删除成功");
            } else {
                return ApiResponse.error(404, "班级实验不存在");
            }
        } catch (Exception e) {
            return ApiResponse.error(500, "删除失败: " + e.getMessage());
        }
    }
}