package com.example.demo.controller;

import com.example.demo.annotation.RequireRole;
import com.example.demo.enums.UserRole;
import com.example.demo.pojo.dto.ApiResponse;
import com.example.demo.pojo.entity.StudentClassRelation;
import com.example.demo.service.StudentClassRelationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学生班级关系管理控制器
 * 提供学生班级关系的查询、创建、删除等接口
 */
@RequestMapping("/api/student-class-relation")
@RestController
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class StudentClassRelationController {

    private final StudentClassRelationService studentClassRelationService;

    /**
     * 根据ID查询学生班级关系
     * @param id 关系ID
     * @return 学生班级关系信息
     */
    @GetMapping("/{id}")
    public ApiResponse<StudentClassRelation> getById(@PathVariable Long id) {
        try {
            StudentClassRelation relation = studentClassRelationService.getById(id);
            if (relation == null) {
                return ApiResponse.error(404, "学生班级关系不存在");
            }
            return ApiResponse.success(relation);
        } catch (Exception e) {
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据学生用户名查询班级关系列表
     * @param studentUsername 学生用户名
     * @return 班级关系列表
     */
    @GetMapping("/student/{studentUsername}")
    public ApiResponse<List<StudentClassRelation>> getByStudentUsername(@PathVariable String studentUsername) {
        try {
            List<StudentClassRelation> relations = studentClassRelationService.getByStudentUsername(studentUsername);
            return ApiResponse.success(relations);
        } catch (Exception e) {
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据班级代码查询学生列表
     * @param classCode 班级代码
     * @return 学生列表
     */
    @GetMapping("/class/{classCode}")
    public ApiResponse<List<StudentClassRelation>> getByClassCode(@PathVariable String classCode) {
        try {
            List<StudentClassRelation> relations = studentClassRelationService.getByClassCode(classCode);
            return ApiResponse.success(relations);
        } catch (Exception e) {
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 创建学生班级关系
     * 仅管理员可调用
     * @param relation 学生班级关系信息
     * @return 创建结果
     */
    @RequireRole(value = UserRole.ADMIN)
    @PostMapping
    public ApiResponse<StudentClassRelation> create(@RequestBody StudentClassRelation relation) {
        try {
            boolean success = studentClassRelationService.save(relation);
            if (success) {
                return ApiResponse.success(relation, "学生班级关系创建成功");
            } else {
                return ApiResponse.error(500, "学生班级关系创建失败");
            }
        } catch (Exception e) {
            return ApiResponse.error(500, "创建失败: " + e.getMessage());
        }
    }

    /**
     * 删除学生班级关系
     * 仅管理员可调用
     * @param id 关系ID
     * @return 删除结果
     */
    @RequireRole(value = UserRole.ADMIN)
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        try {
            boolean success = studentClassRelationService.removeById(id);
            if (success) {
                return ApiResponse.success(null, "学生班级关系删除成功");
            } else {
                return ApiResponse.error(404, "学生班级关系不存在");
            }
        } catch (Exception e) {
            return ApiResponse.error(500, "删除失败: " + e.getMessage());
        }
    }
}