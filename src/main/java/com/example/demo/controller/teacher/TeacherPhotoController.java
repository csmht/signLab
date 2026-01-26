package com.example.demo.controller.teacher;

import com.example.demo.annotation.RequireRole;
import com.example.demo.enums.UserRole;
import com.example.demo.pojo.response.ApiResponse;
import com.example.demo.pojo.response.ClassPhotoResponse;
import com.example.demo.service.ClassPhotoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 教师课堂照片控制器
 * 提供教师查询课堂照片的接口
 */
@RequestMapping("/api/teacher/photos")
@RestController
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class TeacherPhotoController {

    private final ClassPhotoService classPhotoService;

    /**
     * 获取课程照片
     *
     * @param courseId 课程ID
     * @param experimentId 实验ID（可选）
     * @param studentUsername 学生用户名（可选，用于过滤特定学生的照片）
     * @return 照片列表
     */
    @GetMapping("/course/{courseId}")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<List<ClassPhotoResponse>> getCoursePhotos(
            @PathVariable("courseId") String courseId,
            @RequestParam(value = "experimentId", required = false) String experimentId,
            @RequestParam(value = "studentUsername", required = false) String studentUsername) {
        try {
            List<ClassPhotoResponse> photos = classPhotoService.getCoursePhotos(
                    courseId, experimentId, studentUsername
            );

            return ApiResponse.success(photos, "查询成功");
        } catch (Exception e) {
            log.error("查询课程照片失败", e);
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 查看照片详情
     *
     * @param photoId 照片ID
     * @return 照片详情
     */
    @GetMapping("/{photoId}")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<ClassPhotoResponse> getPhotoById(@PathVariable("photoId") Long photoId) {
        try {
            ClassPhotoResponse photo = classPhotoService.getPhotoById(photoId);
            return ApiResponse.success(photo);
        } catch (com.example.demo.exception.BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("查看照片详情失败", e);
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 删除课堂照片（教师端）
     *
     * @param photoId 照片ID
     * @return 是否删除成功
     */
    @DeleteMapping("/{photoId}")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<Void> deletePhoto(@PathVariable("photoId") Long photoId) {
        try {
            // 教师可以删除任何照片，不需要验证权限
            classPhotoService.deletePhoto(photoId, null);

            return ApiResponse.success(null, "删除成功");
        } catch (com.example.demo.exception.BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("删除课堂照片失败", e);
            return ApiResponse.error(500, "删除失败: " + e.getMessage());
        }
    }
}
