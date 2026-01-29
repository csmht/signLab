package com.example.demo.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Component
public class DirectoryInitializer implements CommandLineRunner {

    @Value("${file.upload.path}")
    private String uploadPath;

    @Value("${file.upload.photo.path}")
    private String photoPath;

    @Value("${file.upload.document.path}")
    private String documentPath;

    @Override
    public void run(String... args) throws Exception {
        log.info("开始初始化上传目录...");

        // 确保基础路径存在（项目根目录下的 uploads/signlab）
        createDirectoryIfNotExists(uploadPath, "上传根目录");

        // 创建子目录
        createDirectoryIfNotExists(photoPath, "照片存储目录");
        createDirectoryIfNotExists(documentPath, "文档存储目录");

        // 创建其他必要的目录
        createVideoDirectories();

        log.info("目录初始化完成！");
    }

    /**
     * 创建视频存储目录
     */
    private void createVideoDirectories() {
        try {
            // 教师视频目录：uploads/signlab/teacher-videos/
            String teacherVideoPath = uploadPath + "teacher-videos";
            createDirectoryIfNotExists(teacherVideoPath, "教师视频目录");

            // 步骤提交文件目录：uploads/signlab/procedure-submissions/
            String submissionPath = uploadPath + "procedure-submissions";
            createDirectoryIfNotExists(submissionPath, "步骤提交文件目录");

            // 步骤附件目录：uploads/signlab/attachments/
            String attachmentPath = uploadPath + "attachments";
            createDirectoryIfNotExists(attachmentPath, "步骤附件目录");

            log.info("视频和附件目录结构创建完成");
        } catch (Exception e) {
            log.error("创建视频附件目录失败: {}", e.getMessage(), e);
        }
    }

    private void createDirectoryIfNotExists(String path, String description) {
        try {
            Path dirPath = Paths.get(path).toAbsolutePath();
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
                log.info("创建目录成功: {} ({})", dirPath.toString(), description);
            } else {
                log.info("目录已存在: {} ({})", dirPath.toString(), description);
            }
        } catch (Exception e) {
            log.error("创建目录失败: {} ({}) - {}", path, description, e.getMessage(), e);
        }
    }

    private void createPhotoSubDirectories() {
        try {
            java.time.LocalDate now = java.time.LocalDate.now();
            String year = String.valueOf(now.getYear());
            String month = String.format("%02d", now.getMonthValue());
            String day = String.format("%02d", now.getDayOfMonth());

            String currentDatePath = String.format("%s/%s/%s/%s", photoPath, year, month, day);
            createDirectoryIfNotExists(currentDatePath, "当前日期目录");

            String exampleCoursePath = String.format("%s/example_course", currentDatePath);
            createDirectoryIfNotExists(exampleCoursePath, "示例课程目录");

            String exampleStudentPath = String.format("%s/example_student", exampleCoursePath);
            createDirectoryIfNotExists(exampleStudentPath, "示例学生目录");

            String originalPath = String.format("%s/original", exampleStudentPath);
            String compressedPath = String.format("%s/compressed", exampleStudentPath);
            createDirectoryIfNotExists(originalPath, "原图存储目录");
            createDirectoryIfNotExists(compressedPath, "压缩图存储目录");

            log.info("照片子目录结构创建完成");

        } catch (Exception e) {
            log.error("创建照片子目录失败: {}", e.getMessage(), e);
        }
    }
}