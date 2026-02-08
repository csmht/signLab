package com.example.demo.controller.teacher;

import com.example.demo.annotation.RequireRole;
import com.example.demo.enums.UserRole;
import com.example.demo.exception.BusinessException;
import com.example.demo.pojo.entity.Class;
import com.example.demo.pojo.entity.StudentClassRelation;
import com.example.demo.pojo.request.*;
import com.example.demo.pojo.response.*;
import com.example.demo.service.ClassExperimentClassRelationService;
import com.example.demo.service.ClassExperimentService;
import com.example.demo.service.ClassService;
import com.example.demo.service.StudentClassRelationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 教师班级管理控制器
 * 提供班级的查询、创建、批量添加、学生绑定等接口
 */
@RequestMapping("/api/teacher/class")
@RestController("teacherClassController")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ClassController {

    private final ClassService classService;
    private final StudentClassRelationService studentClassRelationService;
    private final ClassExperimentService classExperimentService;
    private final ClassExperimentClassRelationService classExperimentClassRelationService;

    /**
     * 查询班级列表（分页或列表）
     * 支持根据班级代码、班级名称、创建者进行查询
     *
     * @param request 查询请求
     * @return 查询结果
     */
    @PostMapping("/query")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<PageResponse<ClassResponse>> queryClasses(@RequestBody ClassQueryRequest request) {
        try {
            PageResponse<ClassResponse> response = classService.queryClasses(request);
            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("查询班级列表失败", e);
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID查询班级（包含实验信息）
     *
     * @param id 班级ID
     * @return 班级信息（包含实验信息）
     */
    @GetMapping("/{id}")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<ClassWithExperimentsResponse> getById(@PathVariable Long id) {
        try {
            ClassWithExperimentsResponse response = classService.getClassWithExperimentsById(id);
            return ApiResponse.success(response);
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据班级代码查询班级（包含实验信息）
     *
     * @param classCode 班级代码
     * @return 班级信息（包含实验信息）
     */
    @GetMapping("/code/{classCode}")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<ClassWithExperimentsResponse> getByClassCode(@PathVariable String classCode) {
        try {
            ClassWithExperimentsResponse response = classService.getClassWithExperimentsByCode(classCode);
            return ApiResponse.success(response);
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 创建班级(单个)
     *
     * @param request 创建班级请求
     * @return 创建结果
     */
    @PostMapping
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<Class> create(@RequestBody CreateClassRequest request) {
        try {
            // 如果未传入班级编号,则自动生成
            String classCode = request.getClassCode();
            if (!org.springframework.util.StringUtils.hasText(classCode)) {
                classCode = classService.generateClassCode();
            } else {
                // 如果传入了班级编号,检查是否已存在
                Class existingClass = classService.getByClassCode(classCode);
                if (existingClass != null) {
                    return ApiResponse.error(400, "班级编号已存在");
                }
            }

            // 创建班级
            Class clazz = new Class();
            clazz.setClassCode(classCode);
            clazz.setClassName(request.getClassName());

            // 设置创建者
            String currentUsername = com.example.demo.util.SecurityUtil.getCurrentUsername().orElse(null);
            clazz.setCreator(currentUsername);

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
     * 批量添加班级
     *
     * @param request 批量添加班级请求
     * @return 批量添加结果
     */
    @PostMapping("/batch")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<BatchAddClassResponse> batchAddClasses(@RequestBody BatchAddClassRequest request) {
        try {
            BatchAddClassResponse response = classService.batchAddClasses(request);
            // 判断返回码：全部成功返回200，部分成功返回201，全部失败返回400
            if (response.getSuccessCount() > 0 && response.getFailCount() > 0) {
                return new ApiResponse<BatchAddClassResponse>(201, "部分成功，成功: " + response.getSuccessCount() + "，失败: " + response.getFailCount(), response);
            } else if (response.getSuccessCount() == 0) {
                return ApiResponse.error(400, "批量添加失败，全部" + response.getFailCount() + "条记录失败");
            } else {
                return ApiResponse.success(response, "批量添加完成");
            }
        } catch (Exception e) {
            log.error("批量添加班级失败", e);
            return ApiResponse.error(500, "批量添加失败: " + e.getMessage());
        }
    }

    /**
     * 更新班级信息
     *
     * @param id 班级ID
     * @param request 更新班级请求
     * @return 更新结果
     */
    @PutMapping("/{id}")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody UpdateClassRequest request) {
        try {
            // 查询班级是否存在
            Class existingClass = classService.getById(id);
            if (existingClass == null) {
                return ApiResponse.error(404, "班级不存在");
            }

            // 更新班级信息
            if (request.getClassName() != null) {
                existingClass.setClassName(request.getClassName());
            }

            boolean success = classService.updateById(existingClass);
            if (success) {
                return ApiResponse.success(null, "班级更新成功");
            } else {
                return ApiResponse.error(500, "班级更新失败");
            }
        } catch (Exception e) {
            return ApiResponse.error(500, "更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除班级
     *
     * @param id 班级ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @RequireRole(value = UserRole.TEACHER)
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

    /**
     * 查询班级的所有学生（分页）
     *
     * @param classCode 班级代码
     * @param request   查询请求
     * @return 学生列表
     */
    @PostMapping("/{classCode}/students")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<PageResponse<StudentClassRelation>> getStudents(
            @PathVariable String classCode,
            @RequestBody com.example.demo.pojo.request.StudentQueryRequest request) {
        try {
            PageResponse<StudentClassRelation> response = studentClassRelationService.getStudentsByClassCodePage(
                    classCode, request);
            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("查询学生列表失败", e);
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 批量绑定学生到班级
     *
     * @param request 批量绑定学生请求
     * @return 绑定结果
     */
    @PostMapping("/{classCode}/bind-students")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<BatchBindStudentsResponse> batchBindStudents(
            @PathVariable String classCode,
            @RequestBody BatchBindStudentsRequest request) {
        try {
            request.setClassCode(classCode);
            BatchBindStudentsResponse response = studentClassRelationService.batchBindStudents(request);
            // 判断返回码：全部成功返回200，部分成功返回201，全部失败返回400
            if (response.getSuccessCount() > 0 && response.getFailCount() > 0) {
                return new ApiResponse<BatchBindStudentsResponse>(201, "部分成功，成功: " + response.getSuccessCount() + "，失败: " + response.getFailCount(), response);
            } else if (response.getSuccessCount() == 0) {
                return ApiResponse.error(400, "批量绑定失败，全部" + response.getFailCount() + "条记录失败");
            } else {
                return ApiResponse.success(response, "批量绑定完成");
            }
        } catch (Exception e) {
            log.error("批量绑定学生失败", e);
            return ApiResponse.error(500, "批量绑定失败: " + e.getMessage());
        }
    }

    /**
     * 批量解绑学生
     *
     * @param classCode 班级代码
     * @param studentUsernames 学生用户名列表
     * @return 解绑结果
     */
    @PostMapping("/{classCode}/unbind-students")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<Integer> batchUnbindStudents(
            @PathVariable String classCode,
            @RequestBody List<String> studentUsernames) {
        try {
            int count = studentClassRelationService.batchUnbindStudents(classCode, studentUsernames);
            return ApiResponse.success(count, "解绑成功，共解绑 " + count + " 名学生");
        } catch (Exception e) {
            return ApiResponse.error(500, "解绑失败: " + e.getMessage());
        }
    }

    /**
     * 批量绑定班级到实验
     *
     * @param request 批量绑定班级到实验请求
     * @return 绑定结果
     */
    @PostMapping("/bind-experiment")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<BatchBindClassesToExperimentResponse> batchBindClassesToExperiment(
            @RequestBody BatchBindClassesToExperimentRequest request) {
        try {
            BatchBindClassesToExperimentResponse response = classExperimentService.batchBindClassesToExperiment(request);
            return ApiResponse.success(response, "批量绑定完成");
        } catch (Exception e) {
            log.error("批量绑定班级到实验失败", e);
            return ApiResponse.error(500, "批量绑定失败: " + e.getMessage());
        }
    }

    /**
     * 查询教师的课次列表（分页）
     * 按照实验开始时间降序排序
     *
     * @param request 查询请求
     * @return 课次列表
     */
    @PostMapping("/course-sessions")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<PageResponse<CourseSessionResponse>> getCourseSessions(
            @RequestBody com.example.demo.pojo.request.CourseSessionQueryRequest request) {
        try {
            String teacherUsername = com.example.demo.util.SecurityUtil.getCurrentUsername()
                    .orElseThrow(() -> new com.example.demo.exception.BusinessException(401, "未登录"));

            PageResponse<CourseSessionResponse> sessions = classExperimentService.getCourseSessionsForTeacher(
                    teacherUsername, request);

            return ApiResponse.success(sessions, "查询成功");
        } catch (com.example.demo.exception.BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("查询课次列表失败", e);
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 批量解绑班级
     *
     * @param experimentId 实验ID
     * @param classCodes 班级编号列表
     * @return 解绑结果
     */
    @PostMapping("/unbind-experiment/{experimentId}")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<Integer> batchUnbindClasses(
            @PathVariable String experimentId,
            @RequestBody List<String> classCodes) {
        try {
            int count = classExperimentService.batchUnbindClasses(experimentId, classCodes);
            return ApiResponse.success(count, "解绑成功，共解绑 " + count + " 个班级");
        } catch (Exception e) {
            return ApiResponse.error(500, "解绑失败: " + e.getMessage());
        }
    }

    /**
     * 查询指定实验参与的所有班级
     *
     * @param experimentId 实验ID
     * @return 班级列表
     */
    @GetMapping("/by-experiment/{experimentId}")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<List<Class>> getClassesByExperimentId(@PathVariable String experimentId) {
        try {
            List<Class> classes = classExperimentClassRelationService.getClassesByExperimentId(experimentId);
            return ApiResponse.success(classes, "查询成功");
        } catch (Exception e) {
            log.error("查询实验班级列表失败", e);
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

}