package com.example.demo.pojo.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 视频上传响应
 */
@Data
public class VideoUploadResponse {

    /** 视频ID */
    private Long id;

    /** 原始文件名 */
    private String originalFileName;

    /** 存储文件名 */
    private String storedFileName;

    /** 文件访问路径 */
    private String fileAccessPath;

    /** 文件大小（字节） */
    private Long fileSize;

    /** 文件大小（人类可读格式） */
    private String fileSizeHumanReadable;

    /** 视频时长（秒） */
    private Long videoSeconds;

    /** 上传时间 */
    private LocalDateTime uploadTime;

    /**
     * 从实体转换为响应对象
     */
    public static VideoUploadResponse fromEntity(com.example.demo.pojo.entity.VideoFile videoFile) {
        VideoUploadResponse response = new VideoUploadResponse();
        response.setId(videoFile.getId());
        response.setOriginalFileName(videoFile.getOriginalFileName());
        response.setStoredFileName(videoFile.getStoredFileName());
        response.setFileAccessPath("/api/videos/" + videoFile.getId());
        response.setFileSize(videoFile.getFileSize());
        response.setFileSizeHumanReadable(formatFileSize(videoFile.getFileSize()));
        response.setVideoSeconds(videoFile.getVideoSeconds());
        response.setUploadTime(java.time.LocalDateTime.now());
        return response;
    }

    /**
     * 格式化文件大小为人类可读格式
     */
    private static String formatFileSize(Long bytes) {
        if (bytes == null) {
            return "0 B";
        }

        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", bytes / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
        }
    }
}
