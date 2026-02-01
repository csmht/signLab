package com.example.demo.controller.teacher;

import com.example.demo.annotation.RequireRole;
import com.example.demo.enums.UserRole;
import com.example.demo.pojo.entity.VideoFile;
import com.example.demo.pojo.request.VideoQueryRequest;
import com.example.demo.pojo.response.ApiResponse;
import com.example.demo.pojo.response.PageResponse;
import com.example.demo.pojo.response.VideoUploadResponse;
import com.example.demo.service.VideoService;
import com.example.demo.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 教师视频管理控制器
 * 提供教师上传、删除视频的接口
 */
@RequestMapping("/api/teacher/videos")
@RestController("teacherVideoController")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class TeacherVideoController {

    private final VideoService videoService;

    /**
     * 上传教学视频
     *
     * @param title 视频标题
     * @param description 视频描述（可选）
     * @param file 视频文件
     * @return 上传结果
     */
    @PostMapping("/upload")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<VideoUploadResponse> uploadVideo(
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("file") MultipartFile file) {
        try {
            // 获取当前登录教师用户名
            String teacherUsername = SecurityUtil.getCurrentUsername()
                    .orElseThrow(() -> new com.example.demo.exception.BusinessException(401, "未登录"));

            // 上传视频
            VideoFile videoFile = videoService.uploadTeacherVideo(
                    teacherUsername, title, description,  file
            );

            // 转换为响应对象
            VideoUploadResponse response = VideoUploadResponse.fromEntity(videoFile);

            return ApiResponse.success(response, "视频上传成功");

        } catch (com.example.demo.exception.BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("上传视频失败", e);
            return ApiResponse.error(500, "上传失败: " + e.getMessage());
        }
    }

    /**
     * 删除视频
     *
     * @param videoId 视频ID
     * @return 删除结果
     */
    @DeleteMapping("/{videoId}")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<Void> deleteVideo(@PathVariable Long videoId) {
        try {
            boolean deleted = videoService.deleteVideo(videoId);
            if (deleted) {
                return ApiResponse.success(null, "视频删除成功");
            } else {
                return ApiResponse.error(404, "视频不存在");
            }
        } catch (com.example.demo.exception.BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("删除视频失败", e);
            return ApiResponse.error(500, "删除失败: " + e.getMessage());
        }
    }

    /**
     * 获取视频信息
     *
     * @param videoId 视频ID
     * @return 视频信息
     */
    @GetMapping("/{videoId}")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<VideoUploadResponse> getVideoInfo(@PathVariable Long videoId) {
        try {
            com.example.demo.pojo.entity.VideoFile videoFile = videoService.getById(videoId);
            if (videoFile == null) {
                return ApiResponse.error(404, "视频不存在");
            }

            VideoUploadResponse response = VideoUploadResponse.fromEntity(videoFile);
            return ApiResponse.success(response, "查询成功");

        } catch (Exception e) {
            log.error("查询视频信息失败", e);
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 查询视频列表(分页或列表)
     *
     * @param request 查询请求
     * @return 视频列表
     */
    @PostMapping("/query")
    @RequireRole(value = UserRole.TEACHER)
    public ApiResponse<PageResponse<VideoUploadResponse>> queryVideos(@RequestBody VideoQueryRequest request) {
        try {
            PageResponse<VideoUploadResponse> response = videoService.queryVideos(request);
            return ApiResponse.success(response, "查询成功");
        } catch (Exception e) {
            log.error("查询视频列表失败", e);
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }
}
