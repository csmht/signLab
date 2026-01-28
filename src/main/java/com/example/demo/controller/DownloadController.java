package com.example.demo.controller;

import com.example.demo.service.DownloadService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 下载控制器
 * 提供公共下载接口
 */
@RestController
@RequestMapping("/api/download")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class DownloadController {

    @Autowired
    private DownloadService downloadService;

    /**
     * 根据播放密钥播放视频（流式传输，禁止拖拽）
     *
     * @param playKey Base64编码的播放密钥
     * @param response HTTP响应
     */
    @GetMapping("/play/{playKey}")
    public void playVideo(@PathVariable String playKey, HttpServletResponse response) {
        try {
            // 验证播放密钥
            DownloadService.VideoFilePlayInfo playInfo = downloadService.getVideoByPlayKey(playKey);

            java.io.File file = new java.io.File(playInfo.getFilePath());
            if (!file.exists()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("视频文件不存在");
                return;
            }

            // 检查Range请求头，禁止拖拽
            String rangeHeader = response.getHeader("Range");
            if (rangeHeader != null && !rangeHeader.startsWith("bytes=0-")) {
                response.setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                response.getWriter().write("不支持拖拽进度");
                return;
            }

            // 获取文件MIME类型
            String contentType = getVideoContentType(playInfo.getFileName());

            // 设置响应头
            response.setContentType(contentType);
            response.setContentLengthLong(playInfo.getFileSize());
            response.setHeader(HttpHeaders.ACCEPT_RANGES, "none"); // 禁止范围请求
            response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
            response.setHeader(HttpHeaders.PRAGMA, "no-cache");
            response.setHeader("X-Content-Type-Options", "nosniff");

            // 流式传输视频
            try (FileInputStream fis = new FileInputStream(file);
                 OutputStream os = response.getOutputStream()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                os.flush();
            }

            log.info("视频播放成功: {}, 用户: {}", playInfo.getFileName(), playInfo.getUsername());
        } catch (IOException e) {
            log.error("视频播放失败: {}", e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try {
                response.getWriter().write("播放失败");
            } catch (IOException ex) {
                log.error("写入错误响应失败", ex);
            }
        }
    }

    /**
     * 根据文件名获取视频MIME类型
     */
    private String getVideoContentType(String fileName) {
        if (fileName == null) {
            return "video/mp4";
        }
        String lowerFileName = fileName.toLowerCase();
        if (lowerFileName.endsWith(".mp4")) {
            return "video/mp4";
        } else if (lowerFileName.endsWith(".avi")) {
            return "video/x-msvideo";
        } else if (lowerFileName.endsWith(".mov")) {
            return "video/quicktime";
        } else if (lowerFileName.endsWith(".wmv")) {
            return "video/x-ms-wmv";
        } else if (lowerFileName.endsWith(".flv")) {
            return "video/x-flv";
        } else if (lowerFileName.endsWith(".webm")) {
            return "video/webm";
        } else if (lowerFileName.endsWith(".mkv")) {
            return "video/x-matroska";
        }
        return "video/mp4"; // 默认
    }

    /**
     * 根据密钥下载视频
     *
     * @param key 加密的下载密钥
     * @param response HTTP响应
     */
    @GetMapping("/video/{key}")
    public void downloadVideo(@PathVariable String key, HttpServletResponse response) {
        try {
            DownloadService.VideoFileDownloadInfo downloadInfo = downloadService.getVideoByKey(key);

            java.io.File file = new java.io.File(downloadInfo.getFilePath());
            if (!file.exists()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("文件不存在");
                return;
            }

            // 设置响应头
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            response.setContentLengthLong(downloadInfo.getFileSize());

            // 对文件名进行URL编码
            String encodedFileName = URLEncoder.encode(downloadInfo.getFileName(), StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20");
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName);

            // 写入文件流
            try (FileInputStream fis = new FileInputStream(file);
                 OutputStream os = response.getOutputStream()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                os.flush();
            }

            log.info("视频下载成功: {}", downloadInfo.getFileName());
        } catch (IOException e) {
            log.error("视频下载失败: {}", e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try {
                response.getWriter().write("下载失败");
            } catch (IOException ex) {
                log.error("写入错误响应失败", ex);
            }
        }
    }

    /**
     * 根据密钥下载文件
     *
     * @param key 加密的下载密钥
     * @param response HTTP响应
     */
    @GetMapping("/file/{key}")
    public void downloadFile(@PathVariable String key, HttpServletResponse response) {
        try {
            DownloadService.FileDownloadInfo downloadInfo = downloadService.getFileByKey(key);

            java.io.File file = new java.io.File(downloadInfo.getFilePath());
            if (!file.exists()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("文件不存在");
                return;
            }

            // 设置响应头
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            response.setContentLengthLong(downloadInfo.getFileSize());

            // 对文件名进行URL编码
            String encodedFileName = URLEncoder.encode(downloadInfo.getFileName(), StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20");
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName);

            // 写入文件流
            try (FileInputStream fis = new FileInputStream(file);
                 OutputStream os = response.getOutputStream()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                os.flush();
            }

            log.info("文件下载成功: {}", downloadInfo.getFileName());
        } catch (IOException e) {
            log.error("文件下载失败: {}", e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try {
                response.getWriter().write("下载失败");
            } catch (IOException ex) {
                log.error("写入错误响应失败", ex);
            }
        }
    }
}
