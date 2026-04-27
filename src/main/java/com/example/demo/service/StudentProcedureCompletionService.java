package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.*;
import com.example.demo.pojo.entity.*;
import com.example.demo.pojo.request.student.CompleteTimedQuizProcedureRequest;
import com.example.demo.util.AnswerMapJSONUntil;
import com.example.demo.util.DataCollectionDataUtil;
import com.example.demo.util.TimedQuizKeyGenerator;
import com.example.demo.util.TopicAnswerContractUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
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
    private final TimedQuizProcedureMapper timedQuizProcedureMapper;
    private final TopicMapper topicMapper;
    private final TimedQuizKeyGenerator timedQuizKeyGenerator;
    private final ClassExperimentMapper classExperimentMapper;
    private final ClassExperimentClassRelationMapper classExperimentClassRelationMapper;

    @Value("${file.upload.path}")
    private String uploadBasePath;

    /** 步骤附件存储子目录 */
    private static final String ATTACHMENT_SUB_PATH = "attachments";

    private static final Integer GRADE_STATUS_NOT_GRADED = 0;
    private static final Integer GRADE_STATUS_TEACHER_GRADED = 1;
    private static final Integer GRADE_STATUS_AUTO_GRADED = 2;

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
        // 1. 检查是否已提交
        StudentExperimentalProcedure existing = studentExperimentalProcedureService.getByStudentAndProcedure(
                studentUsername, classCode, procedureId);
        if (existing != null) {
            throw new BusinessException(400, "该步骤已提交，如需修改请使用修改接口");
        }

        // 2. 查询并验证步骤信息
        ExperimentalProcedure procedure = experimentalProcedureService.getById(procedureId);
        if (procedure == null || procedure.getIsDeleted()) {
            throw new BusinessException(404, "实验步骤不存在");
        }

        if (!Integer.valueOf(3).equals(procedure.getType())) {
            throw new BusinessException(400, "该步骤不是题库答题类型");
        }

        // 3. 查询题库配置，判断是随机还是老师选定
        ProcedureTopic procedureTopic = procedureTopicMapper.selectById(procedure.getProcedureTopicId());
        if (procedureTopic == null) {
            throw new BusinessException(404, "题库配置不存在");
        }

        // 3. 验证答案中的题目是否属于该步骤
        if (!Boolean.TRUE.equals(procedureTopic.getIsRandom())) {
            // 老师选定模式：验证题目ID是否在步骤的题目列表中
            LambdaQueryWrapper<ProcedureTopicMap> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ProcedureTopicMap::getExperimentalProcedureId, procedureId);
            List<ProcedureTopicMap> topicMaps = procedureTopicMapMapper.selectList(queryWrapper);
            long topicCount = topicMaps.size();

            if (answers.size() != topicCount) {
                throw new BusinessException(400,
                    String.format("应提交%d道题目，实际提交%d道", topicCount, answers.size()));
            }

            validateSubmittedTopicIds(
                answers.keySet(),
                topicMaps.stream().map(ProcedureTopicMap::getTopicId).toList()
            );
        }

        Map<Long, String> normalizedAnswers = normalizeTopicAnswers(answers);

        // 5. 创建学生步骤答案记录
        StudentExperimentalProcedure studentProcedure = new StudentExperimentalProcedure();
        studentProcedure.setExperimentId(procedure.getExperimentId());
        studentProcedure.setStudentUsername(studentUsername);
        studentProcedure.setClassCode(classCode);
        studentProcedure.setExperimentalProcedureId(procedureId);
        studentProcedure.setNumber(procedure.getNumber());
        studentProcedure.setAnswer(AnswerMapJSONUntil.toTopicJson(normalizedAnswers));
        studentProcedure.setCreatedTime(LocalDateTime.now());
        getClassExperimentId(classCode, procedure, studentProcedure, classExperimentClassRelationMapper, classExperimentMapper);

        boolean saved = studentExperimentalProcedureService.save(studentProcedure);
        if (!saved) {
            throw new BusinessException(500, "提交题库答案失败");
        }

        autoGradeTopicProcedure(studentProcedure.getId(), normalizedAnswers);

        log.info("学生 {} 在班级 {} 完成题库练习，步骤：{}，题目数：{}",
                studentUsername, classCode, procedureId, normalizedAnswers.size());
    }

    static void getClassExperimentId(String classCode, ExperimentalProcedure procedure, StudentExperimentalProcedure studentProcedure, ClassExperimentClassRelationMapper classExperimentClassRelationMapper, ClassExperimentMapper classExperimentMapper) {
        List<Long> list = classExperimentClassRelationMapper.selectList(new LambdaQueryWrapper<ClassExperimentClassRelation>().eq(ClassExperimentClassRelation::getClassCode, classCode)).stream().map(ClassExperimentClassRelation::getId).toList();
        ClassExperiment classExperiment = classExperimentMapper.selectOne(new LambdaQueryWrapper<ClassExperiment>().in(ClassExperiment::getId, list).eq(ClassExperiment::getExperimentId, procedure.getExperimentId()), false);
        studentProcedure.setClassExperimentId(classExperiment.getId());
    }

    /**
     * 完成数据收集（类型2）
     *
     * @param studentUsername 学生用户名
     * @param classCode       班级编号
     * @param procedureId     实验步骤ID
     * @param fillBlankAnswers 填空类型答案
     * @param tableCellAnswers 表格类型答案
     * @param attachments     附件文件列表（不区分照片和文档）
     */
    @Transactional(rollbackFor = Exception.class)
    public void completeDataCollectionProcedure(String studentUsername, String classCode,
                                                Long procedureId,
                                                java.util.Map<String, String> fillBlankAnswers,
                                                java.util.Map<String, String> tableCellAnswers,
                                                List<MultipartFile> attachments) {
        // 1. 检查是否已提交
        StudentExperimentalProcedure existing = studentExperimentalProcedureService.getByStudentAndProcedure(
                studentUsername, classCode, procedureId);
        if (existing != null) {
            throw new BusinessException(400, "该步骤已提交，如需修改请使用修改接口");
        }

        // 2. 查询并验证步骤信息
        ExperimentalProcedure procedure = experimentalProcedureService.getById(procedureId);
        if (procedure == null || procedure.getIsDeleted()) {
            throw new BusinessException(404, "实验步骤不存在");
        }

        if (!Integer.valueOf(2).equals(procedure.getType())) {
            throw new BusinessException(400, "该步骤不是数据收集类型");
        }

        // 3. 查询数据收集配置，判断数据类型
        LambdaQueryWrapper<DataCollection> dcQuery = new LambdaQueryWrapper<>();
        dcQuery.eq(DataCollection::getExperimentalProcedureId, procedureId);
        DataCollection dataCollectionConfig = dataCollectionMapper.selectOne(dcQuery);

        if (dataCollectionConfig == null) {
            throw new BusinessException(400, "数据收集配置不存在");
        }

        Long dataType = dataCollectionConfig.getType();

        // 4. 根据数据类型验证答案数据
        if (dataType == 1) {
            // 填空类型必须有填空答案
            if (fillBlankAnswers == null || fillBlankAnswers.isEmpty()) {
                throw new BusinessException(400, "填空类型必须提交填空答案");
            }
        } else if (dataType == 2) {
            // 表格类型必须有表格答案
            if (tableCellAnswers == null || tableCellAnswers.isEmpty()) {
                throw new BusinessException(400, "表格类型必须提交表格答案");
            }
        } else if (dataType == 3) {
            // 文件数据类型：根据老师配置验证上传文件
            boolean needPhoto = Boolean.TRUE.equals(dataCollectionConfig.getNeedPhoto());
            boolean needDoc = Boolean.TRUE.equals(dataCollectionConfig.getNeedDoc());
            boolean hasAttachments = attachments != null && !attachments.isEmpty();

            // 统计附件中的图片和文档数量
            int photoCount = 0;
            int docCount = 0;
            if (hasAttachments) {
                for (MultipartFile file : attachments) {
                    if (isImageFile(file)) {
                        photoCount++;
                    } else {
                        docCount++;
                    }
                }
            }

            // 根据老师配置验证上传要求
            if (needPhoto && needDoc) {
                // 老师要求图片和文档都需要，必须同时上传
                if (photoCount == 0 || docCount == 0) {
                    throw new BusinessException(400, "请同时上传图片和文档");
                }
            } else if (needPhoto) {
                // 老师只要求图片
                if (photoCount == 0) {
                    throw new BusinessException(400, "请上传图片");
                }
            } else if (needDoc) {
                // 老师只要求文档
                if (docCount == 0) {
                    throw new BusinessException(400, "请上传文档");
                }
            } else {
                // 老师未指定具体类型，至少上传一个文件
                if (!hasAttachments) {
                    throw new BusinessException(400, "请至少上传一个文件");
                }
            }
        }

        // 5. 创建学生步骤答案记录
        StudentExperimentalProcedure studentProcedure = new StudentExperimentalProcedure();
        studentProcedure.setExperimentId(procedure.getExperimentId());
        studentProcedure.setStudentUsername(studentUsername);
        studentProcedure.setClassCode(classCode);
        studentProcedure.setExperimentalProcedureId(procedureId);
        studentProcedure.setNumber(procedure.getNumber());
        getClassExperimentId(classCode,procedure,studentProcedure,classExperimentClassRelationMapper,classExperimentMapper);

        // 根据数据类型生成不同的答案 JSON
        if (dataType == 3) {
            studentProcedure.setAnswer(AnswerMapJSONUntil.toFileUploadJson());
        } else {
            studentProcedure.setAnswer(AnswerMapJSONUntil.toDataCollectionJson(fillBlankAnswers, tableCellAnswers));
        }
        studentProcedure.setCreatedTime(LocalDateTime.now());

        boolean saved = studentExperimentalProcedureService.save(studentProcedure);
        if (!saved) {
            throw new BusinessException(500, "提交数据收集失败");
        }

        if (shouldTriggerAutoGrade(dataType)) {
            autoGradeDataCollectionProcedure(procedureId, studentProcedure.getId(),
                                             fillBlankAnswers, tableCellAnswers);
        }

        // 7. 保存附件文件
        if (attachments != null && !attachments.isEmpty()) {
            for (MultipartFile file : attachments) {
                int fileType = isImageFile(file) ? 1 : 2;
                saveAttachment(procedureId, studentUsername, classCode, file, fileType);
            }
        }

        log.info("学生 {} 在班级 {} 完成数据收集，步骤：{}，附件数：{}",
                studentUsername, classCode, procedureId,
                attachments != null ? attachments.size() : 0);
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
            };

            // 2. 生成存储路径（不区分照片和文档，统一存储在 attachments/ 下）
            String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String relativePath = ATTACHMENT_SUB_PATH + File.separator + procedureId + File.separator + classCode + File.separator + datePath;
            String uploadDir = uploadBasePath + relativePath;

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
    private AutoGradeExecutionResult autoGradeDataCollectionProcedure(Long procedureId, Long studentAnswerId,
                                                  java.util.Map<String, String> fillBlankAnswers,
                                                  java.util.Map<String, String> tableCellAnswers) {
        try {
            // 1. 查询数据收集配置
            LambdaQueryWrapper<DataCollection> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DataCollection::getExperimentalProcedureId, procedureId);
            DataCollection dataCollection = dataCollectionMapper.selectOne(queryWrapper);

            if (dataCollection == null) {
                return AutoGradeExecutionResult.skipped("数据收集配置不存在");
            }

            if (!shouldTriggerAutoGrade(dataCollection.getType())) {
                return AutoGradeExecutionResult.skipped("当前步骤不支持机器批改");
            }

            if (dataCollection.getCorrectAnswer() == null || dataCollection.getCorrectAnswer().trim().isEmpty()) {
                return AutoGradeExecutionResult.skipped("未配置正确答案");
            }

            // 2. 解析正确答案和误差配置
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            java.util.Map<String, String> correctAnswers = objectMapper.readValue(
                    dataCollection.getCorrectAnswer(),
                    new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, String>>() {}
            );

            if (correctAnswers == null || correctAnswers.isEmpty()) {
                return AutoGradeExecutionResult.skipped("正确答案为空");
            }

            // 解析字段级/单元格级/列级误差配置
            Map<String, Double> fieldTolerances = Map.of();
            Map<String, Double> cellTolerances = Map.of();
            Map<String, Double> columnTolerances = Map.of();
            if (dataCollection.getRemark() != null && !dataCollection.getRemark().isEmpty()) {
                // 从remark字段的JSON中解析误差配置
                fieldTolerances = DataCollectionDataUtil.parseFieldTolerancesFromJson(dataCollection.getRemark());
                cellTolerances = DataCollectionDataUtil.parseCellTolerancesFromJson(dataCollection.getRemark());
                columnTolerances = DataCollectionDataUtil.parseColumnTolerancesFromJson(dataCollection.getRemark());
            }

            // 3. 对比答案并计算得分
            int totalQuestions = correctAnswers.size();
            int correctCount = 0;
            Double stepTolerance = dataCollection.getTolerance();

            // 判断填空类型答案
            if (fillBlankAnswers != null && !fillBlankAnswers.isEmpty()) {
                for (java.util.Map.Entry<String, String> entry : correctAnswers.entrySet()) {
                    String fieldName = entry.getKey();
                    String studentAnswer = fillBlankAnswers.get(fieldName);
                    // 优先使用字段级误差，没有则使用步骤级误差
                    Double fieldTolerance = fieldTolerances.getOrDefault(fieldName, stepTolerance);
                    if (isAnswerCorrect(studentAnswer, entry.getValue(), fieldTolerance)) {
                        correctCount++;
                    }
                }
            }

            // 判断表格类型答案
            if (tableCellAnswers != null && !tableCellAnswers.isEmpty()) {
                for (java.util.Map.Entry<String, String> entry : correctAnswers.entrySet()) {
                    String positionKey = entry.getKey(); // 格式: "rowIndex-columnIndex"，如 "0-0"
                    String studentAnswer = tableCellAnswers.get(positionKey);

                    // 误差优先级：单元格级 > 列级 > 步骤级
                    Double finalTolerance = cellTolerances.get(positionKey);
                    if (finalTolerance == null) {
                        // 从位置 key 中提取列索引（如 "0-1" → "1"）
                        String columnIndex = positionKey.split("-")[1];
                        finalTolerance = columnTolerances.get(columnIndex);
                        if (finalTolerance == null) {
                            finalTolerance = stepTolerance;
                        }
                    }

                    if (isAnswerCorrect(studentAnswer, entry.getValue(), finalTolerance)) {
                        correctCount++;
                    }
                }
            }

            // 4. 计算得分（百分比制）
            java.math.BigDecimal score = new java.math.BigDecimal(correctCount * 100.0 / totalQuestions)
                .setScale(2, RoundingMode.HALF_UP);

            // 5. 更新学生答案记录
            StudentExperimentalProcedure studentProcedure = studentExperimentalProcedureService.getById(studentAnswerId);
            if (studentProcedure == null) {
                return AutoGradeExecutionResult.failed("学生答案记录不存在");
            }
            if (!canAutoGradeRecord(studentProcedure)) {
                return AutoGradeExecutionResult.skipped("人工批改记录跳过");
            }

            studentProcedure.setScore(score);
            studentProcedure.setIsGraded(GRADE_STATUS_AUTO_GRADED);
            studentProcedure.setTeacherComment("系统自动评分");
            studentExperimentalProcedureService.updateById(studentProcedure);

            log.info("自动判分完成，步骤ID：{}，学生答案ID：{}，得分：{}/{}",
                    procedureId, studentAnswerId, score, totalQuestions);
            return AutoGradeExecutionResult.success("自动判分成功");

        } catch (Exception e) {
            log.error("自动判分失败", e);
            return AutoGradeExecutionResult.failed("自动判分失败: " + e.getMessage());
        }
    }

    /**
     * 判断答案是否正确
     *
     * @param studentAnswer 学生答案
     * @param correctAnswer 正确答案
     * @param tolerance     误差百分比（单位：%）
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
                // 正确值为0时，使用精确匹配（避免除以0错误）
                if (Math.abs(correctValue) < 0.0001) {
                    return Math.abs(studentValue - correctValue) < 0.0001;
                }
                // 百分比误差计算：|学生答案 - 正确答案| / |正确答案| ≤ 误差百分比 / 100
                double relativeError = Math.abs(studentValue - correctValue) / Math.abs(correctValue);
                return relativeError <= tolerance / 100.0;
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

    /**
     * 判断文件是否为图片类型
     *
     * @param file 上传的文件
     * @return 是否为图片
     */
    private boolean isImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        String filename = file.getOriginalFilename();
        if (filename == null) {
            return false;
        }
        String extension = getFileExtension(filename);
        // 常见图片扩展名
        java.util.Set<String> imageExtensions = new java.util.HashSet<>(
            java.util.Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "webp", "tiff", "tif")
        );
        return imageExtensions.contains(extension);
    }

    /**
     * 修改题库练习答案（类型3）
     *
     * @param studentUsername 学生用户名
     * @param classCode       班级编号
     * @param procedureId     实验步骤ID
     * @param answers         题目答案Map
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateTopicProcedure(String studentUsername, String classCode,
                                      Long procedureId, Map<Long, String> answers) {
        // 1. 验证是否可修改
        if (!studentExperimentalProcedureService.isProcedureModifiable(
                studentUsername, classCode, procedureId)) {
            throw new BusinessException(400, "当前不允许修改该步骤");
        }

        // 2. 查询并验证步骤信息
        ExperimentalProcedure procedure = experimentalProcedureService.getById(procedureId);
        if (procedure == null || procedure.getIsDeleted()) {
            throw new BusinessException(404, "实验步骤不存在");
        }

        if (!Integer.valueOf(3).equals(procedure.getType())) {
            throw new BusinessException(400, "该步骤不是题库答题类型");
        }

        // 3. 验证答案中的题目是否属于该步骤
        ProcedureTopic procedureTopic = procedureTopicMapper.selectById(procedure.getProcedureTopicId());
        if (procedureTopic == null) {
            throw new BusinessException(404, "题库配置不存在");
        }

        if (!Boolean.TRUE.equals(procedureTopic.getIsRandom())) {
            LambdaQueryWrapper<ProcedureTopicMap> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ProcedureTopicMap::getExperimentalProcedureId, procedureId);
            List<ProcedureTopicMap> topicMaps = procedureTopicMapMapper.selectList(queryWrapper);
            long topicCount = topicMaps.size();

            if (answers.size() != topicCount) {
                throw new BusinessException(400,
                    String.format("应提交%d道题目，实际提交%d道", topicCount, answers.size()));
            }

            validateSubmittedTopicIds(
                answers.keySet(),
                topicMaps.stream().map(ProcedureTopicMap::getTopicId).toList()
            );
        }

        Map<Long, String> normalizedAnswers = normalizeTopicAnswers(answers);

        // 5. 查询现有记录并更新
        StudentExperimentalProcedure studentProcedure =
                studentExperimentalProcedureService.getByStudentAndProcedure(
                        studentUsername, classCode, procedureId);

        studentProcedure.setAnswer(AnswerMapJSONUntil.toTopicJson(normalizedAnswers));
        studentProcedure.setScore(null); // 清除之前分数
        studentProcedure.setIsGraded(0); // 重置为未评分
        studentProcedure.setTeacherComment(null); // 清除评语

        boolean updated = studentExperimentalProcedureService.updateById(studentProcedure);
        if (!updated) {
            throw new BusinessException(500, "修改题库答案失败");
        }

        autoGradeTopicProcedure(studentProcedure.getId(), normalizedAnswers);

        log.info("学生 {} 在班级 {} 修改题库练习，步骤：{}，题目数：{}",
                studentUsername, classCode, procedureId, normalizedAnswers.size());
    }

    /**
     * 修改数据收集答案（类型2）
     *
     * @param studentUsername         学生用户名
     * @param classCode               班级编号
     * @param procedureId             实验步骤ID
     * @param fillBlankAnswers        填空类型答案
     * @param tableCellAnswers        表格类型答案
     * @param attachments             新附件文件列表（不区分照片和文档）
     * @param attachmentIdsToDelete   需要删除的附件ID列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateDataCollectionProcedure(String studentUsername, String classCode,
                                              Long procedureId,
                                              Map<String, String> fillBlankAnswers,
                                              Map<String, String> tableCellAnswers,
                                              List<MultipartFile> attachments,
                                              List<Long> attachmentIdsToDelete) {
        // 1. 验证是否可修改
        if (!studentExperimentalProcedureService.isProcedureModifiable(
                studentUsername, classCode, procedureId)) {
            throw new BusinessException(400, "当前不允许修改该步骤");
        }

        // 2. 查询并验证步骤信息
        ExperimentalProcedure procedure = experimentalProcedureService.getById(procedureId);
        if (procedure == null || procedure.getIsDeleted()) {
            throw new BusinessException(404, "实验步骤不存在");
        }

        if (!Integer.valueOf(2).equals(procedure.getType())) {
            throw new BusinessException(400, "该步骤不是数据收集类型");
        }

        // 3. 查询数据收集配置，判断数据类型
        LambdaQueryWrapper<DataCollection> dcQuery = new LambdaQueryWrapper<>();
        dcQuery.eq(DataCollection::getExperimentalProcedureId, procedureId);
        DataCollection dataCollectionConfig = dataCollectionMapper.selectOne(dcQuery);

        if (dataCollectionConfig == null) {
            throw new BusinessException(400, "数据收集配置不存在");
        }

        Long dataType = dataCollectionConfig.getType();

        // 4. 根据数据类型验证答案数据
        if (dataType == 1) {
            // 填空类型必须有填空答案
            if (fillBlankAnswers == null || fillBlankAnswers.isEmpty()) {
                throw new BusinessException(400, "填空类型必须提交填空答案");
            }
        } else if (dataType == 2) {
            // 表格类型必须有表格答案
            if (tableCellAnswers == null || tableCellAnswers.isEmpty()) {
                throw new BusinessException(400, "表格类型必须提交表格答案");
            }
        } else if (dataType == 3) {
            // 文件数据类型：根据老师配置验证上传文件
            boolean needPhoto = Boolean.TRUE.equals(dataCollectionConfig.getNeedPhoto());
            boolean needDoc = Boolean.TRUE.equals(dataCollectionConfig.getNeedDoc());

            // 修改时需验证是否满足上传要求
            if (needPhoto || needDoc) {
                // 查询现有附件
                LambdaQueryWrapper<StudentProcedureAttachment> attachQuery = new LambdaQueryWrapper<>();
                attachQuery.eq(StudentProcedureAttachment::getProcedureId, procedureId)
                        .eq(StudentProcedureAttachment::getStudentUsername, studentUsername)
                        .eq(StudentProcedureAttachment::getClassCode, classCode);
                List<StudentProcedureAttachment> existingAttachments = list(attachQuery);

                // 计算新附件中的图片和文档数量
                int newPhotoCount = 0;
                int newDocCount = 0;
                if (attachments != null && !attachments.isEmpty()) {
                    for (MultipartFile file : attachments) {
                        if (isImageFile(file)) {
                            newPhotoCount++;
                        } else {
                            newDocCount++;
                        }
                    }
                }

                // 计算删除后剩余的附件
                long remainingPhotoCount = existingAttachments.stream()
                        .filter(a -> a.getFileType() == 1)
                        .count();
                long remainingDocCount = existingAttachments.stream()
                        .filter(a -> a.getFileType() == 2)
                        .count();

                if (attachmentIdsToDelete != null && !attachmentIdsToDelete.isEmpty()) {
                    // 统计要删除的各类型附件数量
                    for (Long attachId : attachmentIdsToDelete) {
                        StudentProcedureAttachment attach = baseMapper.selectById(attachId);
                        if (attach != null) {
                            if (attach.getFileType() == 1) remainingPhotoCount--;
                            else if (attach.getFileType() == 2) remainingDocCount--;
                        }
                    }
                }

                boolean hasPhotos = remainingPhotoCount > 0 || newPhotoCount > 0;
                boolean hasDocs = remainingDocCount > 0 || newDocCount > 0;

                // 根据老师配置验证上传要求
                if (needPhoto && needDoc) {
                    // 老师要求图片和文档都需要，必须同时上传
                    if (!hasPhotos || !hasDocs) {
                        throw new BusinessException(400, "请同时上传图片和文档");
                    }
                } else if (needPhoto) {
                    if (!hasPhotos) {
                        throw new BusinessException(400, "请上传图片");
                    }
                } else if (needDoc) {
                    if (!hasDocs) {
                        throw new BusinessException(400, "请上传文档");
                    }
                }
            }
        }

        // 5. 查询现有记录并更新
        StudentExperimentalProcedure studentProcedure =
                studentExperimentalProcedureService.getByStudentAndProcedure(
                        studentUsername, classCode, procedureId);

        // 根据数据类型生成不同的答案 JSON
        if (dataType == 3) {
            studentProcedure.setAnswer(AnswerMapJSONUntil.toFileUploadJson());
        } else {
            studentProcedure.setAnswer(AnswerMapJSONUntil.toDataCollectionJson(fillBlankAnswers, tableCellAnswers));
        }

        if (canAutoGradeRecord(studentProcedure)) {
            resetMachineGrade(studentProcedure);
        }

        boolean updated = studentExperimentalProcedureService.updateById(studentProcedure);
        if (!updated) {
            throw new BusinessException(500, "修改数据收集失败");
        }

        // 6. 删除指定的旧附件
        if (attachmentIdsToDelete != null && !attachmentIdsToDelete.isEmpty()) {
            for (Long attachmentId : attachmentIdsToDelete) {
                deleteAttachment(attachmentId, studentUsername, classCode, procedureId);
            }
        }

        // 7. 保存新附件文件
        if (attachments != null && !attachments.isEmpty()) {
            for (MultipartFile file : attachments) {
                int fileType = isImageFile(file) ? 1 : 2;
                saveAttachment(procedureId, studentUsername, classCode, file, fileType);
            }
        }

        // 8. 重新自动判分
        if (shouldTriggerAutoGrade(dataType) && canAutoGradeRecord(studentProcedure)) {
            autoGradeDataCollectionProcedure(procedureId, studentProcedure.getId(),
                                             fillBlankAnswers, tableCellAnswers);
        }

        log.info("学生 {} 在班级 {} 修改数据收集，步骤：{}", studentUsername, classCode, procedureId);
    }

    private boolean shouldTriggerAutoGrade(Long dataType) {
        return dataType != null && (dataType == 1L || dataType == 2L);
    }

    private void autoGradeTopicProcedure(Long studentProcedureId, Map<Long, String> answers) {
        StudentExperimentalProcedure studentProcedure = studentExperimentalProcedureService.getById(studentProcedureId);
        if (studentProcedure == null) {
            throw new BusinessException(500, "题库答案记录不存在");
        }

        try {
            if (answers == null || answers.isEmpty()) {
                resetMachineGrade(studentProcedure);
                studentExperimentalProcedureService.updateById(studentProcedure);
                return;
            }

            List<Topic> topics = loadTopicsByIds(answers.keySet());
            if (topics.size() != answers.size()) {
                resetMachineGrade(studentProcedure);
                studentExperimentalProcedureService.updateById(studentProcedure);
                return;
            }

            Map<Long, Topic> topicMap = new HashMap<>();
            for (Topic topic : topics) {
                topicMap.put(topic.getId(), topic);
            }

            int totalCount = answers.size();
            int correctCount = 0;
            for (Map.Entry<Long, String> entry : answers.entrySet()) {
                Topic topic = topicMap.get(entry.getKey());
                if (topic == null || topic.getCorrectAnswer() == null || topic.getCorrectAnswer().trim().isEmpty()) {
                    resetMachineGrade(studentProcedure);
                    studentExperimentalProcedureService.updateById(studentProcedure);
                    return;
                }
                if (TopicAnswerContractUtil.answersEqual(topic.getType(), entry.getValue(), topic.getCorrectAnswer())) {
                    correctCount++;
                }
            }

            studentProcedure.setScore(new java.math.BigDecimal(correctCount * 100.0 / totalCount)
                    .setScale(2, RoundingMode.HALF_UP));
            studentProcedure.setIsGraded(GRADE_STATUS_AUTO_GRADED);
            studentProcedure.setTeacherComment("系统自动评分");
            studentExperimentalProcedureService.updateById(studentProcedure);
        } catch (Exception e) {
            log.error("题库练习自动评分失败，studentProcedureId={}", studentProcedureId, e);
            resetMachineGrade(studentProcedure);
            studentExperimentalProcedureService.updateById(studentProcedure);
        }
    }

    private boolean canAutoGradeRecord(StudentExperimentalProcedure studentProcedure) {
        return studentProcedure == null
                || studentProcedure.getIsGraded() == null
                || GRADE_STATUS_NOT_GRADED.equals(studentProcedure.getIsGraded())
                || GRADE_STATUS_AUTO_GRADED.equals(studentProcedure.getIsGraded());
    }

    private void resetMachineGrade(StudentExperimentalProcedure studentProcedure) {
        studentProcedure.setScore(null);
        studentProcedure.setIsGraded(GRADE_STATUS_NOT_GRADED);
        studentProcedure.setTeacherComment(null);
    }

    public AutoGradeExecutionResult autoGradeExistingDataCollectionSubmission(Long studentProcedureId) {
        StudentExperimentalProcedure studentProcedure = studentExperimentalProcedureService.getById(studentProcedureId);
        if (studentProcedure == null) {
            return AutoGradeExecutionResult.skipped("提交记录不存在");
        }
        if (!canAutoGradeRecord(studentProcedure)) {
            return AutoGradeExecutionResult.skipped("人工批改记录跳过");
        }

        ExperimentalProcedure procedure = experimentalProcedureService.getById(studentProcedure.getExperimentalProcedureId());
        if (procedure == null || procedure.getIsDeleted() || !Integer.valueOf(2).equals(procedure.getType())) {
            return AutoGradeExecutionResult.skipped("非数据收集步骤");
        }

        LambdaQueryWrapper<DataCollection> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DataCollection::getExperimentalProcedureId, procedure.getId());
        DataCollection dataCollection = dataCollectionMapper.selectOne(queryWrapper);
        if (dataCollection == null || !shouldTriggerAutoGrade(dataCollection.getType())) {
            return AutoGradeExecutionResult.skipped("当前步骤不支持机器批改");
        }

        DataCollectionAnswerMaps answerMaps = parseStoredDataCollectionAnswer(studentProcedure.getAnswer());
        if (answerMaps.isEmpty()) {
            return AutoGradeExecutionResult.failed("学生答案为空或解析失败");
        }

        resetMachineGrade(studentProcedure);
        boolean updated = studentExperimentalProcedureService.updateById(studentProcedure);
        if (!updated) {
            return AutoGradeExecutionResult.failed("重置机器评分状态失败");
        }

        return autoGradeDataCollectionProcedure(
                procedure.getId(),
                studentProcedureId,
                answerMaps.getFillBlankAnswers(),
                answerMaps.getTableCellAnswers()
        );
    }

    private DataCollectionAnswerMaps parseStoredDataCollectionAnswer(String answerJson) {
        Map<String, Object> dataMap = AnswerMapJSONUntil.parseDataAsObject(answerJson);
        if (dataMap == null || dataMap.isEmpty()) {
            return new DataCollectionAnswerMaps(null, null);
        }
        Map<String, String> fillBlankAnswers = convertToStringMap(dataMap.get("fillBlankAnswers"));
        Map<String, String> tableCellAnswers = convertToStringMap(dataMap.get("tableCellAnswers"));
        return new DataCollectionAnswerMaps(fillBlankAnswers, tableCellAnswers);
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> convertToStringMap(Object value) {
        if (!(value instanceof Map<?, ?> rawMap) || rawMap.isEmpty()) {
            return null;
        }
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                result.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
            }
        }
        return result.isEmpty() ? null : result;
    }

    @Data
    @AllArgsConstructor
    public static class AutoGradeExecutionResult {
        private boolean success;
        private boolean skipped;
        private String message;

        public static AutoGradeExecutionResult success(String message) {
            return new AutoGradeExecutionResult(true, false, message);
        }

        public static AutoGradeExecutionResult skipped(String message) {
            return new AutoGradeExecutionResult(false, true, message);
        }

        public static AutoGradeExecutionResult failed(String message) {
            return new AutoGradeExecutionResult(false, false, message);
        }
    }

    @Data
    @AllArgsConstructor
    private static class DataCollectionAnswerMaps {
        private Map<String, String> fillBlankAnswers;
        private Map<String, String> tableCellAnswers;

        private boolean isEmpty() {
            return (fillBlankAnswers == null || fillBlankAnswers.isEmpty())
                    && (tableCellAnswers == null || tableCellAnswers.isEmpty());
        }
    }

    /**
     * 删除附件文件
     *
     * @param attachmentId     附件ID
     * @param studentUsername  学生用户名
     * @param classCode        班级编号
     * @param procedureId      步骤ID
     */
    private void deleteAttachment(Long attachmentId, String studentUsername,
                                  String classCode, Long procedureId) {
        // 1. 查询附件记录
        StudentProcedureAttachment attachment = baseMapper.selectById(attachmentId);
        if (attachment == null) {
            throw new BusinessException(404, "附件不存在");
        }

        // 2. 验证附件归属
        if (!attachment.getStudentUsername().equals(studentUsername) ||
            !attachment.getClassCode().equals(classCode) ||
            !attachment.getProcedureId().equals(procedureId)) {
            throw new BusinessException(403, "无权删除该附件");
        }

        // 3. 删除物理文件
        try {
            String fullPath = uploadBasePath + attachment.getFilePath();
            Files.deleteIfExists(Paths.get(fullPath));
        } catch (IOException e) {
            log.error("删除附件文件失败: {}", attachment.getFilePath(), e);
        }

        // 4. 删除数据库记录
        boolean deleted = removeById(attachmentId);
        if (!deleted) {
            throw new BusinessException(500, "删除附件信息失败");
        }
    }

    /**
     * 完成限时答题（类型5）
     *
     * @param studentUsername 学生用户名
     * @param classCode       班级编号
     * @param request         提交限时答题请求
     */
    @Transactional(rollbackFor = Exception.class)
    public void completeTimedQuizProcedure(String studentUsername, String classCode,
                                           CompleteTimedQuizProcedureRequest request) {
        log.info("学生 {} 在班级 {} 提交限时答题，步骤ID：{}", studentUsername, classCode, request.getProcedureId());

        // 1. 查询并验证步骤信息
        ExperimentalProcedure procedure = experimentalProcedureService.getById(request.getProcedureId());
        if (procedure == null || procedure.getIsDeleted()) {
            throw new BusinessException(404, "实验步骤不存在");
        }

        if (!Integer.valueOf(5).equals(procedure.getType())) {
            throw new BusinessException(400, "该步骤不是限时答题类型");
        }

        // 2. 查询限时答题配置
        TimedQuizProcedure timedQuiz = timedQuizProcedureMapper.selectById(procedure.getTimedQuizId());
        if (timedQuiz == null) {
            throw new BusinessException(404, "限时答题配置不存在");
        }

        // 3. 验证密钥（不依赖数据库）
        TimedQuizKeyGenerator.KeyValidationResult validationResult =
                timedQuizKeyGenerator.validateKey(request.getSecretKey(), studentUsername, timedQuiz.getQuizTimeLimit());

        if (!validationResult.isValid()) {
            throw new BusinessException(400, validationResult.getMessage());
        }

        // 4. 检查是否已提交（直接查询学生答案表）
        LambdaQueryWrapper<StudentExperimentalProcedure> existingWrapper = new LambdaQueryWrapper<>();
        existingWrapper.eq(StudentExperimentalProcedure::getExperimentalProcedureId, request.getProcedureId())
                .eq(StudentExperimentalProcedure::getStudentUsername, studentUsername)
                .eq(StudentExperimentalProcedure::getClassCode, classCode);
        StudentExperimentalProcedure existing = studentExperimentalProcedureService.getOne(existingWrapper);

        if (existing != null) {
            throw new BusinessException(400, "该答题已提交，不可重复提交");
        }

        // 5. 验证答案数量
        if (!Boolean.TRUE.equals(timedQuiz.getIsRandom())) {
            // 老师选定模式：验证题目ID是否在步骤的题目列表中
            LambdaQueryWrapper<ProcedureTopicMap> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ProcedureTopicMap::getExperimentalProcedureId, request.getProcedureId());
            List<ProcedureTopicMap> topicMaps = procedureTopicMapMapper.selectList(queryWrapper);
            long topicCount = topicMaps.size();

            if (request.getAnswers().size() != topicCount) {
                throw new BusinessException(400,
                    String.format("应提交%d道题目，实际提交%d道", topicCount, request.getAnswers().size()));
            }

            validateSubmittedTopicIds(
                request.getAnswers().stream()
                    .map(com.example.demo.pojo.dto.mapvo.TopicAnswerItem::getTopicId)
                    .collect(java.util.stream.Collectors.toSet()),
                topicMaps.stream().map(ProcedureTopicMap::getTopicId).toList()
            );
        } else {
            // 随机模式：验证题目数量
            if (request.getAnswers().size() != timedQuiz.getTopicNumber()) {
                throw new BusinessException(400,
                    String.format("应提交%d道题目，实际提交%d道", timedQuiz.getTopicNumber(), request.getAnswers().size()));
            }
        }

        Map<Long, String> normalizedAnswers;
        try {
            normalizedAnswers = TopicAnswerContractUtil.normalizeAnswerMapForWrite(
                loadTopicsByIds(request.getAnswers().stream()
                    .map(com.example.demo.pojo.dto.mapvo.TopicAnswerItem::getTopicId)
                    .toList()),
                request.getAnswers().stream().collect(java.util.stream.Collectors.toMap(
                    com.example.demo.pojo.dto.mapvo.TopicAnswerItem::getTopicId,
                    com.example.demo.pojo.dto.mapvo.TopicAnswerItem::getAnswer
                )));
        } catch (IllegalArgumentException e) {
            throw new BusinessException(400, e.getMessage());
        }

        // 7. 创建学生步骤答案记录（设置 isLocked = true）
        StudentExperimentalProcedure studentProcedure = new StudentExperimentalProcedure();
        studentProcedure.setExperimentId(procedure.getExperimentId());
        studentProcedure.setStudentUsername(studentUsername);
        studentProcedure.setClassCode(classCode);
        studentProcedure.setExperimentalProcedureId(request.getProcedureId());
        studentProcedure.setNumber(procedure.getNumber());
        studentProcedure.setAnswer(AnswerMapJSONUntil.toTimedQuizJson(normalizedAnswers));
        studentProcedure.setIsLocked(true);  // 锁定答案，不允许修改
        studentProcedure.setCreatedTime(LocalDateTime.now());
        getClassExperimentId(classCode,procedure,studentProcedure,classExperimentClassRelationMapper,classExperimentMapper);

        boolean saved = studentExperimentalProcedureService.save(studentProcedure);
        if (!saved) {
            throw new BusinessException(500, "提交限时答题失败");
        }

        log.info("学生 {} 在班级 {} 完成限时答题，步骤：{}，题目数：{}",
                studentUsername, classCode, request.getProcedureId(), normalizedAnswers.size());
    }

    private Map<Long, String> normalizeTopicAnswers(Map<Long, String> answers) {
        try {
            return TopicAnswerContractUtil.normalizeAnswerMapForWrite(
                loadTopicsByIds(answers.keySet()),
                answers
            );
        } catch (IllegalArgumentException e) {
            throw new BusinessException(400, e.getMessage());
        }
    }

    private List<Topic> loadTopicsByIds(java.util.Collection<Long> topicIds) {
        if (topicIds == null || topicIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return topicMapper.selectList(new LambdaQueryWrapper<Topic>().in(Topic::getId, topicIds));
    }

    private void validateSubmittedTopicIds(java.util.Set<Long> submittedTopicIds, List<Long> allowedTopicIds) {
        java.util.Set<Long> allowedSet = new java.util.HashSet<>(allowedTopicIds);
        if (!allowedSet.equals(submittedTopicIds)) {
            throw new BusinessException(400, "提交的题目ID与当前步骤不匹配");
        }
    }
}
