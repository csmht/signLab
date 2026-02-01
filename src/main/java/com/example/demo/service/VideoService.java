package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.VideoFileMapper;
import com.example.demo.pojo.entity.VideoFile;
import com.example.demo.pojo.request.VideoQueryRequest;
import com.example.demo.pojo.response.PageResponse;
import com.example.demo.pojo.response.VideoUploadResponse;
import com.example.demo.util.VideoMetadataUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 视频上传服务
 * 提供教师上传视频的业务逻辑处理
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VideoService extends ServiceImpl<VideoFileMapper, VideoFile> {

    /** 支持的视频格式 */
    private static final List<String> VIDEO_EXTENSIONS = Arrays.asList(
            "mp4", "avi", "mov", "wmv", "flv", "mkv", "webm"
    );

    /** 视频文件最大大小（默认500MB） */
    private static final long MAX_VIDEO_SIZE = 500 * 1024 * 1024;

    @Value("${file.upload.path:uploads/}")
    private String uploadBasePath;

    private final VideoMetadataUtil videoMetadataUtil;

    /**
     * 教师上传教学视频
     *
     * @param teacherUsername 教师用户名
     * @param title 视频标题
     * @param description 视频描述（可选）
     * @param courseId 课程ID（可选）
     * @param experimentId 实验ID（可选）
     * @param file 视频文件
     * @return 上传后的视频信息
     */
    @Transactional(rollbackFor = Exception.class)
    public VideoFile uploadTeacherVideo(String teacherUsername, String title, String description,
                                        String courseId, String experimentId, MultipartFile file) {
        // 1. 验证文件是否为空
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "视频文件不能为空");
        }

        // 2. 验证视频标题
        if (title == null || title.trim().isEmpty()) {
            throw new BusinessException(400, "视频标题不能为空");
        }

        // 3. 验证文件格式
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new BusinessException(400, "文件名不能为空");
        }

        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!VIDEO_EXTENSIONS.contains(extension)) {
            throw new BusinessException(400, "不支持的视频格式，仅支持：" + String.join(", ", VIDEO_EXTENSIONS));
        }

        // 4. 验证文件大小
        if (file.getSize() > MAX_VIDEO_SIZE) {
            throw new BusinessException(400, "视频文件大小不能超过500MB");
        }

        try {
            // 5. 生成存储路径：uploads/teacher-videos/教师用户名/年/月/日/
            String datePath = LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("yyyy" + File.separator + "MM" + File.separator + "dd")
            );
            String relativePath = "teacher-videos" + File.separator + teacherUsername + File.separator + datePath;
            String uploadDir = uploadBasePath + relativePath;

            // 6. 创建目录
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                boolean created = directory.mkdirs();
                if (!created) {
                    throw new BusinessException(500, "创建存储目录失败");
                }
            }

            // 7. 生成唯一文件名
            String uniqueFileName = UUID.randomUUID().toString() + "." + extension;
            String filePath = uploadDir + File.separator + uniqueFileName;

            // 8. 保存文件
            Path targetPath = Paths.get(filePath);
            file.transferTo(targetPath);

            // 9. 读取视频时长
            File savedVideoFile = targetPath.toFile();
            long videoSeconds = videoMetadataUtil.extractVideoDuration(savedVideoFile);

            // 10. 创建视频记录
            VideoFile videoFile = new VideoFile();
            videoFile.setOriginalFileName(originalFilename);
            videoFile.setStoredFileName(uniqueFileName);
            videoFile.setFilePath(relativePath + File.separator + uniqueFileName);
            videoFile.setFileSize(file.getSize());
            videoFile.setVideoSeconds(videoSeconds);

            // 11. 保存到数据库
            save(videoFile);

            // 12. 记录结果日志
            if (videoSeconds > 0) {
                log.info("教师 {} 上传视频成功: {}, 时长: {}秒", teacherUsername, originalFilename, videoSeconds);
            } else {
                log.warn("教师 {} 上传视频成功(但时长读取失败): {}", teacherUsername, originalFilename);
            }

            return videoFile;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("上传视频失败", e);
            throw new BusinessException(500, "上传视频失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件扩展名
     *
     * @param filename 文件名
     * @return 扩展名
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1);
    }

    /**
     * 删除视频文件
     *
     * @param videoId 视频ID
     * @return 是否删除成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteVideo(Long videoId) {
        try {
            VideoFile videoFile = getById(videoId);
            if (videoFile == null) {
                throw new BusinessException(404, "视频不存在");
            }

            // 删除物理文件
            String fullPath = uploadBasePath + videoFile.getFilePath();
            Path path = Paths.get(fullPath);
            if (Files.exists(path)) {
                Files.delete(path);
            }

            // 删除数据库记录
            boolean deleted = removeById(videoId);
            log.info("删除视频成功: {}", videoFile.getOriginalFileName());
            return deleted;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("删除视频失败", e);
            throw new BusinessException(500, "删除视频失败: " + e.getMessage());
        }
    }

    /**
     * 分页查询视频列表
     *
     * @param request 查询请求
     * @return 分页结果
     */
    public PageResponse<VideoUploadResponse> queryVideos(VideoQueryRequest request) {
        // 构建查询条件
        QueryWrapper<VideoFile> queryWrapper = new QueryWrapper<>();

        // 按原始文件名模糊查询
        if (StringUtils.hasText(request.getOriginalFileName())) {
            queryWrapper.like("original_file_name", request.getOriginalFileName().trim());
        }

        // 按回答ID精确查询
        if (request.getAnswerId() != null) {
            queryWrapper.eq("answer_id", request.getAnswerId());
        }

        // 按ID倒序排序
        queryWrapper.orderByDesc("id");

        // 判断是否分页查询
        if (Boolean.TRUE.equals(request.getPageable())) {
            // 分页查询
            Page<VideoFile> page = new Page<>(request.getCurrent(), request.getSize());
            Page<VideoFile> resultPage = page(page, queryWrapper);

            // 转换为响应DTO
            List<VideoUploadResponse> records = resultPage.getRecords().stream()
                    .map(VideoUploadResponse::fromEntity)
                    .collect(Collectors.toList());

            return PageResponse.of(
                    resultPage.getCurrent(),
                    resultPage.getSize(),
                    resultPage.getTotal(),
                    records
            );
        } else {
            // 列表查询
            List<VideoFile> list = list(queryWrapper);
            List<VideoUploadResponse> records = list.stream()
                    .map(VideoUploadResponse::fromEntity)
                    .collect(Collectors.toList());

            return PageResponse.of(
                    1L,
                    (long) records.size(),
                    (long) records.size(),
                    records
            );
        }
    }
}
