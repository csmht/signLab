package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.ProcedureSubmissionMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.pojo.entity.ProcedureSubmission;
import com.example.demo.pojo.entity.User;
import com.example.demo.pojo.response.ProcedureSubmissionResponse;
import com.example.demo.util.SecurityUtil;
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
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 实验步骤提交服务
 * 提供步骤文件上传、查询、批改等业务逻辑处理
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcedureSubmissionService extends ServiceImpl<ProcedureSubmissionMapper, ProcedureSubmission> {

    private final UserMapper userMapper;
    private final DownloadService downloadService;

    /** 步骤文件存储根路径 */
    private static final String PROCEDURE_SUBMISSION_ROOT_PATH = "uploads" + File.separator + "procedure-submissions";

    /**
     * 上传步骤文件
     *
     * @param courseId 课程ID
     * @param experimentId 实验ID
     * @param studentUsername 学生用户名
     * @param submissionType 提交类型（实验报告、数据文件等）
     * @param file 步骤文件
     * @return 上传后的步骤信息
     */
    @Transactional(rollbackFor = Exception.class)
    public ProcedureSubmissionResponse uploadProcedureSubmission(String courseId, String experimentId,
                                                              String studentUsername, String submissionType,
                                                              MultipartFile file) {
        // 1. 验证学生是否存在
        User student = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, studentUsername)
        );
        if (student == null) {
            throw new BusinessException(404, "学生不存在");
        }

        // 2. 验证文件是否为空
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "文件不能为空");
        }

        // 3. 验证文件大小（限制50MB）
        long maxSize = 50 * 1024 * 1024; // 50MB
        if (file.getSize() > maxSize) {
            throw new BusinessException(400, "文件大小不能超过50MB");
        }

        try {
            // 4. 生成存储路径：uploads/procedure-submissions/课程ID/实验ID/年/月/日/
            String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy" + File.separator + "MM" + File.separator + "dd"));
            String relativePath = courseId + File.separator + experimentId + File.separator + datePath;
            String uploadDir = PROCEDURE_SUBMISSION_ROOT_PATH + File.separator + relativePath;

            // 5. 创建目录
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                boolean created = directory.mkdirs();
                if (!created) {
                    throw new BusinessException(500, "创建存储目录失败");
                }
            }

            // 6. 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String uniqueFileName = UUID.randomUUID().toString() + "." + extension;
            String filePath = uploadDir + File.separator + uniqueFileName;

            // 7. 保存文件
            Path targetPath = Paths.get(filePath);
            file.transferTo(targetPath);

            // 8. 创建步骤提交记录
            ProcedureSubmission submission = new ProcedureSubmission();
            submission.setCourseId(courseId);
            submission.setExperimentId(experimentId);
            submission.setStudentUsername(studentUsername);
            submission.setSubmissionType(submissionType);
            submission.setFilePath(relativePath + File.separator + uniqueFileName);  // 只保存相对路径
            submission.setFileName(originalFilename);
            submission.setFileSize(file.getSize());
            submission.setSubmissionStatus(ProcedureSubmission.STATUS_NOT_GRADED); // 默认为未批改状态
            submission.setCreateTime(LocalDateTime.now());
            submission.setUpdateTime(LocalDateTime.now());

            // 9. 保存到数据库
            boolean saved = save(submission);
            if (!saved) {
                // 如果保存失败，删除已上传的文件
                Files.deleteIfExists(targetPath);
                throw new BusinessException(500, "保存步骤信息失败");
            }

            log.info("学生 {} 上传实验步骤文件成功，课程：{}，实验：{}，类型：{}",
                    studentUsername, courseId, experimentId, submissionType);

            // 10. 构建返回结果
            ProcedureSubmissionResponse response = new ProcedureSubmissionResponse();
            response.setId(submission.getId());
            response.setCourseId(courseId);
            response.setExperimentId(experimentId);
            response.setStudentUsername(studentUsername);
            response.setStudentName(student.getName());
            response.setSubmissionType(submissionType);
            response.setFileName(originalFilename);
            response.setFileSize(file.getSize());
            response.setSubmissionStatus(ProcedureSubmission.STATUS_NOT_GRADED);
            response.setCreateTime(submission.getCreateTime());

            return response;

        } catch (IOException e) {
            log.error("上传文件失败", e);
            throw new BusinessException(500, "上传文件失败: " + e.getMessage());
        }
    }

    /**
     * 提交步骤（将草稿状态改为已提交）
     *
     * @param submissionId 步骤提交ID
     * @param studentUsername 学生用户名（用于权限验证）
     */
    @Transactional(rollbackFor = Exception.class)
    public void submitProcedure(Long submissionId, String studentUsername) {
        ProcedureSubmission submission = getById(submissionId);
        if (submission == null || submission.getIsDeleted() == 1) {
            throw new BusinessException(404, "步骤提交记录不存在");
        }

        // 权限验证：只能提交自己的步骤
        if (!submission.getStudentUsername().equals(studentUsername)) {
            throw new BusinessException(403, "无权提交此步骤");
        }

        submission.setSubmissionStatus(ProcedureSubmission.STATUS_NOT_GRADED);
        submission.setSubmissionTime(LocalDateTime.now());
        submission.setUpdateTime(LocalDateTime.now());

        boolean updated = updateById(submission);
        if (!updated) {
            throw new BusinessException(500, "提交步骤失败");
        }

        log.info("学生 {} 提交实验步骤成功，ID：{}", studentUsername, submissionId);
    }

    /**
     * 查询学生的步骤提交列表
     *
     * @param studentUsername 学生用户名
     * @param courseId 课程ID（可选）
     * @param experimentId 实验ID（可选）
     * @return 步骤列表
     */
    public List<ProcedureSubmissionResponse> getStudentSubmissions(String studentUsername, String courseId, String experimentId) {
        LambdaQueryWrapper<ProcedureSubmission> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProcedureSubmission::getStudentUsername, studentUsername);

        if (courseId != null && !courseId.trim().isEmpty()) {
            queryWrapper.eq(ProcedureSubmission::getCourseId, courseId);
        }

        if (experimentId != null && !experimentId.trim().isEmpty()) {
            queryWrapper.eq(ProcedureSubmission::getExperimentId, experimentId);
        }

        queryWrapper.orderByDesc(ProcedureSubmission::getCreateTime);

        List<ProcedureSubmission> submissions = list(queryWrapper);

        return submissions.stream().map(this::buildResponse).collect(Collectors.toList());
    }

    /**
     * 查询课程的步骤提交列表（教师端）
     *
     * @param courseId 课程ID
     * @param experimentId 实验ID（可选）
     * @param submissionStatus 提交状态（可选）
     * @return 步骤列表
     */
    public List<ProcedureSubmissionResponse> getCourseSubmissions(String courseId, String experimentId, Integer submissionStatus) {
        LambdaQueryWrapper<ProcedureSubmission> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProcedureSubmission::getCourseId, courseId);

        if (experimentId != null && !experimentId.trim().isEmpty()) {
            queryWrapper.eq(ProcedureSubmission::getExperimentId, experimentId);
        }

        if (submissionStatus != null) {
            queryWrapper.eq(ProcedureSubmission::getSubmissionStatus, submissionStatus);
        }

        queryWrapper.orderByDesc(ProcedureSubmission::getCreateTime);

        List<ProcedureSubmission> submissions = list(queryWrapper);

        return submissions.stream().map(this::buildResponse).collect(Collectors.toList());
    }

    /**
     * 根据ID查询步骤提交详情
     *
     * @param submissionId 步骤提交ID
     * @return 步骤详情
     */
    public ProcedureSubmissionResponse getSubmissionById(Long submissionId) {
        ProcedureSubmission submission = getById(submissionId);
        if (submission == null || submission.getIsDeleted() == 1) {
            throw new BusinessException(404, "步骤提交记录不存在");
        }

        return buildResponse(submission);
    }

    /**
     * 批改步骤提交
     *
     * @param submissionId 步骤提交ID
     * @param teacherComment 教师评语
     * @param score 评分
     */
    @Transactional(rollbackFor = Exception.class)
    public void gradeProcedure(Long submissionId, String teacherComment, java.math.BigDecimal score) {
        ProcedureSubmission submission = getById(submissionId);
        if (submission == null || submission.getIsDeleted() == 1) {
            throw new BusinessException(404, "步骤提交记录不存在");
        }

        submission.setSubmissionStatus(ProcedureSubmission.STATUS_GRADED);
        submission.setTeacherComment(teacherComment);
        submission.setScore(score);
        submission.setUpdateTime(LocalDateTime.now());

        boolean updated = updateById(submission);
        if (!updated) {
            throw new BusinessException(500, "批改步骤失败");
        }

        log.info("批改实验步骤成功，ID：{}，评分：{}", submissionId, score);
    }

    /**
     * 删除步骤提交（已禁用）
     * 学生禁止删除步骤提交记录
     *
     * @param submissionId 步骤提交ID
     * @param studentUsername 当前登录用户名（用于权限验证）
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteProcedure(Long submissionId, String studentUsername) {
        // 学生禁止删除步骤
        throw new BusinessException(403, "学生禁止删除步骤提交记录");
    }

    /**
     * 构建响应对象
     */
    private ProcedureSubmissionResponse buildResponse(ProcedureSubmission submission) {
        ProcedureSubmissionResponse response = new ProcedureSubmissionResponse();
        response.setId(submission.getId());
        response.setCourseId(submission.getCourseId());
        response.setExperimentId(submission.getExperimentId());
        response.setStudentUsername(submission.getStudentUsername());

        // 查询学生姓名
        User student = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, submission.getStudentUsername())
        );
        response.setStudentName(student != null ? student.getName() : submission.getStudentUsername());

        response.setSubmissionType(submission.getSubmissionType());
        response.setFileName(submission.getFileName());
        response.setFileSize(submission.getFileSize());
        response.setSubmissionStatus(submission.getSubmissionStatus());
        response.setTeacherComment(submission.getTeacherComment());
        response.setScore(submission.getScore());
        response.setSubmissionTime(submission.getSubmissionTime());
        response.setCreateTime(submission.getCreateTime());

        // 生成文件下载密钥
        String currentUsername = SecurityUtil.getCurrentUsername().orElse(null);
        if (currentUsername != null) {
            String downloadKey = downloadService.generateFileKey(DownloadService.TYPE_SUBMISSION, submission.getId(), currentUsername);
            response.setDownloadKey(downloadKey);
        }

        return response;
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
}
