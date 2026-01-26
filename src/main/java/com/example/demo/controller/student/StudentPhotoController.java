package com.example.demo.controller.student;

import com.example.demo.annotation.RequireRole;
import com.example.demo.enums.UserRole;
import com.example.demo.pojo.response.ApiResponse;
import com.example.demo.pojo.response.ClassPhotoResponse;
import com.example.demo.service.ClassPhotoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 学生课堂照片控制器
 * 提供学生上传和查询课堂照片的接口
 */
@RequestMapping("/api/student/photos")
@RestController
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class StudentPhotoController {

    private final ClassPhotoService classPhotoService;

    /**
     * 上传课堂照片
     *
     * @param courseId 课程ID
     * @param experimentId 实验ID
     * @param file 照片文件
     * @param remark 备注（可选）
     * @return 上传后的照片信息
     */
    @PostMapping("/upload")
    @RequireRole(value = UserRole.STUDENT)
    public ApiResponse<ClassPhotoResponse> uploadPhoto(
            @RequestParam("courseId") String courseId,
            @RequestParam("experimentId") String experimentId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "remark", required = false) String remark) {
        try {
            // 获取当前登录学生用户名
            String studentUsername = com.example.demo.util.SecurityUtil.getCurrentUsername()
                    .orElseThrow(() -> new com.example.demo.exception.BusinessException(401, "未登录"));

            ClassPhotoResponse response = classPhotoService.uploadPhoto(
                    courseId, experimentId, studentUsername, file, remark
            );

            return ApiResponse.success(response, "上传成功");
        } catch (com.example.demo.exception.BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("上传课堂照片失败", e);
            return ApiResponse.error(500, "上传失败: " + e.getMessage());
        }
    }

    /**
     * 查询学生的课堂照片列表
     *
     * @param courseId 课程ID（可选）
     * @return 照片列表
     */
    @GetMapping
    @RequireRole(value = UserRole.STUDENT)
    public ApiResponse<List<ClassPhotoResponse>> getPhotos(
            @RequestParam(value = "courseId", required = false) String courseId) {
        try {
            String studentUsername = com.example.demo.util.SecurityUtil.getCurrentUsername()
                    .orElseThrow(() -> new com.example.demo.exception.BusinessException(401, "未登录"));

            List<ClassPhotoResponse> photos = classPhotoService.getStudentPhotos(studentUsername, courseId);

            return ApiResponse.success(photos, "查询成功");
        } catch (Exception e) {
            log.error("查询课堂照片失败", e);
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 按课程查询照片
     *
     * @param courseId 课程ID
     * @return 照片列表
     */
    @GetMapping("/course/{courseId}")
    @RequireRole(value = UserRole.STUDENT)
    public ApiResponse<List<ClassPhotoResponse>> getPhotosByCourse(
            @PathVariable("courseId") String courseId) {
        try {
            String studentUsername = com.example.demo.util.SecurityUtil.getCurrentUsername()
                    .orElseThrow(() -> new com.example.demo.exception.BusinessException(401, "未登录"));

            List<ClassPhotoResponse> photos = classPhotoService.getStudentPhotos(studentUsername, courseId);

            return ApiResponse.success(photos, "查询成功");
        } catch (Exception e) {
            log.error("查询课堂照片失败", e);
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 删除课堂照片
     *
     * @param photoId 照片ID
     * @return 是否删除成功
     */
    @DeleteMapping("/{photoId}")
    @RequireRole(value = UserRole.STUDENT)
    public ApiResponse<Void> deletePhoto(@PathVariable("photoId") Long photoId) {
        try {
            String studentUsername = com.example.demo.util.SecurityUtil.getCurrentUsername()
                    .orElseThrow(() -> new com.example.demo.exception.BusinessException(401, "未登录"));

            classPhotoService.deletePhoto(photoId, studentUsername);

            return ApiResponse.success(null, "删除成功");
        } catch (com.example.demo.exception.BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("删除课堂照片失败", e);
            return ApiResponse.error(500, "删除失败: " + e.getMessage());
        }
    }
}
