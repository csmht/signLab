package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.*;
import com.example.demo.pojo.entity.*;
import com.example.demo.pojo.response.StudentProcedureDetailWithAnswerResponse;
import com.example.demo.pojo.response.StudentProcedureDetailWithoutAnswerResponse;
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

        // 判断是否已过答题时间
        boolean isAfterEndTime = LocalDateTime.now().isAfter(procedure.getEndTime());

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
                    item.setChoices(topic.getChoices());

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
                    item.setChoices(topic.getChoices());
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
                    QueryWrapper<TopicTagMap> tagWrapper = new QueryWrapper<>();
                    tagWrapper.in("tag_id", tagIdList);
                    List<TopicTagMap> topicTagMaps = topicTagMapMapper.selectList(tagWrapper);

                    if (!topicTagMaps.isEmpty()) {
                        List<Long> topicIds = topicTagMaps.stream()
                            .map(TopicTagMap::getTopicId)
                            .distinct()
                            .collect(Collectors.toList());

                        if (!topicIds.isEmpty()) {
                            QueryWrapper<Topic> topicWrapper = new QueryWrapper<>();
                            topicWrapper.in("id", topicIds);
                            topicWrapper.orderByAsc("number");
                            return topicMapper.selectList(topicWrapper);
                        }
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
}
