package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.ProcedureTopicMapper;
import com.example.demo.mapper.ProcedureTopicMapMapper;
import com.example.demo.mapper.StudentProcedureAttachmentMapper;
import com.example.demo.pojo.entity.ExperimentalProcedure;
import com.example.demo.pojo.entity.ProcedureTopic;
import com.example.demo.pojo.entity.ProcedureTopicMap;
import com.example.demo.pojo.entity.StudentExperimentalProcedure;
import com.example.demo.pojo.entity.StudentProcedureAttachment;
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
import java.util.Map;
import java.util.UUID;

/**
 * 学生步骤完成服务
 * 提供学生完成不同类型步骤的业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StudentProcedureCompletionService extends ServiceImpl<StudentProcedureAttachmentMapper, StudentProcedureAttachment> {

    private final StudentExperimentalProcedureService studentExperimentalProcedureService;
    private final ExperimentalProcedureService experimentalProcedureService;
    private final ProcedureTopicMapper procedureTopicMapper;
    private final ProcedureTopicMapMapper procedureTopicMapMapper;

    /** 步骤附件存储根路径 */
    private static final String ATTACHMENT_ROOT_PATH = "uploads" + File.separator + "procedure-attachments";

    /**
     * 完成题库练习（类型3）
     *
     * @param studentUsername 学生用户名
     * @param classCode       班级编号
     * @param procedureId     实验步骤ID
     * @param answers         题目答案Map（题目ID -> 答案）
     */
    @Transactional(rollbackFor = Exception.class)
    public void completeTopicProcedure(String studentUsername, String classCode,
                                       Long procedureId, Map<Long, String> answers) {
        // 1. 查询并验证步骤信息
        ExperimentalProcedure procedure = experimentalProcedureService.getById(procedureId);
        if (procedure == null || procedure.getIsDeleted()) {
            throw new BusinessException(404, "实验步骤不存在");
        }

        if (!Integer.valueOf(3).equals(procedure.getType())) {
            throw new BusinessException(400, "该步骤不是题库答题类型");
        }

        // 2. 查询题库配置，判断是随机还是老师选定
        ProcedureTopic procedureTopic = procedureTopicMapper.selectById(procedure.getProcedureTopicId());
        if (procedureTopic == null) {
            throw new BusinessException(404, "题库配置不存在");
        }

        // 3. 验证答案中的题目是否属于该步骤
        if (!Boolean.TRUE.equals(procedureTopic.getIsRandom())) {
            // 老师选定模式：验证题目ID是否在步骤的题目列表中
            QueryWrapper<ProcedureTopicMap> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("experimental_procedure_id", procedureId);
            long topicCount = procedureTopicMapMapper.selectCount(queryWrapper);

            if (answers.size() != topicCount) {
                throw new BusinessException(400,
                    String.format("应提交%d道题目，实际提交%d道", topicCount, answers.size()));
            }
        }

        // 4. 构建答案字符串
        StringBuilder answerBuilder = new StringBuilder();
        for (Map.Entry<Long, String> entry : answers.entrySet()) {
            answerBuilder.append(entry.getKey()).append(":").append(entry.getValue()).append(";");
        }

        // 5. 创建学生步骤答案记录
        StudentExperimentalProcedure studentProcedure = new StudentExperimentalProcedure();
        studentProcedure.setExperimentId(procedure.getExperimentId());
        studentProcedure.setStudentUsername(studentUsername);
        studentProcedure.setClassCode(classCode);
        studentProcedure.setExperimentalProcedureId(procedureId);
        studentProcedure.setNumber(procedure.getNumber());
        studentProcedure.setAnswer(answerBuilder.toString());
        studentProcedure.setCreatedTime(LocalDateTime.now());

        boolean saved = studentExperimentalProcedureService.save(studentProcedure);
        if (!saved) {
            throw new BusinessException(500, "提交题库答案失败");
        }

        log.info("学生 {} 在班级 {} 完成题库练习，步骤：{}，题目数：{}",
                studentUsername, classCode, procedureId, answers.size());
    }

    /**
     * 完成数据收集（类型2）
     *
     * @param studentUsername 学生用户名
     * @param classCode       班级编号
     * @param procedureId     实验步骤ID
     * @param dataAnswer      数据答案
     * @param photos          照片文件列表
     * @param documents       文档文件列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void completeDataCollectionProcedure(String studentUsername, String classCode,
                                                Long procedureId, String dataAnswer,
                                                List<MultipartFile> photos,
                                                List<MultipartFile> documents) {
        // 1. 查询并验证步骤信息
        ExperimentalProcedure procedure = experimentalProcedureService.getById(procedureId);
        if (procedure == null || procedure.getIsDeleted()) {
            throw new BusinessException(404, "实验步骤不存在");
        }

        if (!Integer.valueOf(2).equals(procedure.getType())) {
            throw new BusinessException(400, "该步骤不是数据收集类型");
        }

        // 2. 创建学生步骤答案记录
        StudentExperimentalProcedure studentProcedure = new StudentExperimentalProcedure();
        studentProcedure.setExperimentId(procedure.getExperimentId());
        studentProcedure.setStudentUsername(studentUsername);
        studentProcedure.setClassCode(classCode);
        studentProcedure.setExperimentalProcedureId(procedureId);
        studentProcedure.setNumber(procedure.getNumber());
        studentProcedure.setAnswer(dataAnswer != null ? dataAnswer : "");
        studentProcedure.setCreatedTime(LocalDateTime.now());

        boolean saved = studentExperimentalProcedureService.save(studentProcedure);
        if (!saved) {
            throw new BusinessException(500, "提交数据收集失败");
        }

        // 3. 保存照片文件
        if (photos != null && !photos.isEmpty()) {
            for (MultipartFile photo : photos) {
                saveAttachment(procedureId, studentUsername, classCode, photo, 1);
            }
        }

        // 4. 保存文档文件
        if (documents != null && !documents.isEmpty()) {
            for (MultipartFile document : documents) {
                saveAttachment(procedureId, studentUsername, classCode, document, 2);
            }
        }

        log.info("学生 {} 在班级 {} 完成数据收集，步骤：{}，照片：{}，文档：{}",
                studentUsername, classCode, procedureId,
                photos != null ? photos.size() : 0,
                documents != null ? documents.size() : 0);
    }

    /**
     * 保存附件文件
     *
     * @param procedureId     实验步骤ID
     * @param studentUsername 学生用户名
     * @param classCode       班级编号
     * @param file            文件
     * @param fileType        文件类型（1-照片，2-文档）
     */
    private void saveAttachment(Long procedureId, String studentUsername, String classCode,
                                MultipartFile file, Integer fileType) {
        try {
            // 1. 验证文件
            if (file == null || file.isEmpty()) {
                throw new BusinessException(400, "文件不能为空");
            }

            // 2. 生成存储路径
            String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String relativePath = procedureId + File.separator + classCode + File.separator + datePath;
            String uploadDir = ATTACHMENT_ROOT_PATH + File.separator + relativePath;

            // 3. 创建目录
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                boolean created = directory.mkdirs();
                if (!created) {
                    throw new BusinessException(500, "创建存储目录失败");
                }
            }

            // 4. 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String uniqueFileName = UUID.randomUUID().toString() + "." + extension;
            String filePath = uploadDir + File.separator + uniqueFileName;

            // 5. 保存文件
            Path targetPath = Paths.get(filePath);
            file.transferTo(targetPath);

            // 6. 创建附件记录
            StudentProcedureAttachment attachment = new StudentProcedureAttachment();
            attachment.setProcedureId(procedureId);
            attachment.setStudentUsername(studentUsername);
            attachment.setClassCode(classCode);
            attachment.setFileType(fileType);
            attachment.setFileFormat(extension);
            attachment.setOriginalFileName(originalFilename);
            attachment.setStoredFileName(uniqueFileName);
            attachment.setFilePath(filePath);
            attachment.setFileSize(file.getSize());
            attachment.setCreateTime(LocalDateTime.now());

            boolean saved = save(attachment);
            if (!saved) {
                // 如果保存失败，删除已上传的文件
                Files.deleteIfExists(targetPath);
                throw new BusinessException(500, "保存附件信息失败");
            }

        } catch (IOException e) {
            log.error("保存附件文件失败", e);
            throw new BusinessException(500, "保存附件失败: " + e.getMessage());
        }
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
