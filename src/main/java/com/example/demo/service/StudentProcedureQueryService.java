package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.*;
import com.example.demo.pojo.entity.*;
import com.example.demo.pojo.response.StudentProcedureDetailWithAnswerResponse;
import com.example.demo.pojo.response.StudentProcedureDetailWithoutAnswerResponse;
import com.example.demo.util.ProcedureTimeCalculator;
import com.example.demo.util.TimedQuizKeyGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 学生步骤查询服务
 * 提供学生查询已提交和未提交步骤详情的功能
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class StudentProcedureQueryService {

    private final ExperimentalProcedureMapper experimentalProcedureMapper;
    private final StudentExperimentalProcedureMapper studentExperimentalProcedureMapper;
    private final StudentProcedureAttachmentMapper studentProcedureAttachmentMapper;
    private final VideoFileMapper videoFileMapper;
    private final DataCollectionMapper dataCollectionMapper;
    private final ProcedureTopicMapper procedureTopicMapper;
    private final TopicMapper topicMapper;
    private final TopicTagMapMapper topicTagMapMapper;
    private final ProcedureTopicMapMapper procedureTopicMapMapper;
    private final DownloadService downloadService;
    private final ClassExperimentMapper classExperimentMapper;
    private final ClassExperimentClassRelationService classExperimentClassRelationService;
    private final TimedQuizProcedureMapper timedQuizProcedureMapper;
    private final TimedQuizKeyGenerator timedQuizKeyGenerator;

    /**
     * 查询已提交的步骤详情（带答案）
     *
     * @param courseId   课程ID
     * @param experimentId 实验ID
     * @param procedureId 步骤ID
     * @param username   用户名
     * @return 步骤详情（带答案）
     */
    public StudentProcedureDetailWithAnswerResponse getCompletedProcedureDetail(
            String courseId, Long experimentId, Long procedureId, String username) {

        // 查询步骤基本信息
        ExperimentalProcedure procedure = experimentalProcedureMapper.selectById(procedureId);
        if (procedure == null) {
            throw new BusinessException(404, "步骤不存在");
        }

        if (!procedure.getExperimentId().equals(experimentId)) {
            throw new BusinessException(400, "步骤与实验不匹配");
        }

        // 查询学生提交记录
        QueryWrapper<StudentExperimentalProcedure> wrapper = new QueryWrapper<>();
        wrapper.eq("experimental_procedure_id", procedureId);
        wrapper.eq("student_username", username);
        StudentExperimentalProcedure studentProcedure = studentExperimentalProcedureMapper.selectOne(wrapper);

        if (studentProcedure == null) {
            throw new BusinessException(404, "未找到提交记录");
        }

        // 查询班级实验并计算步骤时间
        boolean isAfterEndTime = false;
        try {
            // 查询班级实验
            QueryWrapper<com.example.demo.pojo.entity.ClassExperiment> classExperimentWrapper = new QueryWrapper<>();
            classExperimentWrapper.eq("course_id", courseId);
            classExperimentWrapper.eq("experiment_id", experimentId);
            com.example.demo.pojo.entity.ClassExperiment classExperiment = classExperimentMapper.selectOne(classExperimentWrapper);

            if (classExperiment != null && procedure.getOffsetMinutes() != null) {
                // 使用工具类计算步骤时间
                LocalDateTime endTime = ProcedureTimeCalculator.calculateEndTime(
                        ProcedureTimeCalculator.calculateStartTime(
                                classExperiment.getStartTime(),
                                procedure.getOffsetMinutes()
                        ),
                        procedure.getDurationMinutes()
                );

                if (endTime != null) {
                    isAfterEndTime = LocalDateTime.now().isAfter(endTime);
                }
            }
        } catch (Exception e) {
            log.error("计算步骤时间失败", e);
            // 如果计算失败,默认不允许查看答案
            isAfterEndTime = false;
        }

        StudentProcedureDetailWithAnswerResponse response = new StudentProcedureDetailWithAnswerResponse();
        response.setId(procedure.getId());
        response.setNumber(procedure.getNumber());
        response.setType(procedure.getType());
        response.setRemark(procedure.getRemark());
        response.setProportion(procedure.getProportion());
        response.setSubmissionTime(studentProcedure.getCreatedTime());
        response.setScore(studentProcedure.getScore());
        response.setTeacherComment(studentProcedure.getTeacherComment());
        response.setIsAfterEndTime(isAfterEndTime);

        // 根据步骤类型填充详细信息
        fillCompletedProcedureDetailByType(response, procedure, studentProcedure, username, isAfterEndTime);

        return response;
    }

    /**
     * 查询未提交的步骤详情
     *
     * @param courseId   课程ID
     * @param experimentId 实验ID
     * @param procedureId 步骤ID
     * @param username   用户名
     * @return 步骤详情（不含答案）
     */
    public StudentProcedureDetailWithoutAnswerResponse getUncompletedProcedureDetail(
            String courseId, Long experimentId, Long procedureId, String username) {

        // 查询步骤基本信息
        ExperimentalProcedure procedure = experimentalProcedureMapper.selectById(procedureId);
        if (procedure == null) {
            throw new BusinessException(404, "步骤不存在");
        }

        if (!procedure.getExperimentId().equals(experimentId)) {
            throw new BusinessException(400, "步骤与实验不匹配");
        }

        // 检查是否已提交
        QueryWrapper<StudentExperimentalProcedure> wrapper = new QueryWrapper<>();
        wrapper.eq("experimental_procedure_id", procedureId);
        wrapper.eq("student_username", username);
        StudentExperimentalProcedure studentProcedure = studentExperimentalProcedureMapper.selectOne(wrapper);

        if (studentProcedure != null) {
            throw new BusinessException(400, "步骤已提交，请使用已提交接口查询");
        }

        StudentProcedureDetailWithoutAnswerResponse response = new StudentProcedureDetailWithoutAnswerResponse();
        response.setId(procedure.getId());
        response.setNumber(procedure.getNumber());
        response.setType(procedure.getType());
        response.setRemark(procedure.getRemark());
        response.setProportion(procedure.getProportion());

        // 根据步骤类型填充详细信息
        fillUncompletedProcedureDetailByType(response, procedure, username);

        return response;
    }

    /**
     * 根据步骤类型填充已提交步骤的详细信息
     */
    private void fillCompletedProcedureDetailByType(
            StudentProcedureDetailWithAnswerResponse response,
            ExperimentalProcedure procedure,
            StudentExperimentalProcedure studentProcedure,
            String username,
            boolean isAfterEndTime) {

        int type = procedure.getType();

        if (type == 1) {
            // 观看视频
            fillVideoDetailForCompleted(response, procedure);
        } else if (type == 2) {
            // 数据收集
            fillDataCollectionDetailForCompleted(response, procedure, username, isAfterEndTime);
        } else if (type == 3) {
            // 题库答题
            fillTopicDetailForCompleted(response, procedure, studentProcedure, isAfterEndTime);
        } else if (type == 5) {
            // 限时答题
            fillTimedQuizDetailForCompleted(response, procedure, studentProcedure, isAfterEndTime);
        }
    }

    /**
     * 根据步骤类型填充未提交步骤的详细信息
     */
    private void fillUncompletedProcedureDetailByType(
            StudentProcedureDetailWithoutAnswerResponse response,
            ExperimentalProcedure procedure,
            String username) {

        int type = procedure.getType();

        if (type == 1) {
            // 观看视频
            fillVideoDetailForUncompleted(response, procedure);
        } else if (type == 2) {
            // 数据收集
            fillDataCollectionDetailForUncompleted(response, procedure);
        } else if (type == 3) {
            // 题库答题
            fillTopicDetailForUncompleted(response, procedure);
        } else if (type == 5) {
            // 限时答题
            fillTimedQuizDetailForUncompleted(response, procedure, username);
        }
    }

    /**
     * 填充视频详情（已提交）
     */
    private void fillVideoDetailForCompleted(
            StudentProcedureDetailWithAnswerResponse response,
            ExperimentalProcedure procedure) {

        if (procedure.getVideoId() != null) {
            VideoFile videoFile = videoFileMapper.selectById(procedure.getVideoId());
            if (videoFile != null) {
                StudentProcedureDetailWithAnswerResponse.VideoDetail detail =
                    new StudentProcedureDetailWithAnswerResponse.VideoDetail();
                detail.setId(videoFile.getId());
                detail.setTitle(videoFile.getOriginalFileName());
                detail.setSeconds(videoFile.getVideoSeconds());
                detail.setFilePath(videoFile.getFilePath());
                detail.setFileSize(videoFile.getFileSize());
                response.setVideoDetail(detail);
            }
        }
    }

    /**
     * 填充视频详情（未提交）
     */
    private void fillVideoDetailForUncompleted(
            StudentProcedureDetailWithoutAnswerResponse response,
            ExperimentalProcedure procedure) {

        if (procedure.getVideoId() != null) {
            VideoFile videoFile = videoFileMapper.selectById(procedure.getVideoId());
            if (videoFile != null) {
                StudentProcedureDetailWithoutAnswerResponse.VideoDetail detail =
                    new StudentProcedureDetailWithoutAnswerResponse.VideoDetail();
                detail.setId(videoFile.getId());
                detail.setTitle(videoFile.getOriginalFileName());
                detail.setSeconds(videoFile.getVideoSeconds());
                // 生成播放密钥
                String playKey = downloadService.generatePlayKey(videoFile.getId(),
                    com.example.demo.util.SecurityUtil.getCurrentUsername().orElse("unknown"));
                detail.setPlayKey(playKey);
                response.setVideoDetail(detail);
            }
        }
    }

    /**
     * 填充数据收集详情（已提交）
     */
    private void fillDataCollectionDetailForCompleted(
            StudentProcedureDetailWithAnswerResponse response,
            ExperimentalProcedure procedure,
            String username,
            boolean isAfterEndTime) {

        if (procedure.getDataCollectionId() != null) {
            DataCollection dataCollection = dataCollectionMapper.selectById(procedure.getDataCollectionId());
            if (dataCollection != null) {
                StudentProcedureDetailWithAnswerResponse.DataCollectionDetail detail =
                    new StudentProcedureDetailWithAnswerResponse.DataCollectionDetail();
                detail.setId(dataCollection.getId());
                detail.setType(dataCollection.getType() != null ? dataCollection.getType().intValue() : null);
                detail.setRemark(dataCollection.getRemark());
                detail.setNeedPhoto(dataCollection.getNeedPhoto());
                detail.setNeedDoc(dataCollection.getNeedDoc());

                // 查询附件信息
                QueryWrapper<StudentProcedureAttachment> attachmentWrapper = new QueryWrapper<>();
                attachmentWrapper.eq("procedure_id", procedure.getId());
                attachmentWrapper.eq("student_username", username);
                List<StudentProcedureAttachment> attachments = studentProcedureAttachmentMapper.selectList(attachmentWrapper);

                List<StudentProcedureDetailWithAnswerResponse.AttachmentInfo> photos = new ArrayList<>();
                List<StudentProcedureDetailWithAnswerResponse.AttachmentInfo> documents = new ArrayList<>();

                for (StudentProcedureAttachment attachment : attachments) {
                    StudentProcedureDetailWithAnswerResponse.AttachmentInfo info =
                        new StudentProcedureDetailWithAnswerResponse.AttachmentInfo();
                    info.setId(attachment.getId());
                    info.setFileType(attachment.getFileType());
                    info.setFileFormat(attachment.getFileFormat());
                    info.setOriginalFileName(attachment.getOriginalFileName());
                    info.setFileSize(attachment.getFileSize());
                    info.setRemark(attachment.getRemark());
                    info.setCreateTime(attachment.getCreateTime());

                    // 生成文件下载密钥
                    String downloadKey = downloadService.generateFileKey(
                        DownloadService.TYPE_ATTACHMENT, attachment.getId(), username);
                    info.setDownloadKey(downloadKey);

                    if (attachment.getFileType() == 1) {
                        photos.add(info);
                    } else if (attachment.getFileType() == 2) {
                        documents.add(info);
                    }
                }

                detail.setPhotos(photos);
                detail.setDocuments(documents);

                // 解析答案JSON
                QueryWrapper<StudentExperimentalProcedure> spWrapper = new QueryWrapper<>();
                spWrapper.eq("experimental_procedure_id", procedure.getId());
                spWrapper.eq("student_username", username);
                StudentExperimentalProcedure studentProcedure = studentExperimentalProcedureMapper.selectOne(spWrapper);

                if (studentProcedure != null) {
                    String answer = studentProcedure.getAnswer();
                    if (answer != null && !answer.isEmpty()) {
                        try {
                            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                            Map<String, Object> answerMap = mapper.readValue(answer,
                                new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});

                            @SuppressWarnings("unchecked")
                            Map<String, String> fillBlankAnswers = (Map<String, String>) answerMap.get("fillBlankAnswers");
                            @SuppressWarnings("unchecked")
                            Map<String, String> tableCellAnswers = (Map<String, String>) answerMap.get("tableCellAnswers");

                            detail.setFillBlankAnswers(fillBlankAnswers);
                            detail.setTableCellAnswers(tableCellAnswers);
                        } catch (Exception e) {
                            log.error("解析数据收集答案失败", e);
                        }
                    }

                    // 如果已过答题时间，返回正确答案
                    if (isAfterEndTime && dataCollection.getCorrectAnswer() != null) {
                        detail.setCorrectAnswer(dataCollection.getCorrectAnswer());
                    }
                }

                response.setDataCollectionDetail(detail);
            }
        }
    }

    /**
     * 填充数据收集详情（未提交）
     */
    private void fillDataCollectionDetailForUncompleted(
            StudentProcedureDetailWithoutAnswerResponse response,
            ExperimentalProcedure procedure) {

        if (procedure.getDataCollectionId() != null) {
            DataCollection dataCollection = dataCollectionMapper.selectById(procedure.getDataCollectionId());
            if (dataCollection != null) {
                StudentProcedureDetailWithoutAnswerResponse.DataCollectionDetail detail =
                    new StudentProcedureDetailWithoutAnswerResponse.DataCollectionDetail();
                detail.setId(dataCollection.getId());
                detail.setType(dataCollection.getType() != null ? dataCollection.getType().intValue() : null);
                detail.setRemark(dataCollection.getRemark());
                detail.setNeedPhoto(dataCollection.getNeedPhoto());
                detail.setNeedDoc(dataCollection.getNeedDoc());
                response.setDataCollectionDetail(detail);
            }
        }
    }

    /**
     * 填充题库详情（已提交）
     */
    private void fillTopicDetailForCompleted(
            StudentProcedureDetailWithAnswerResponse response,
            ExperimentalProcedure procedure,
            StudentExperimentalProcedure studentProcedure,
            boolean isAfterEndTime) {

        if (procedure.getProcedureTopicId() != null) {
            ProcedureTopic procedureTopic = procedureTopicMapper.selectById(procedure.getProcedureTopicId());
            if (procedureTopic != null) {
                StudentProcedureDetailWithAnswerResponse.TopicDetail detail =
                    new StudentProcedureDetailWithAnswerResponse.TopicDetail();
                detail.setId(procedureTopic.getId());
                detail.setIsRandom(procedureTopic.getIsRandom());
                detail.setNumber(procedureTopic.getNumber());
                detail.setTags(procedureTopic.getTags());

                // 查询题目列表
                List<Topic> topics = getTopicsForProcedure(procedureTopic);
                List<StudentProcedureDetailWithAnswerResponse.TopicItem> topicItems = new ArrayList<>();

                // 解析学生答案
                Map<Long, String> studentAnswers = parseTopicAnswers(studentProcedure.getAnswer());

                for (Topic topic : topics) {
                    StudentProcedureDetailWithAnswerResponse.TopicItem item =
                        new StudentProcedureDetailWithAnswerResponse.TopicItem();
                    item.setId(topic.getId());
                    item.setNumber(topic.getNumber());
                    item.setType(topic.getType());
                    item.setContent(topic.getContent());
                    item.setChoices(parseTopicChoices(topic.getChoices()));

                    String studentAnswer = studentAnswers.get(topic.getId());
                    item.setStudentAnswer(studentAnswer);

                    // 如果已过答题时间，才返回正确答案和是否正确
                    if (isAfterEndTime) {
                        item.setCorrectAnswer(topic.getCorrectAnswer());

                        if (studentAnswer != null && studentAnswer.equals(topic.getCorrectAnswer())) {
                            item.setIsCorrect(true);
                        } else {
                            item.setIsCorrect(false);
                        }
                    }

                    topicItems.add(item);
                }

                detail.setTopics(topicItems);
                response.setTopicDetail(detail);
            }
        }
    }

    /**
     * 填充题库详情（未提交）
     */
    private void fillTopicDetailForUncompleted(
            StudentProcedureDetailWithoutAnswerResponse response,
            ExperimentalProcedure procedure) {

        if (procedure.getProcedureTopicId() != null) {
            ProcedureTopic procedureTopic = procedureTopicMapper.selectById(procedure.getProcedureTopicId());
            if (procedureTopic != null) {
                StudentProcedureDetailWithoutAnswerResponse.TopicDetail detail =
                    new StudentProcedureDetailWithoutAnswerResponse.TopicDetail();
                detail.setId(procedureTopic.getId());
                detail.setIsRandom(procedureTopic.getIsRandom());
                detail.setNumber(procedureTopic.getNumber());
                detail.setTags(procedureTopic.getTags());

                // 查询题目列表（不含答案）
                List<Topic> topics = getTopicsForProcedure(procedureTopic);
                List<StudentProcedureDetailWithoutAnswerResponse.TopicItem> topicItems = new ArrayList<>();

                for (Topic topic : topics) {
                    StudentProcedureDetailWithoutAnswerResponse.TopicItem item =
                        new StudentProcedureDetailWithoutAnswerResponse.TopicItem();
                    item.setId(topic.getId());
                    item.setNumber(topic.getNumber());
                    item.setType(topic.getType());
                    item.setContent(topic.getContent());
                    item.setChoices(parseTopicChoices(topic.getChoices()));
                    topicItems.add(item);
                }

                detail.setTopics(topicItems);
                response.setTopicDetail(detail);
            }
        }
    }

    /**
     * 根据题库详情获取题目列表
     */
    private List<Topic> getTopicsForProcedure(ProcedureTopic procedureTopic) {
        if (procedureTopic.getIsRandom()) {
            // 随机抽取：根据标签过滤题目
            if (procedureTopic.getTags() != null && !procedureTopic.getTags().isEmpty()) {
                String[] tagIds = procedureTopic.getTags().split(",");
                List<Long> tagIdList = Arrays.stream(tagIds)
                    .filter(s -> s != null && !s.isEmpty())
                    .map(Long::parseLong)
                    .collect(Collectors.toList());

                if (!tagIdList.isEmpty()) {
                    // 查询包含所有指定标签的题目（AND逻辑）
                    // 使用GROUP BY和HAVING确保题目包含所有标签
                    String sql = "SELECT topic_id FROM topic_tag_map " +
                                "WHERE tag_id IN (" + tagIdList.stream().map(String::valueOf).collect(Collectors.joining(",")) + ") " +
                                "GROUP BY topic_id " +
                                "HAVING COUNT(DISTINCT tag_id) = " + tagIdList.size();

                    // 使用原生SQL查询满足条件的题目ID
                    List<Long> topicIds = topicTagMapMapper.selectList(
                        new QueryWrapper<TopicTagMap>().apply(sql)
                    ).stream()
                    .map(TopicTagMap::getTopicId)
                    .distinct()
                    .collect(Collectors.toList());

                    if (!topicIds.isEmpty()) {
                        QueryWrapper<Topic> topicWrapper = new QueryWrapper<>();
                        topicWrapper.in("id", topicIds);

                        // 添加题目类型过滤
                        if (procedureTopic.getTopicTypes() != null && !procedureTopic.getTopicTypes().isEmpty()) {
                            String[] typeArray = procedureTopic.getTopicTypes().split(",");
                            List<Integer> types = Arrays.stream(typeArray)
                                .filter(s -> s != null && !s.isEmpty())
                                .map(Integer::parseInt)
                                .collect(Collectors.toList());
                            if (!types.isEmpty()) {
                                topicWrapper.in("type", types);
                            }
                        }

                        // 随机排序并限制数量
                        if (procedureTopic.getNumber() != null && procedureTopic.getNumber() > 0) {
                            topicWrapper.last("ORDER BY RAND() LIMIT " + procedureTopic.getNumber());
                        } else {
                            topicWrapper.last("ORDER BY RAND()");
                        }
                        return topicMapper.selectList(topicWrapper);
                    }
                }
            }
            return new ArrayList<>();
        } else {
            // 固定题目：从题库详情映射表查询
            QueryWrapper<ProcedureTopicMap> mapWrapper = new QueryWrapper<>();
            mapWrapper.eq("procedure_topic_id", procedureTopic.getId());
            mapWrapper.orderByAsc("id");
            List<ProcedureTopicMap> topicMaps = procedureTopicMapMapper.selectList(mapWrapper);

            if (!topicMaps.isEmpty()) {
                List<Long> topicIds = topicMaps.stream()
                    .map(ProcedureTopicMap::getTopicId)
                    .collect(Collectors.toList());

                QueryWrapper<Topic> topicWrapper = new QueryWrapper<>();
                topicWrapper.in("id", topicIds);
                topicWrapper.orderByAsc("number");
                return topicMapper.selectList(topicWrapper);
            }
            return new ArrayList<>();
        }
    }

    /**
     * 解析题库答案JSON
     */
    private Map<Long, String> parseTopicAnswers(String answerJson) {
        if (answerJson == null || answerJson.isEmpty()) {
            return new HashMap<>();
        }

        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readValue(answerJson,
                new com.fasterxml.jackson.core.type.TypeReference<Map<Long, String>>() {});
        } catch (Exception e) {
            log.error("解析题库答案失败", e);
            return new HashMap<>();
        }
    }

    /**
     * 解析题目选项字符串为Map
     * 格式：A:选项A内容$B:选项B内容$C:选项C内容$D:选项D内容
     * @param choices 选项字符串
     * @return 选项Map，key为选项字母(A、B、C、D)，value为选项内容
     */
    private Map<String, String> parseTopicChoices(String choices) {
        if (choices == null || choices.isEmpty()) {
            return new HashMap<>();
        }

        Map<String, String> choicesMap = new HashMap<>();
        try {
            // 按 $ 分割各个选项
            String[] options = choices.split("\\$");
            for (String option : options) {
                // 每个选项格式为 "A:选项内容"
                if (option.contains(":")) {
                    String[] parts = option.split(":", 2);
                    if (parts.length == 2) {
                        String key = parts[0].trim();
                        String value = parts[1].trim();
                        choicesMap.put(key, value);
                    }
                }
            }
        } catch (Exception e) {
            log.error("解析题目选项失败, choices: {}", choices, e);
            return new HashMap<>();
        }

        return choicesMap;
    }

    /**
     * 填充限时答题详情（已提交）
     */
    private void fillTimedQuizDetailForCompleted(
            StudentProcedureDetailWithAnswerResponse response,
            ExperimentalProcedure procedure,
            StudentExperimentalProcedure studentProcedure,
            boolean isAfterEndTime) {

        if (procedure.getTimedQuizId() != null) {
            TimedQuizProcedure timedQuiz = timedQuizProcedureMapper.selectById(procedure.getTimedQuizId());
            if (timedQuiz != null) {
                StudentProcedureDetailWithAnswerResponse.TimedQuizDetail detail =
                    new StudentProcedureDetailWithAnswerResponse.TimedQuizDetail();
                detail.setId(timedQuiz.getId());
                detail.setIsRandom(timedQuiz.getIsRandom());
                detail.setNumber(timedQuiz.getTopicNumber());
                detail.setQuizTimeLimit(timedQuiz.getQuizTimeLimit());
                detail.setIsLocked(studentProcedure.getIsLocked());

                // 查询题目列表
                List<Topic> topics = getTopicsForTimedQuiz(procedure, timedQuiz);
                List<StudentProcedureDetailWithAnswerResponse.TopicItem> topicItems = new ArrayList<>();

                // 解析学生答案
                Map<Long, String> studentAnswers = parseTopicAnswers(studentProcedure.getAnswer());

                for (Topic topic : topics) {
                    StudentProcedureDetailWithAnswerResponse.TopicItem item =
                        new StudentProcedureDetailWithAnswerResponse.TopicItem();
                    item.setId(topic.getId());
                    item.setNumber(topic.getNumber());
                    item.setType(topic.getType());
                    item.setContent(topic.getContent());
                    item.setChoices(parseTopicChoices(topic.getChoices()));

                    String studentAnswer = studentAnswers.get(topic.getId());
                    item.setStudentAnswer(studentAnswer);

                    // 如果已过答题时间，才返回正确答案和是否正确
                    if (isAfterEndTime) {
                        item.setCorrectAnswer(topic.getCorrectAnswer());

                        if (studentAnswer != null && studentAnswer.equals(topic.getCorrectAnswer())) {
                            item.setIsCorrect(true);
                        } else {
                            item.setIsCorrect(false);
                        }
                    }

                    topicItems.add(item);
                }

                detail.setTopics(topicItems);
                response.setTimedQuizDetail(detail);
            }
        }
    }

    /**
     * 填充限时答题详情（未提交）
     */
    private void fillTimedQuizDetailForUncompleted(
            StudentProcedureDetailWithoutAnswerResponse response,
            ExperimentalProcedure procedure,
            String username) {

        if (procedure.getTimedQuizId() != null) {
            TimedQuizProcedure timedQuiz = timedQuizProcedureMapper.selectById(procedure.getTimedQuizId());
            if (timedQuiz != null) {
                StudentProcedureDetailWithoutAnswerResponse.TimedQuizDetail detail =
                    new StudentProcedureDetailWithoutAnswerResponse.TimedQuizDetail();
                detail.setId(timedQuiz.getId());
                detail.setIsRandom(timedQuiz.getIsRandom());
                detail.setNumber(timedQuiz.getTopicNumber());
                detail.setQuizTimeLimit(timedQuiz.getQuizTimeLimit());

                // 查询题目列表（不含答案）
                List<Topic> topics = getTopicsForTimedQuiz(procedure, timedQuiz);
                List<StudentProcedureDetailWithoutAnswerResponse.TopicItem> topicItems = new ArrayList<>();

                for (Topic topic : topics) {
                    StudentProcedureDetailWithoutAnswerResponse.TopicItem item =
                        new StudentProcedureDetailWithoutAnswerResponse.TopicItem();
                    item.setId(topic.getId());
                    item.setNumber(topic.getNumber());
                    item.setType(topic.getType());
                    item.setContent(topic.getContent());
                    item.setChoices(parseTopicChoices(topic.getChoices()));
                    topicItems.add(item);
                }

                detail.setTopics(topicItems);

                // 生成密钥
                String secretKey = timedQuizKeyGenerator.generateKey(username);
                detail.setSecretKey(secretKey);

                // 设置剩余时间为完整时间
                detail.setRemainingTime((long) timedQuiz.getQuizTimeLimit() * 60);

                response.setTimedQuizDetail(detail);
            }
        }
    }

    /**
     * 获取限时答题的题目列表
     */
    private List<Topic> getTopicsForTimedQuiz(ExperimentalProcedure procedure, TimedQuizProcedure timedQuiz) {
        if (Boolean.TRUE.equals(timedQuiz.getIsRandom())) {
            // 随机模式：从题库中随机抽取
            return getRandomTopicsForTimedQuiz(timedQuiz);
        } else {
            // 老师选定模式：查询映射的题目
            QueryWrapper<ProcedureTopicMap> topicMapQueryWrapper = new QueryWrapper<>();
            topicMapQueryWrapper.eq("experimental_procedure_id", procedure.getId());
            List<ProcedureTopicMap> topicMaps = procedureTopicMapMapper.selectList(topicMapQueryWrapper);

            if (topicMaps == null || topicMaps.isEmpty()) {
                return new ArrayList<>();
            }

            List<Long> topicIds = topicMaps.stream()
                    .map(ProcedureTopicMap::getTopicId)
                    .collect(Collectors.toList());

            QueryWrapper<Topic> topicQueryWrapper = new QueryWrapper<>();
            topicQueryWrapper.in("id", topicIds)
                    .eq("is_deleted", false)
                    .orderByAsc("number");
            return topicMapper.selectList(topicQueryWrapper);
        }
    }

    /**
     * 随机抽取题目（限时答题专用）
     */
    private List<Topic> getRandomTopicsForTimedQuiz(TimedQuizProcedure timedQuiz) {
        // 根据标签过滤题目
        if (timedQuiz.getTopicTags() != null && !timedQuiz.getTopicTags().isEmpty()) {
            String[] tagIds = timedQuiz.getTopicTags().split(",");
            List<Long> tagIdList = Arrays.stream(tagIds)
                .filter(s -> s != null && !s.isEmpty())
                .map(Long::parseLong)
                .collect(Collectors.toList());

            if (!tagIdList.isEmpty()) {
                // 查询包含所有指定标签的题目（AND逻辑）
                String sql = "SELECT topic_id FROM topic_tag_map " +
                            "WHERE tag_id IN (" + tagIdList.stream().map(String::valueOf).collect(Collectors.joining(",")) + ") " +
                            "GROUP BY topic_id " +
                            "HAVING COUNT(DISTINCT tag_id) = " + tagIdList.size();

                List<Long> topicIds = topicTagMapMapper.selectList(
                    new QueryWrapper<TopicTagMap>().apply(sql)
                ).stream()
                .map(TopicTagMap::getTopicId)
                .distinct()
                .collect(Collectors.toList());

                if (!topicIds.isEmpty()) {
                    QueryWrapper<Topic> topicWrapper = new QueryWrapper<>();
                    topicWrapper.in("id", topicIds);

                    // 添加题目类型过滤
                    if (timedQuiz.getTopicTypes() != null && !timedQuiz.getTopicTypes().isEmpty()) {
                        String[] typeArray = timedQuiz.getTopicTypes().split(",");
                        List<Integer> types = Arrays.stream(typeArray)
                            .filter(s -> s != null && !s.isEmpty())
                            .map(Integer::parseInt)
                            .collect(Collectors.toList());
                        if (!types.isEmpty()) {
                            topicWrapper.in("type", types);
                        }
                    }

                    // 随机排序并限制数量
                    if (timedQuiz.getTopicNumber() != null && timedQuiz.getTopicNumber() > 0) {
                        topicWrapper.last("ORDER BY RAND() LIMIT " + timedQuiz.getTopicNumber());
                    } else {
                        topicWrapper.last("ORDER BY RAND()");
                    }
                    return topicMapper.selectList(topicWrapper);
                }
            }
        }
        return new ArrayList<>();
    }
}
