package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.ClassPhotoMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.mapper.ClassMapper;
import com.example.demo.pojo.entity.ClassPhoto;
import com.example.demo.pojo.entity.User;
import com.example.demo.pojo.entity.Class;
import com.example.demo.pojo.response.ClassPhotoResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 课堂照片服务
 * 提供课堂照片的业务逻辑处理
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClassPhotoService extends ServiceImpl<ClassPhotoMapper, ClassPhoto> {

    private final UserMapper userMapper;
    private final ClassMapper classMapper;

    /** 照片存储根路径 */
    private static final String PHOTO_ROOT_PATH = "uploads" + File.separator + "class_photos";

    /**
     * 上传课堂照片
     *
     * @param courseId 课程ID
     * @param experimentId 实验ID
     * @param studentUsername 学生用户名
     * @param file 照片文件
     * @param remark 备注
     * @return 上传后的照片信息
     */
    @Transactional(rollbackFor = Exception.class)
    public ClassPhotoResponse uploadPhoto(String courseId, String experimentId,
                                          String studentUsername, MultipartFile file, String remark) {
        // 1. 验证学生是否存在
        User student = userMapper.selectOne(
                new QueryWrapper<User>().eq("username", studentUsername)
        );
        if (student == null) {
            throw new BusinessException(404, "学生不存在");
        }

        // 2. 验证文件是否为空
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "文件不能为空");
        }

        // 3. 验证文件类型（支持 jpg, jpeg, png, gif, webp）
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        if (!isValidImageExtension(extension)) {
            throw new BusinessException(400, "不支持的文件格式，仅支持：jpg, jpeg, png, gif, webp");
        }

        // 4. 验证文件大小（限制10MB）
        long maxSize = 10 * 1024 * 1024; // 10MB
        if (file.getSize() > maxSize) {
            throw new BusinessException(400, "文件大小不能超过10MB");
        }

        try {
            // 5. 生成存储路径：uploads/class_photos/课程ID/实验ID/年/月/日/
            String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy" + File.separator + "MM" + File.separator + "dd"));
            String relativePath = courseId + File.separator + experimentId + File.separator + datePath;
            String uploadDir = PHOTO_ROOT_PATH + File.separator + relativePath;

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

            // 9. 创建照片记录
            ClassPhoto classPhoto = new ClassPhoto();
            classPhoto.setCourseId(courseId);
            classPhoto.setExperimentId(experimentId);
            classPhoto.setStudentUsername(studentUsername);
            classPhoto.setPhotoName(originalFilename);
            classPhoto.setPhotoPath(filePath);
            classPhoto.setPhotoUrl("/api/photos/class/" + classPhoto.getId()); // 暂时设置，保存后更新
            classPhoto.setRemark(remark);
            classPhoto.setFileSize(file.getSize());
            classPhoto.setUploadTime(LocalDateTime.now());

            // 10. 保存到数据库
            boolean saved = save(classPhoto);
            if (!saved) {
                // 如果保存失败，删除已上传的文件
                Files.deleteIfExists(targetPath);
                throw new BusinessException(500, "保存照片信息失败");
            }

            // 11. 更新照片URL
            classPhoto.setPhotoUrl("/api/photos/class/" + classPhoto.getId());
            updateById(classPhoto);

            log.info("学生 {} 上传课堂照片成功，课程：{}，实验：{}", studentUsername, courseId, experimentId);

            // 12. 构建返回结果
            ClassPhotoResponse response = new ClassPhotoResponse();
            response.setId(classPhoto.getId());
            response.setCourseId(courseId);
            response.setExperimentId(experimentId);
            response.setStudentUsername(studentUsername);
            response.setStudentName(student.getName());
            response.setPhotoName(originalFilename);
            response.setPhotoUrl(classPhoto.getPhotoUrl());
            response.setRemark(remark);
            response.setFileSize(file.getSize());
            response.setUploadTime(classPhoto.getUploadTime());

            return response;

        } catch (IOException e) {
            log.error("上传文件失败", e);
            throw new BusinessException(500, "上传文件失败: " + e.getMessage());
        }
    }

    /**
     * 查询学生的课堂照片列表
     *
     * @param studentUsername 学生用户名
     * @param courseId 课程ID（可选）
     * @return 照片列表
     */
    public List<ClassPhotoResponse> getStudentPhotos(String studentUsername, String courseId) {
        QueryWrapper<ClassPhoto> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("student_username", studentUsername)
                .eq("is_deleted", 0);

        if (courseId != null && !courseId.trim().isEmpty()) {
            queryWrapper.eq("course_id", courseId);
        }

        queryWrapper.orderByDesc("upload_time");

        List<ClassPhoto> photos = list(queryWrapper);

        return photos.stream().map(photo -> {
            ClassPhotoResponse response = new ClassPhotoResponse();
            response.setId(photo.getId());
            response.setCourseId(photo.getCourseId());
            response.setExperimentId(photo.getExperimentId());
            response.setStudentUsername(photo.getStudentUsername());

            // 查询学生姓名
            User student = userMapper.selectOne(
                    new QueryWrapper<User>().eq("username", photo.getStudentUsername())
            );
            response.setStudentName(student != null ? student.getName() : photo.getStudentUsername());

            response.setPhotoName(photo.getPhotoName());
            response.setPhotoUrl(photo.getPhotoUrl());
            response.setRemark(photo.getRemark());
            response.setFileSize(photo.getFileSize());
            response.setUploadTime(photo.getUploadTime());

            return response;
        }).collect(Collectors.toList());
    }

    /**
     * 查询课程的课堂照片列表（教师端）
     *
     * @param courseId 课程ID
     * @param experimentId 实验ID（可选）
     * @param studentUsername 学生用户名（可选，用于过滤特定学生的照片）
     * @return 照片列表
     */
    public List<ClassPhotoResponse> getCoursePhotos(String courseId, String experimentId, String studentUsername) {
        QueryWrapper<ClassPhoto> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("course_id", courseId)
                .eq("is_deleted", 0);

        if (experimentId != null && !experimentId.trim().isEmpty()) {
            queryWrapper.eq("experiment_id", experimentId);
        }

        if (studentUsername != null && !studentUsername.trim().isEmpty()) {
            queryWrapper.eq("student_username", studentUsername);
        }

        queryWrapper.orderByDesc("upload_time");

        List<ClassPhoto> photos = list(queryWrapper);

        return photos.stream().map(photo -> {
            ClassPhotoResponse response = new ClassPhotoResponse();
            response.setId(photo.getId());
            response.setCourseId(photo.getCourseId());
            response.setExperimentId(photo.getExperimentId());
            response.setStudentUsername(photo.getStudentUsername());

            // 查询学生姓名
            User student = userMapper.selectOne(
                    new QueryWrapper<User>().eq("username", photo.getStudentUsername())
            );
            response.setStudentName(student != null ? student.getName() : photo.getStudentUsername());

            response.setPhotoName(photo.getPhotoName());
            response.setPhotoUrl(photo.getPhotoUrl());
            response.setRemark(photo.getRemark());
            response.setFileSize(photo.getFileSize());
            response.setUploadTime(photo.getUploadTime());

            return response;
        }).collect(Collectors.toList());
    }

    /**
     * 根据ID查询课堂照片
     *
     * @param photoId 照片ID
     * @return 照片信息
     */
    public ClassPhotoResponse getPhotoById(Long photoId) {
        ClassPhoto photo = getById(photoId);
        if (photo == null || photo.getIsDeleted() == 1) {
            throw new BusinessException(404, "照片不存在");
        }

        // 查询学生姓名
        User student = userMapper.selectOne(
                new QueryWrapper<User>().eq("username", photo.getStudentUsername())
        );

        ClassPhotoResponse response = new ClassPhotoResponse();
        response.setId(photo.getId());
        response.setCourseId(photo.getCourseId());
        response.setExperimentId(photo.getExperimentId());
        response.setStudentUsername(photo.getStudentUsername());
        response.setStudentName(student != null ? student.getName() : photo.getStudentUsername());
        response.setPhotoName(photo.getPhotoName());
        response.setPhotoUrl(photo.getPhotoUrl());
        response.setRemark(photo.getRemark());
        response.setFileSize(photo.getFileSize());
        response.setUploadTime(photo.getUploadTime());

        return response;
    }

    /**
     * 删除课堂照片
     *
     * @param photoId 照片ID
     * @param studentUsername 当前登录用户名（用于权限验证）
     */
    @Transactional(rollbackFor = Exception.class)
    public void deletePhoto(Long photoId, String studentUsername) {
        // 1. 查询照片
        ClassPhoto photo = getById(photoId);
        if (photo == null || photo.getIsDeleted() == 1) {
            throw new BusinessException(404, "照片不存在");
        }

        // 2. 权限验证：学生只能删除自己的照片
        // 注意：教师可以删除所有照片，这里可以通过角色判断
        // 简化处理：如果是学生，只能删除自己的照片
        // 在Controller层进行角色验证

        // 3. 删除物理文件
        try {
            Path filePath = Paths.get(photo.getPhotoPath());
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("删除物理文件成功: {}", photo.getPhotoPath());
            }
        } catch (IOException e) {
            log.warn("删除物理文件失败: {}", photo.getPhotoPath(), e);
            // 即使物理文件删除失败，也继续删除数据库记录
        }

        // 4. 软删除数据库记录
        photo.setIsDeleted(1);
        boolean updated = updateById(photo);
        if (!updated) {
            throw new BusinessException(500, "删除照片失败");
        }

        log.info("删除照片成功，ID：{}", photoId);
    }

    /**
     * 获取文件的扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex + 1).toLowerCase();
        }
        return "";
    }

    /**
     * 验证是否为有效的图片格式
     */
    private boolean isValidImageExtension(String extension) {
        return extension.equals("jpg")
                || extension.equals("jpeg")
                || extension.equals("png")
                || extension.equals("gif")
                || extension.equals("webp");
    }
}
