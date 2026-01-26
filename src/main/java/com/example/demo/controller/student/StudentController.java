package com.example.demo.controller.student;

import com.example.demo.annotation.RequireRole;
import com.example.demo.enums.UserRole;
import com.example.demo.pojo.entity.Class;
import com.example.demo.pojo.request.BindClassRequest;
import com.example.demo.pojo.response.ApiResponse;
import com.example.demo.pojo.response.ClassInfoResponse;
import com.example.demo.service.StudentClassRelationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学生控制器
 * 提供学生端的接口
 */
@RequestMapping("/api/student")
@RestController
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class StudentController {

    private final StudentClassRelationService studentClassRelationService;

    /**
     * 绑定班级
     * 学生通过班级验证码绑定到指定班级
     *
     * @param request 绑定班级请求
     * @return 绑定结果
     */
    @PostMapping("/bind-class")
    @RequireRole(value = UserRole.STUDENT)
    public ApiResponse<Class> bindClass(@RequestBody BindClassRequest request) {
        try {
            String studentUsername = com.example.demo.util.SecurityUtil.getCurrentUsername()
                    .orElseThrow(() -> new com.example.demo.exception.BusinessException(401, "未登录"));

            if (request.getVerificationCode() == null || request.getVerificationCode().trim().isEmpty()) {
                return ApiResponse.error(400, "验证码不能为空");
            }

            Class clazz = studentClassRelationService.bindClass(studentUsername, request.getVerificationCode());
            return ApiResponse.success(clazz, "绑定班级成功");
        } catch (com.example.demo.exception.BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("绑定班级失败", e);
            return ApiResponse.error(500, "绑定失败: " + e.getMessage());
        }
    }

    /**
     * 查询班级列表
     * 查询当前登录学生已绑定的所有班级
     *
     * @return 班级列表
     */
    @GetMapping("/classes")
    @RequireRole(value = UserRole.STUDENT)
    public ApiResponse<List<ClassInfoResponse>> getClasses() {
        try {
            String studentUsername = com.example.demo.util.SecurityUtil.getCurrentUsername()
                    .orElseThrow(() -> new com.example.demo.exception.BusinessException(401, "未登录"));

            List<ClassInfoResponse> classes = studentClassRelationService.getStudentClassInfoList(studentUsername);
            return ApiResponse.success(classes, "获取班级列表成功");
        } catch (Exception e) {
            log.error("查询班级列表失败", e);
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }
}
