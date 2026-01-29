package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.DataCollectionMapper;
import com.example.demo.mapper.ProcedureTopicMapper;
import com.example.demo.mapper.ProcedureTopicMapMapper;
import com.example.demo.mapper.StudentProcedureAttachmentMapper;
import com.example.demo.pojo.entity.DataCollection;
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
    private final DataCollectionMapper dataCollectionMapper;

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
     * @param fillBlankAnswers 填空类型答案
     * @param tableCellAnswers 表格类型答案
     * @param photos          照片文件列表
     * @param documents       文档文件列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void completeDataCollectionProcedure(String studentUsername, String classCode,
                                                Long procedureId,
                                                java.util.Map<String, String> fillBlankAnswers,
                                                java.util.Map<String, String> tableCellAnswers,
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

        // 2. 将答案转换为JSON格式存储
        String answerJson = convertAnswersToJson(fillBlankAnswers, tableCellAnswers);

        // 3. 创建学生步骤答案记录
        StudentExperimentalProcedure studentProcedure = new StudentExperimentalProcedure();
        studentProcedure.setExperimentId(procedure.getExperimentId());
        studentProcedure.setStudentUsername(studentUsername);
        studentProcedure.setClassCode(classCode);
        studentProcedure.setExperimentalProcedureId(procedureId);
        studentProcedure.setNumber(procedure.getNumber());
        studentProcedure.setAnswer(answerJson);
        studentProcedure.setCreatedTime(LocalDateTime.now());

        boolean saved = studentExperimentalProcedureService.save(studentProcedure);
        if (!saved) {
            throw new BusinessException(500, "提交数据收集失败");
        }

        // 4. 自动判分（如果有正确答案）
        autoGradeDataCollectionProcedure(procedureId, studentProcedure.getId(),
                                         fillBlankAnswers, tableCellAnswers);

        // 5. 保存照片文件

        // 4. 保存照片文件
        if (photos != null && !photos.isEmpty()) {
            for (MultipartFile photo : photos) {
                saveAttachment(procedureId, studentUsername, classCode, photo, 1);
            }
        }

        // 5. 保存文档文件
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
     * 将答案转换为JSON格式
     *
     * @param fillBlankAnswers 填空类型答案
     * @param tableCellAnswers 表格类型答案
     * @return JSON字符串
     */
    private String convertAnswersToJson(java.util.Map<String, String> fillBlankAnswers,
                                       java.util.Map<String, String> tableCellAnswers) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            java.util.Map<String, Object> answerData = new java.util.HashMap<>();

            if (fillBlankAnswers != null && !fillBlankAnswers.isEmpty()) {
                answerData.put("fillBlankAnswers", fillBlankAnswers);
            }
            if (tableCellAnswers != null && !tableCellAnswers.isEmpty()) {
                answerData.put("tableCellAnswers", tableCellAnswers);
            }

            return objectMapper.writeValueAsString(answerData);
        } catch (Exception e) {
            log.error("转换答案为JSON失败", e);
            throw new BusinessException(500, "答案格式转换失败");
        }
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
            attachment.setFilePath(relativePath + File.separator + uniqueFileName);  // 只保存相对路径
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
     * 自动判分数据收集步骤
     * 如果配置了正确答案，则自动对比并评分
     *
     * @param procedureId        步骤ID
     * @param studentAnswerId    学生答案记录ID
     * @param fillBlankAnswers   填空类型答案
     * @param tableCellAnswers   表格类型答案
     */
    private void autoGradeDataCollectionProcedure(Long procedureId, Long studentAnswerId,
                                                  java.util.Map<String, String> fillBlankAnswers,
                                                  java.util.Map<String, String> tableCellAnswers) {
        try {
            // 1. 查询数据收集配置
            QueryWrapper<DataCollection> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("experimental_procedure_id", procedureId);
            DataCollection dataCollection = dataCollectionMapper.selectOne(queryWrapper);

            if (dataCollection == null || dataCollection.getCorrectAnswer() == null
                    || dataCollection.getCorrectAnswer().trim().isEmpty()) {
                // 没有配置正确答案，不进行自动判分
                return;
            }

            // 2. 解析正确答案
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            java.util.Map<String, String> correctAnswers = objectMapper.readValue(
                    dataCollection.getCorrectAnswer(),
                    new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, String>>() {}
            );

            if (correctAnswers == null || correctAnswers.isEmpty()) {
                return;
            }

            // 3. 对比答案并计算得分
            int totalQuestions = correctAnswers.size();
            int correctCount = 0;
            Double tolerance = dataCollection.getTolerance();

            // 判断填空类型答案
            if (fillBlankAnswers != null && !fillBlankAnswers.isEmpty()) {
                for (java.util.Map.Entry<String, String> entry : correctAnswers.entrySet()) {
                    String studentAnswer = fillBlankAnswers.get(entry.getKey());
                    if (isAnswerCorrect(studentAnswer, entry.getValue(), tolerance)) {
                        correctCount++;
                    }
                }
            }

            // 判断表格类型答案
            if (tableCellAnswers != null && !tableCellAnswers.isEmpty()) {
                for (java.util.Map.Entry<String, String> entry : correctAnswers.entrySet()) {
                    String studentAnswer = tableCellAnswers.get(entry.getKey());
                    if (isAnswerCorrect(studentAnswer, entry.getValue(), tolerance)) {
                        correctCount++;
                    }
                }
            }

            // 4. 计算得分（百分比制）
            java.math.BigDecimal score = totalQuestions > 0
                    ? new java.math.BigDecimal(correctCount * 100.0 / totalQuestions)
                        .setScale(2, java.math.BigDecimal.ROUND_HALF_UP)
                    : java.math.BigDecimal.ZERO;

            // 5. 更新学生答案记录
            StudentExperimentalProcedure studentProcedure = studentExperimentalProcedureService.getById(studentAnswerId);
            if (studentProcedure != null) {
                studentProcedure.setScore(score);
                studentProcedure.setIsGraded(2); // 2-系统自动评分
                studentProcedure.setTeacherComment("系统自动评分");
                studentExperimentalProcedureService.updateById(studentProcedure);

                log.info("自动判分完成，步骤ID：{}，学生答案ID：{}，得分：{}/{}",
                        procedureId, studentAnswerId, score, totalQuestions);
            }

        } catch (Exception e) {
            log.error("自动判分失败", e);
            // 判分失败不影响提交，只记录日志
        }
    }

    /**
     * 判断答案是否正确
     *
     * @param studentAnswer 学生答案
     * @param correctAnswer 正确答案
     * @param tolerance     误差范围
     * @return 是否正确
     */
    private boolean isAnswerCorrect(String studentAnswer, String correctAnswer, Double tolerance) {
        if (studentAnswer == null || correctAnswer == null) {
            return false;
        }

        // 尝试按数值比较
        try {
            double studentValue = Double.parseDouble(studentAnswer.trim());
            double correctValue = Double.parseDouble(correctAnswer.trim());

            if (tolerance != null && tolerance > 0) {
                // 有误差范围，判断是否在误差范围内
                return Math.abs(studentValue - correctValue) <= tolerance;
            } else {
                // 没有误差范围，精确匹配
                return Math.abs(studentValue - correctValue) < 0.0001;
            }
        } catch (NumberFormatException e) {
            // 不是数字，按字符串比较
            return studentAnswer.trim().equalsIgnoreCase(correctAnswer.trim());
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
