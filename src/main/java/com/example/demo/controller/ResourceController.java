package com.example.demo.controller;

import com.example.demo.annotation.RequireRole;
import com.example.demo.enums.UserRole;
import com.example.demo.pojo.dto.mapvo.DownloadKeyResponse;
import com.example.demo.pojo.response.ApiResponse;
import com.example.demo.service.DownloadService;
import com.example.demo.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 资源控制器
 * 提供需要认证的文件资源接口
 */
@RequestMapping("/api/resource")
@RestController
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ResourceController {

    private final DownloadService downloadService;

    /**
     * 根据视频ID获取下载密钥
     *
     * @param videoId 视频文件ID
     * @return 下载密钥
     */
    @GetMapping("/video/key/{videoId}")
    @RequireRole(value = UserRole.STUDENT)
    public ApiResponse<DownloadKeyResponse> getVideoDownloadKey(@PathVariable Long videoId) {
        try {
            String username = SecurityUtil.getCurrentUsername()
                    .orElseThrow(() -> new IllegalArgumentException("未登录用户"));

            String downloadKey = downloadService.generateVideoKey(videoId, username);

            DownloadKeyResponse result = new DownloadKeyResponse();
            result.setDownloadKey(downloadKey);
            result.setDownloadUrl("/api/download/video/" + downloadKey);

            return ApiResponse.success(result, "获取下载密钥成功");
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(401, e.getMessage());
        } catch (Exception e) {
            log.error("获取视频下载密钥失败", e);
            return ApiResponse.error(500, "获取下载密钥失败: " + e.getMessage());
        }
    }

    /**
     * 根据文件ID获取下载密钥
     *
     * @param fileType 文件类型（submission-步骤提交文件，attachment-步骤附件）
     * @param fileId   文件ID
     * @return 下载密钥
     */
    @GetMapping("/file/key/{fileType}/{fileId}")
    @RequireRole(value = UserRole.STUDENT)
    public ApiResponse<DownloadKeyResponse> getFileDownloadKey(
            @PathVariable String fileType,
            @PathVariable Long fileId) {
        try {
            String username = SecurityUtil.getCurrentUsername()
                    .orElseThrow(() -> new IllegalArgumentException("未登录用户"));

            String downloadKey = downloadService.generateFileKey(fileType, fileId, username);

            DownloadKeyResponse result = new DownloadKeyResponse();
            result.setDownloadKey(downloadKey);
            result.setDownloadUrl("/api/download/file/" + downloadKey);

            return ApiResponse.success(result, "获取下载密钥成功");
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(401, e.getMessage());
        } catch (Exception e) {
            log.error("获取文件下载密钥失败", e);
            return ApiResponse.error(500, "获取下载密钥失败: " + e.getMessage());
        }
    }

    /**
     * 根据视频ID获取播放密钥
     *
     * @param videoId 视频文件ID
     * @return 播放密钥
     */
    @GetMapping("/video/playkey/{videoId}")
    @RequireRole(value = UserRole.STUDENT)
    public ApiResponse<DownloadKeyResponse> getVideoPlayKey(@PathVariable Long videoId) {
        try {
            String username = SecurityUtil.getCurrentUsername()
                    .orElseThrow(() -> new IllegalArgumentException("未登录用户"));

            String playKey = downloadService.generatePlayKey(videoId, username);

            DownloadKeyResponse result = new DownloadKeyResponse();
            result.setPlayKey(playKey);
            result.setPlayUrl("/api/download/play/" + playKey);

            return ApiResponse.success(result, "获取播放密钥成功");
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(401, e.getMessage());
        } catch (Exception e) {
            log.error("获取视频播放密钥失败", e);
            return ApiResponse.error(500, "获取播放密钥失败: " + e.getMessage());
        }
    }
}
