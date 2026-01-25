package com.example.demo.controller;

import com.example.demo.annotation.RequireRole;
import com.example.demo.enums.UserRole;
import com.example.demo.pojo.dto.ApiResponse;
import com.example.demo.pojo.entity.Class;
import com.example.demo.service.ClassService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 班级管理控制器
 * 提供班级的查询、创建、更新、删除等接口
 */
@RequestMapping("/api/class")
@RestController
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ClassController {

    private final ClassService classService;

    /**
     * 根据ID查询班级
     * @param id 班级ID
     * @return 班级信息
     */
    @GetMapping("/{id}")
    public ApiResponse<Class> getById(@PathVariable Long id) {
        try {
            Class clazz = classService.getById(id);
            if (clazz == null) {
                return ApiResponse.error(404, "班级不存在");
            }
            return ApiResponse.success(clazz);
        } catch (Exception e) {
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据班级代码查询班级
     * @param classCode 班级代码
     * @return 班级信息
     */
    @GetMapping("/code/{classCode}")
    public ApiResponse<Class> getByClassCode(@PathVariable String classCode) {
        try {
            Class clazz = classService.getByClassCode(classCode);
            if (clazz == null) {
                return ApiResponse.error(404, "班级不存在");
            }
            return ApiResponse.success(clazz);
        } catch (Exception e) {
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 创建班级
     * 仅管理员可调用
     * @param clazz 班级信息
     * @return 创建结果
     */
    @RequireRole(value = UserRole.ADMIN)
    @PostMapping
    public ApiResponse<Class> create(@RequestBody Class clazz) {
        try {
            boolean success = classService.save(clazz);
            if (success) {
                return ApiResponse.success(clazz, "班级创建成功");
            } else {
                return ApiResponse.error(500, "班级创建失败");
            }
        } catch (Exception e) {
            return ApiResponse.error(500, "创建失败: " + e.getMessage());
        }
    }

    /**
     * 更新班级信息
     * 仅管理员可调用
     * @param id 班级ID
     * @param clazz 班级信息
     * @return 更新结果
     */
    @RequireRole(value = UserRole.ADMIN)
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody Class clazz) {
        try {
            clazz.setId(id);
            boolean success = classService.updateById(clazz);
            if (success) {
                return ApiResponse.success(null, "班级更新成功");
            } else {
                return ApiResponse.error(404, "班级不存在");
            }
        } catch (Exception e) {
            return ApiResponse.error(500, "更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除班级
     * 仅管理员可调用
     * @param id 班级ID
     * @return 删除结果
     */
    @RequireRole(value = UserRole.ADMIN)
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        try {
            boolean success = classService.removeById(id);
            if (success) {
                return ApiResponse.success(null, "班级删除成功");
            } else {
                return ApiResponse.error(404, "班级不存在");
            }
        } catch (Exception e) {
            return ApiResponse.error(500, "删除失败: " + e.getMessage());
        }
    }
}