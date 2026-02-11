package com.example.demo.controller.student;

import com.example.demo.annotation.RequireRole;
import com.example.demo.enums.UserRole;
import com.example.demo.exception.BusinessException;
import com.example.demo.pojo.entity.StudentClassRelation;
import com.example.demo.pojo.request.ClassExperimentQueryRequest;
import com.example.demo.pojo.response.ApiResponse;
import com.example.demo.pojo.response.ClassExperimentDetailResponse;
import com.example.demo.pojo.response.PageResponse;
import com.example.demo.pojo.response.StudentExperimentDetailResponse;
import com.example.demo.service.ClassExperimentService;
import com.example.demo.service.StudentClassRelationService;
import com.example.demo.service.StudentExperimentService;
import com.example.demo.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学生实验控制器
 * 提供学生查询实验详情的接口
 */
@RequestMapping("/api/student/experiments")
@RestController
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class StudentExperimentController {

    private final StudentExperimentService studentExperimentService;
    private final ClassExperimentService classExperimentService;
    private final StudentClassRelationService studentClassRelationService;

    /**
     * 查询学生实验详情
     * 返回实验基本信息及步骤列表（包含是否可做标识）
     *
     * @param experimentId 实验ID
     * @param classCode    班级编号
     * @return 实验详情
     */
    @GetMapping("/{experimentId}")
    @RequireRole(value = UserRole.STUDENT)
    public ApiResponse<StudentExperimentDetailResponse> getExperimentDetail(
            @PathVariable("experimentId") Long experimentId,
            @RequestParam("classCode") String classCode) {
        try {
            // 获取当前登录学生用户名
            String studentUsername = com.example.demo.util.SecurityUtil.getCurrentUsername()
                    .orElseThrow(() -> new com.example.demo.exception.BusinessException(401, "未登录"));

            StudentExperimentDetailResponse response = studentExperimentService.getStudentExperimentDetail(
                    experimentId, classCode, studentUsername);

            return ApiResponse.success(response, "查询成功");
        } catch (com.example.demo.exception.BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("查询实验详情失败", e);
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 学生查询班级实验列表
     *
     * @param request 查询请求
     * @return 分页结果
     */
    @GetMapping("/class-experiments")
    @RequireRole(value = UserRole.STUDENT)
    public ApiResponse<PageResponse<ClassExperimentDetailResponse>> queryClassExperiments(
            ClassExperimentQueryRequest request) {
        try {
            // 获取当前登录学生用户名
            String studentUsername = SecurityUtil.getCurrentUsername()
                .orElseThrow(() -> new BusinessException(401, "未登录"));

            // 查询学生所属的班级列表
            List<StudentClassRelation> relations = studentClassRelationService
                .getByStudentUsername(studentUsername);
            List<String> classCodeList = relations.stream()
                .map(StudentClassRelation::getClassCode)
                .collect(java.util.stream.Collectors.toList());

            PageResponse<ClassExperimentDetailResponse> response = classExperimentService
                .queryClassExperimentsForStudent(classCodeList, request);

            return ApiResponse.success(response, "查询成功");
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("学生查询班级实验失败", e);
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }
}
