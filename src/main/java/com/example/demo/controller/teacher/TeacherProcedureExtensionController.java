package com.example.demo.controller.teacher;

import com.example.demo.annotation.RequireRole;
import com.example.demo.enums.UserRole;
import com.example.demo.exception.BusinessException;
import com.example.demo.pojo.entity.StudentProcedureExtension;
import com.example.demo.pojo.request.teacher.BatchExtendProcedureTimeRequest;
import com.example.demo.pojo.response.ApiResponse;
import com.example.demo.service.StudentProcedureExtensionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 教师步骤时间延长控制器
 * 提供教师为学生延长实验步骤时间的接口
 */
@RequestMapping("/api/teacher/procedures/extensions")
@RestController
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class TeacherProcedureExtensionController {

    private final StudentProcedureExtensionService studentProcedureExtensionService;

    /**
     * 批量延长学生步骤时间
     *
     * @param request 延长请求
     * @return 操作结果
     */
    @PostMapping
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<Void> batchExtendProcedureTime(
            @Valid @RequestBody BatchExtendProcedureTimeRequest request) {
        try {
            String teacherUsername = com.example.demo.util.SecurityUtil.getCurrentUsername()
                    .orElseThrow(() -> new BusinessException(401, "未登录"));

            studentProcedureExtensionService.batchExtend(
                    request.getExperimentalProcedureId(),
                    request.getStudentUsernames(),
                    request.getExtendedMinutes(),
                    teacherUsername
            );
            return ApiResponse.success(null, "设置延长时间成功");
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("批量延长步骤时间失败", e);
            return ApiResponse.error(500, "设置失败: " + e.getMessage());
        }
    }

    /**
     * 更新延长记录
     *
     * @param id              延长记录ID
     * @param extendedMinutes 延长时间（分钟）
     * @return 操作结果
     */
    @PutMapping("/{id}")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<Void> updateExtensionTime(
            @PathVariable("id") Long id,
            @RequestParam("extendedMinutes") Integer extendedMinutes) {
        try {
            studentProcedureExtensionService.updateExtension(id, extendedMinutes);
            return ApiResponse.success(null, "更新延长时间成功");
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("更新延长时间失败", e);
            return ApiResponse.error(500, "更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除延长记录
     *
     * @param id 延长记录ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<Void> deleteExtension(@PathVariable("id") Long id) {
        try {
            studentProcedureExtensionService.deleteExtension(id);
            return ApiResponse.success(null, "删除延长记录成功");
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("删除延长记录失败", e);
            return ApiResponse.error(500, "删除失败: " + e.getMessage());
        }
    }

    /**
     * 查询步骤延长记录列表
     *
     * @param procedureId 实验步骤ID
     * @return 延长记录列表
     */
    @GetMapping
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<List<StudentProcedureExtension>> getProcedureExtensions(
            @RequestParam("procedureId") Long procedureId) {
        try {
            List<StudentProcedureExtension> extensions =
                    studentProcedureExtensionService.getExtensionsByProcedureId(procedureId);
            return ApiResponse.success(extensions, "查询成功");
        } catch (Exception e) {
            log.error("查询延长记录失败", e);
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }
}
