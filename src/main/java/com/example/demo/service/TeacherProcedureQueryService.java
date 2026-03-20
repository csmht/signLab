package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demo.mapper.ClassExperimentMapper;
import com.example.demo.mapper.DataCollectionMapper;
import com.example.demo.mapper.ProcedureTopicMapMapper;
import com.example.demo.mapper.ProcedureTopicMapper;
import com.example.demo.mapper.TopicMapper;
import com.example.demo.mapper.VideoFileMapper;
import com.example.demo.mapper.TagMapper;
import com.example.demo.mapper.TimedQuizProcedureMapper;
import com.example.demo.mapper.TopicTagMapMapper;
import com.example.demo.pojo.entity.ClassExperiment;
import com.example.demo.pojo.entity.DataCollection;
import com.example.demo.pojo.entity.ExperimentalProcedure;
import com.example.demo.pojo.entity.ProcedureTopic;
import com.example.demo.pojo.entity.ProcedureTopicMap;
import com.example.demo.pojo.entity.Topic;
import com.example.demo.pojo.entity.VideoFile;
import com.example.demo.pojo.entity.Tag;
import com.example.demo.pojo.entity.TimedQuizProcedure;
import com.example.demo.pojo.entity.TopicTagMap;
import com.example.demo.pojo.response.TeacherDataCollectionProcedureDetailResponse;
import com.example.demo.pojo.response.TeacherProcedureDetailResponse;
import com.example.demo.pojo.response.TeacherTopicProcedureDetailResponse;
import com.example.demo.pojo.response.TeacherVideoProcedureDetailResponse;
import com.example.demo.util.ProcedureTimeCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 教师查询步骤详情服务
 * 提供教师查询实验步骤详情的业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TeacherProcedureQueryService {

    private final ExperimentalProcedureService experimentalProcedureService;
    private final VideoFileMapper videoFileMapper;
    private final DataCollectionMapper dataCollectionMapper;
    private final ProcedureTopicMapper procedureTopicMapper;
    private final ProcedureTopicMapMapper procedureTopicMapMapper;
    private final TopicMapper topicMapper;
    private final ClassExperimentMapper classExperimentMapper;
    private final TagMapper tagMapper;
    private final TimedQuizProcedureMapper timedQuizProcedureMapper;
    private final TopicTagMapMapper topicTagMapMapper;

    /**
     * 查询步骤详情(包含类型特定的完整信息)
     *
     * @param procedureId       步骤ID
     * @return 步骤详情
     */
    public TeacherProcedureDetailResponse getProcedureDetail(Long procedureId) {
        log.info("查询步骤详情，步骤ID: {}", procedureId);

        // 1. 查询步骤基本信息
        ExperimentalProcedure procedure = experimentalProcedureService.getById(procedureId);
        if (procedure == null) {
            throw new com.example.demo.exception.BusinessException(404, "步骤不存在");
        }

        // 2. 构建响应对象
        TeacherProcedureDetailResponse response = new TeacherProcedureDetailResponse();
        response.setId(procedure.getId());
        response.setExperimentId(procedure.getExperimentId());
        response.setNumber(procedure.getNumber());
        response.setType(procedure.getType());
        response.setRemark(procedure.getRemark());
        response.setIsSkip(procedure.getIsSkip());
        response.setProportion(procedure.getProportion());

        // 3. 查询时间配置(如果提供了 classExperimentId)
        response.setOffsetMinutes(procedure.getOffsetMinutes());
        response.setDurationMinutes(procedure.getDurationMinutes());

        // 4. 根据步骤类型填充详细信息
        fillProcedureDetailByType(response, procedure);

        return response;
    }

    /**
     * 根据步骤类型查询实验的所有步骤详情
     *
     * @param experimentId      实验ID
     * @return 步骤详情列表
     */
    public List<TeacherProcedureDetailResponse> getExperimentProcedures(Long experimentId) {
        log.info("查询实验的所有步骤详情，实验ID: {}", experimentId);

        // 1. 查询实验的所有步骤
        List<ExperimentalProcedure> procedures = experimentalProcedureService.getByExperimentId(experimentId);

        // 3. 为每个步骤构建详情响应
        List<TeacherProcedureDetailResponse> responses = new ArrayList<>();
        for (ExperimentalProcedure procedure : procedures) {
            TeacherProcedureDetailResponse response = new TeacherProcedureDetailResponse();
            response.setId(procedure.getId());
            response.setExperimentId(procedure.getExperimentId());
            response.setNumber(procedure.getNumber());
            response.setType(procedure.getType());
            response.setRemark(procedure.getRemark());
            response.setIsSkip(procedure.getIsSkip());
            response.setProportion(procedure.getProportion());

            // 设置偏移量和持续时间
            response.setOffsetMinutes(procedure.getOffsetMinutes());
            response.setDurationMinutes(procedure.getDurationMinutes());

            // 填充类型特定的详细信息
            fillProcedureDetailByType(response, procedure);

            responses.add(response);
        }

        return responses;
    }

    /**
     * 根据步骤类型填充详细信息
     *
     * @param response  步骤详情响应
     * @param procedure 步骤信息
     */
    private void fillProcedureDetailByType(TeacherProcedureDetailResponse response, ExperimentalProcedure procedure) {
        Integer type = procedure.getType();
        if (type == null) {
            return;
        }

        switch (type) {
            case 1:
                // 类型1：观看视频
                fillVideoDetail(response, procedure);
                break;
            case 2:
                // 类型2：数据收集
                fillDataCollectionDetail(response, procedure);
                break;
            case 3:
                // 类型3：题库答题
                fillTopicDetail(response, procedure);
                break;
            case 5:
                // 类型5：限时答题
                fillTimedQuizDetail(response, procedure);
                break;
            default:
                break;
        }
    }

    /**
     * 填充视频详情（类型1）
     */
    private void fillVideoDetail(TeacherProcedureDetailResponse response, ExperimentalProcedure procedure) {
        if (procedure.getVideoId() == null) {
            return;
        }

        VideoFile videoFile = videoFileMapper.selectById(procedure.getVideoId());
        if (videoFile != null) {
            response.setVideoId(videoFile.getId());
            response.setVideoTitle(videoFile.getOriginalFileName());
            response.setVideoSeconds(videoFile.getVideoSeconds());
            response.setVideoFilePath(videoFile.getFilePath());
            response.setVideoFileSize(videoFile.getFileSize());
        }
    }

    /**
     * 填充数据收集详情（类型2）
     */
    private void fillDataCollectionDetail(TeacherProcedureDetailResponse response, ExperimentalProcedure procedure) {
        if (procedure.getDataCollectionId() == null) {
            return;
        }

        DataCollection dataCollection = dataCollectionMapper.selectById(procedure.getDataCollectionId());
        if (dataCollection != null) {
            response.setDataCollectionId(dataCollection.getId());
            response.setDataCollectionType(dataCollection.getType());
            response.setDataRemark(dataCollection.getRemark());
            response.setDataNeedPhoto(dataCollection.getNeedPhoto());
            response.setDataNeedDoc(dataCollection.getNeedDoc());
        }
    }

    /**
     * 填充题库详情（类型3）
     */
    private void fillTopicDetail(TeacherProcedureDetailResponse response, ExperimentalProcedure procedure) {
        if (procedure.getProcedureTopicId() == null) {
            return;
        }

        // 查询题库配置
        ProcedureTopic procedureTopic = procedureTopicMapper.selectById(procedure.getProcedureTopicId());
        if (procedureTopic != null) {
            response.setProcedureTopicId(procedureTopic.getId());
            response.setTopicIsRandom(procedureTopic.getIsRandom());
            response.setTopicNumber(procedureTopic.getNumber());
            response.setTopicTypes(procedureTopic.getTopicTypes());

            // 查询标签信息
            if (procedureTopic.getTags() != null && !procedureTopic.getTags().trim().isEmpty()) {
                String[] tagIds = procedureTopic.getTags().split(",");
                List<Long> tagIdList = Arrays.stream(tagIds)
                        .filter(s -> s != null && !s.isEmpty())
                        .map(Long::parseLong)
                        .collect(Collectors.toList());

                if (!tagIdList.isEmpty()) {
                    List<Tag> tags = tagMapper.selectList(
                        new LambdaQueryWrapper<Tag>()
                            .in(Tag::getId, tagIdList)
                    );

                    List<TeacherProcedureDetailResponse.TagInfo> tagInfos = tags.stream()
                            .map(tag -> {
                                TeacherProcedureDetailResponse.TagInfo tagInfo =
                                    new TeacherProcedureDetailResponse.TagInfo();
                                tagInfo.setId(tag.getId());
                                tagInfo.setTagName(tag.getTagName());
                                tagInfo.setType(tag.getType());
                                tagInfo.setDescription(tag.getDescription());
                                return tagInfo;
                            })
                            .collect(Collectors.toList());

                    response.setTopicTags(tagInfos);
                }
            }
        }

        // 非随机模式下才返回题目列表
        if (procedureTopic == null || !Boolean.TRUE.equals(procedureTopic.getIsRandom())) {
            // 查询题目映射
            LambdaQueryWrapper<ProcedureTopicMap> topicMapQueryWrapper = new LambdaQueryWrapper<>();
            topicMapQueryWrapper.eq(ProcedureTopicMap::getExperimentalProcedureId, procedure.getId());
            List<ProcedureTopicMap> topicMaps = procedureTopicMapMapper.selectList(topicMapQueryWrapper);

            if (topicMaps != null && !topicMaps.isEmpty()) {
                // 提取题目ID列表
                List<Long> topicIds = topicMaps.stream()
                        .map(ProcedureTopicMap::getTopicId)
                        .collect(Collectors.toList());
                response.setTopicIds(topicIds);

                // 查询题目详情（包含答案）
                LambdaQueryWrapper<Topic> topicQueryWrapper = new LambdaQueryWrapper<>();
                topicQueryWrapper.in(Topic::getId, topicIds)
                        .eq(Topic::getIsDeleted, false)
                        .orderByAsc(Topic::getNumber);
                List<Topic> topics = topicMapper.selectList(topicQueryWrapper);

                if (topics != null && !topics.isEmpty()) {
                    List<TeacherProcedureDetailResponse.TopicDetail> topicDetails = new ArrayList<>();
                    for (Topic topic : topics) {
                        TeacherProcedureDetailResponse.TopicDetail topicDetail = new TeacherProcedureDetailResponse.TopicDetail();
                        topicDetail.setId(topic.getId());
                        topicDetail.setNumber(topic.getNumber());
                        topicDetail.setType(topic.getType());
                        topicDetail.setContent(topic.getContent());
                        topicDetail.setChoices(topic.getChoices());
                        topicDetail.setCorrectAnswer(topic.getCorrectAnswer());
                        topicDetails.add(topicDetail);
                    }
                    response.setTopics(topicDetails);
                }
            }
        }
    }

    /**
     * 填充限时答题详情（类型5）
     */
    private void fillTimedQuizDetail(TeacherProcedureDetailResponse response, ExperimentalProcedure procedure) {
        if (procedure.getTimedQuizId() == null) {
            return;
        }

        TimedQuizProcedure timedQuiz = timedQuizProcedureMapper.selectById(procedure.getTimedQuizId());
        if (timedQuiz == null) {
            return;
        }

        response.setTimedQuizId(timedQuiz.getId());
        response.setTimedQuizIsRandom(timedQuiz.getIsRandom());
        response.setTimedQuizNumber(timedQuiz.getTopicNumber());
        response.setTimedQuizTimeLimit(timedQuiz.getQuizTimeLimit());
        response.setTimedQuizTopicTypes(timedQuiz.getTopicTypes());

        // 查询标签信息
        if (timedQuiz.getTopicTags() != null && !timedQuiz.getTopicTags().trim().isEmpty()) {
            String[] tagIds = timedQuiz.getTopicTags().split(",");
            List<Long> tagIdList = Arrays.stream(tagIds)
                    .filter(s -> s != null && !s.isEmpty())
                    .map(Long::parseLong)
                    .collect(Collectors.toList());

            if (!tagIdList.isEmpty()) {
                List<Tag> tags = tagMapper.selectList(
                    new LambdaQueryWrapper<Tag>()
                        .in(Tag::getId, tagIdList)
                );

                List<TeacherProcedureDetailResponse.TagInfo> tagInfos = tags.stream()
                        .map(tag -> {
                            TeacherProcedureDetailResponse.TagInfo tagInfo =
                                new TeacherProcedureDetailResponse.TagInfo();
                            tagInfo.setId(tag.getId());
                            tagInfo.setTagName(tag.getTagName());
                            tagInfo.setType(tag.getType());
                            tagInfo.setDescription(tag.getDescription());
                            return tagInfo;
                        })
                        .collect(Collectors.toList());

                response.setTimedQuizTags(tagInfos);
            }
        }

        // 非随机模式下才返回题目列表
        if (!Boolean.TRUE.equals(timedQuiz.getIsRandom())) {
            // 查询题目列表
            List<Topic> topics = getTopicsForTimedQuizProcedure(procedure, timedQuiz);
            if (topics != null && !topics.isEmpty()) {
                List<TeacherProcedureDetailResponse.TimedQuizTopicDetail> topicDetails = new ArrayList<>();
                for (Topic topic : topics) {
                    TeacherProcedureDetailResponse.TimedQuizTopicDetail topicDetail =
                        new TeacherProcedureDetailResponse.TimedQuizTopicDetail();
                    topicDetail.setId(topic.getId());
                    topicDetail.setNumber(topic.getNumber());
                    topicDetail.setType(topic.getType());
                    topicDetail.setContent(topic.getContent());
                    topicDetail.setChoices(topic.getChoices());
                    topicDetail.setCorrectAnswer(topic.getCorrectAnswer());
                    topicDetails.add(topicDetail);
                }
                response.setTimedQuizTopics(topicDetails);
            }
        }
    }

    /**
     * 获取限时答题的题目列表
     */
    private List<Topic> getTopicsForTimedQuizProcedure(ExperimentalProcedure procedure, TimedQuizProcedure timedQuiz) {
        if (Boolean.TRUE.equals(timedQuiz.getIsRandom())) {
            // 随机模式：根据标签过滤题目
            if (timedQuiz.getTopicTags() != null && !timedQuiz.getTopicTags().isEmpty()) {
                String[] tagIds = timedQuiz.getTopicTags().split(",");
                List<Long> tagIdList = Arrays.stream(tagIds)
                    .filter(s -> s != null && !s.isEmpty())
                    .map(Long::parseLong)
                    .collect(Collectors.toList());

                if (!tagIdList.isEmpty()) {
                    LambdaQueryWrapper<TopicTagMap> tagWrapper = new LambdaQueryWrapper<>();
                    tagWrapper.in(TopicTagMap::getTagId, tagIdList);
                    List<TopicTagMap> topicTagMaps = topicTagMapMapper.selectList(tagWrapper);

                    if (!topicTagMaps.isEmpty()) {
                        List<Long> topicIds = topicTagMaps.stream()
                            .map(TopicTagMap::getTopicId)
                            .distinct()
                            .collect(Collectors.toList());

                        if (!topicIds.isEmpty()) {
                            LambdaQueryWrapper<Topic> topicWrapper = new LambdaQueryWrapper<>();
                            topicWrapper.in(Topic::getId, topicIds);

                            // 添加题目类型过滤
                            if (timedQuiz.getTopicTypes() != null && !timedQuiz.getTopicTypes().isEmpty()) {
                                String[] typeArray = timedQuiz.getTopicTypes().split(",");
                                List<Integer> types = Arrays.stream(typeArray)
                                    .filter(s -> s != null && !s.isEmpty())
                                    .map(Integer::parseInt)
                                    .collect(Collectors.toList());
                                if (!types.isEmpty()) {
                                    topicWrapper.in(Topic::getType, types);
                                }
                            }

                            topicWrapper.orderByAsc(Topic::getNumber);
                            return topicMapper.selectList(topicWrapper);
                        }
                    }
                }
            }
            return new ArrayList<>();
        } else {
            // 老师选定模式：从题库详情映射表查询
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ProcedureTopicMap> mapWrapper =
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
            mapWrapper.eq("experimental_procedure_id", procedure.getId());
            mapWrapper.orderByAsc("id");
            List<ProcedureTopicMap> topicMaps = procedureTopicMapMapper.selectList(mapWrapper);

            if (!topicMaps.isEmpty()) {
                List<Long> topicIds = topicMaps.stream()
                    .map(ProcedureTopicMap::getTopicId)
                    .collect(Collectors.toList());

                com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Topic> topicWrapper =
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
                topicWrapper.in("id", topicIds);
                topicWrapper.orderByAsc("number");
                return topicMapper.selectList(topicWrapper);
            }
            return new ArrayList<>();
        }
    }

    // ==================== 按类型分离的查询方法 ====================

    /**
     * 查询视频步骤详情
     * 步骤类型：type=1（观看视频）
     *
     * @param procedureId 步骤ID
     * @return 视频步骤详情
     */
    public TeacherVideoProcedureDetailResponse getVideoProcedureDetail(Long procedureId) {
        log.info("查询视频步骤详情，步骤ID: {}", procedureId);

        ExperimentalProcedure procedure = validateAndGetProcedure(procedureId);
        if (procedure.getType() != 1) {
            throw new com.example.demo.exception.BusinessException(400, "该步骤不是视频类型");
        }

        TeacherVideoProcedureDetailResponse response = new TeacherVideoProcedureDetailResponse();
        fillBaseProcedureInfo(response, procedure);
        fillVideoDetail(response, procedure);

        return response;
    }

    /**
     * 查询数据收集步骤详情
     * 步骤类型：type=2（数据收集）
     *
     * @param procedureId 步骤ID
     * @return 数据收集步骤详情
     */
    public TeacherDataCollectionProcedureDetailResponse getDataCollectionProcedureDetail(Long procedureId) {
        log.info("查询数据收集步骤详情，步骤ID: {}", procedureId);

        ExperimentalProcedure procedure = validateAndGetProcedure(procedureId);
        if (procedure.getType() != 2) {
            throw new com.example.demo.exception.BusinessException(400, "该步骤不是数据收集类型");
        }

        TeacherDataCollectionProcedureDetailResponse response = new TeacherDataCollectionProcedureDetailResponse();
        fillBaseProcedureInfo(response, procedure);
        fillDataCollectionDetail(response, procedure);

        return response;
    }

    /**
     * 查询题库步骤详情
     * 步骤类型：type=3（题库答题）或 type=5（限时答题）
     *
     * @param procedureId 步骤ID
     * @return 题库步骤详情
     */
    public TeacherTopicProcedureDetailResponse getTopicProcedureDetail(Long procedureId) {
        log.info("查询题库步骤详情，步骤ID: {}", procedureId);

        ExperimentalProcedure procedure = validateAndGetProcedure(procedureId);
        if (procedure.getType() != 3 && procedure.getType() != 5) {
            throw new com.example.demo.exception.BusinessException(400, "该步骤不是题库答题或限时答题类型");
        }

        TeacherTopicProcedureDetailResponse response = new TeacherTopicProcedureDetailResponse();
        fillBaseProcedureInfo(response, procedure);

        if (procedure.getType() == 3) {
            fillTopicDetail(response, procedure);
        } else if (procedure.getType() == 5) {
            fillTimedQuizDetail(response, procedure);
        }

        return response;
    }

    // ==================== 私有辅助方法 ====================

    private ExperimentalProcedure validateAndGetProcedure(Long procedureId) {
        ExperimentalProcedure procedure = experimentalProcedureService.getById(procedureId);
        if (procedure == null) {
            throw new com.example.demo.exception.BusinessException(404, "步骤不存在");
        }
        return procedure;
    }

    private void fillBaseProcedureInfo(Object response, ExperimentalProcedure procedure) {
        try {
            var setId = response.getClass().getMethod("setId", Long.class);
            var setExperimentId = response.getClass().getMethod("setExperimentId", Long.class);
            var setNumber = response.getClass().getMethod("setNumber", Integer.class);
            var setType = response.getClass().getMethod("setType", Integer.class);
            var setRemark = response.getClass().getMethod("setRemark", String.class);
            var setIsSkip = response.getClass().getMethod("setIsSkip", Boolean.class);
            var setProportion = response.getClass().getMethod("setProportion", Integer.class);
            var setOffsetMinutes = response.getClass().getMethod("setOffsetMinutes", Integer.class);
            var setDurationMinutes = response.getClass().getMethod("setDurationMinutes", Integer.class);

            setId.invoke(response, procedure.getId());
            setExperimentId.invoke(response, procedure.getExperimentId());
            setNumber.invoke(response, procedure.getNumber());
            setType.invoke(response, procedure.getType());
            setRemark.invoke(response, procedure.getRemark());
            setIsSkip.invoke(response, procedure.getIsSkip());
            setProportion.invoke(response, procedure.getProportion());
            setOffsetMinutes.invoke(response, procedure.getOffsetMinutes());
            setDurationMinutes.invoke(response, procedure.getDurationMinutes());
        } catch (Exception e) {
            log.error("填充基础步骤信息失败", e);
        }
    }

    private void fillVideoDetail(TeacherVideoProcedureDetailResponse response, ExperimentalProcedure procedure) {
        if (procedure.getVideoId() == null) {
            return;
        }

        VideoFile videoFile = videoFileMapper.selectById(procedure.getVideoId());
        if (videoFile != null) {
            response.setVideoId(videoFile.getId());
            response.setVideoTitle(videoFile.getOriginalFileName());
            response.setVideoSeconds(videoFile.getVideoSeconds());
            response.setVideoFilePath(videoFile.getFilePath());
            response.setVideoFileSize(videoFile.getFileSize());
        }
    }

    private void fillDataCollectionDetail(TeacherDataCollectionProcedureDetailResponse response, ExperimentalProcedure procedure) {
        if (procedure.getDataCollectionId() == null) {
            return;
        }

        DataCollection dataCollection = dataCollectionMapper.selectById(procedure.getDataCollectionId());
        if (dataCollection != null) {
            response.setDataCollectionId(dataCollection.getId());
            response.setDataCollectionType(dataCollection.getType());
            response.setDataRemark(dataCollection.getRemark());
            response.setDataNeedPhoto(dataCollection.getNeedPhoto());
            response.setDataNeedDoc(dataCollection.getNeedDoc());
        }
    }

    private void fillTopicDetail(TeacherTopicProcedureDetailResponse response, ExperimentalProcedure procedure) {
        if (procedure.getProcedureTopicId() == null) {
            return;
        }

        ProcedureTopic procedureTopic = procedureTopicMapper.selectById(procedure.getProcedureTopicId());
        if (procedureTopic != null) {
            response.setProcedureTopicId(procedureTopic.getId());
            response.setTopicIsRandom(procedureTopic.getIsRandom());
            response.setTopicNumber(procedureTopic.getNumber());
            response.setTopicTypes(procedureTopic.getTopicTypes());

            if (procedureTopic.getTags() != null && !procedureTopic.getTags().trim().isEmpty()) {
                String[] tagIds = procedureTopic.getTags().split(",");
                List<Long> tagIdList = Arrays.stream(tagIds)
                        .filter(s -> s != null && !s.isEmpty())
                        .map(Long::parseLong)
                        .collect(Collectors.toList());

                if (!tagIdList.isEmpty()) {
                    List<Tag> tags = tagMapper.selectList(
                            new LambdaQueryWrapper<Tag>().in(Tag::getId, tagIdList)
                    );

                    List<TeacherProcedureDetailResponse.TagInfo> tagInfos = tags.stream()
                            .map(tag -> {
                                TeacherProcedureDetailResponse.TagInfo tagInfo =
                                        new TeacherProcedureDetailResponse.TagInfo();
                                tagInfo.setId(tag.getId());
                                tagInfo.setTagName(tag.getTagName());
                                tagInfo.setType(tag.getType());
                                tagInfo.setDescription(tag.getDescription());
                                return tagInfo;
                            })
                            .collect(Collectors.toList());

                    response.setTopicTags(tagInfos);
                }
            }

            if (!Boolean.TRUE.equals(procedureTopic.getIsRandom())) {
                LambdaQueryWrapper<ProcedureTopicMap> topicMapQueryWrapper = new LambdaQueryWrapper<>();
                topicMapQueryWrapper.eq(ProcedureTopicMap::getExperimentalProcedureId, procedure.getId());
                List<ProcedureTopicMap> topicMaps = procedureTopicMapMapper.selectList(topicMapQueryWrapper);

                if (topicMaps != null && !topicMaps.isEmpty()) {
                    List<Long> topicIds = topicMaps.stream()
                            .map(ProcedureTopicMap::getTopicId)
                            .collect(Collectors.toList());
                    response.setTopicIds(topicIds);

                    LambdaQueryWrapper<Topic> topicQueryWrapper = new LambdaQueryWrapper<>();
                    topicQueryWrapper.in(Topic::getId, topicIds)
                            .eq(Topic::getIsDeleted, false)
                            .orderByAsc(Topic::getNumber);
                    List<Topic> topics = topicMapper.selectList(topicQueryWrapper);

                    if (topics != null && !topics.isEmpty()) {
                        List<TeacherProcedureDetailResponse.TopicDetail> topicDetails = new ArrayList<>();
                        for (Topic topic : topics) {
                            TeacherProcedureDetailResponse.TopicDetail topicDetail = new TeacherProcedureDetailResponse.TopicDetail();
                            topicDetail.setId(topic.getId());
                            topicDetail.setNumber(topic.getNumber());
                            topicDetail.setType(topic.getType());
                            topicDetail.setContent(topic.getContent());
                            topicDetail.setChoices(topic.getChoices());
                            topicDetail.setCorrectAnswer(topic.getCorrectAnswer());
                            topicDetails.add(topicDetail);
                        }
                        response.setTopics(topicDetails);
                    }
                }
            }
        }
    }

    private void fillTimedQuizDetail(TeacherTopicProcedureDetailResponse response, ExperimentalProcedure procedure) {
        if (procedure.getTimedQuizId() == null) {
            return;
        }

        TimedQuizProcedure timedQuiz = timedQuizProcedureMapper.selectById(procedure.getTimedQuizId());
        if (timedQuiz == null) {
            return;
        }

        response.setTimedQuizId(timedQuiz.getId());
        response.setTimedQuizIsRandom(timedQuiz.getIsRandom());
        response.setTimedQuizNumber(timedQuiz.getTopicNumber());
        response.setTimedQuizTimeLimit(timedQuiz.getQuizTimeLimit());
        response.setTimedQuizTopicTypes(timedQuiz.getTopicTypes());

        if (timedQuiz.getTopicTags() != null && !timedQuiz.getTopicTags().trim().isEmpty()) {
            String[] tagIds = timedQuiz.getTopicTags().split(",");
            List<Long> tagIdList = Arrays.stream(tagIds)
                    .filter(s -> s != null && !s.isEmpty())
                    .map(Long::parseLong)
                    .collect(Collectors.toList());

            if (!tagIdList.isEmpty()) {
                List<Tag> tags = tagMapper.selectList(
                        new LambdaQueryWrapper<Tag>().in(Tag::getId, tagIdList)
                );

                List<TeacherProcedureDetailResponse.TagInfo> tagInfos = tags.stream()
                        .map(tag -> {
                            TeacherProcedureDetailResponse.TagInfo tagInfo =
                                    new TeacherProcedureDetailResponse.TagInfo();
                            tagInfo.setId(tag.getId());
                            tagInfo.setTagName(tag.getTagName());
                            tagInfo.setType(tag.getType());
                            tagInfo.setDescription(tag.getDescription());
                            return tagInfo;
                        })
                        .collect(Collectors.toList());

                response.setTimedQuizTags(tagInfos);
            }
        }

        if (!Boolean.TRUE.equals(timedQuiz.getIsRandom())) {
            List<Topic> topics = getTopicsForTimedQuizProcedure(procedure, timedQuiz);
            if (topics != null && !topics.isEmpty()) {
                List<TeacherProcedureDetailResponse.TimedQuizTopicDetail> topicDetails = new ArrayList<>();
                for (Topic topic : topics) {
                    TeacherProcedureDetailResponse.TimedQuizTopicDetail topicDetail =
                            new TeacherProcedureDetailResponse.TimedQuizTopicDetail();
                    topicDetail.setId(topic.getId());
                    topicDetail.setNumber(topic.getNumber());
                    topicDetail.setType(topic.getType());
                    topicDetail.setContent(topic.getContent());
                    topicDetail.setChoices(topic.getChoices());
                    topicDetail.setCorrectAnswer(topic.getCorrectAnswer());
                    topicDetails.add(topicDetail);
                }
                response.setTimedQuizTopics(topicDetails);
            }
        }
    }
}
