package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demo.mapper.ClassExperimentMapper;
import com.example.demo.mapper.DataCollectionMapper;
import com.example.demo.mapper.ProcedureTopicMapMapper;
import com.example.demo.mapper.ProcedureTopicMapper;
import com.example.demo.mapper.TopicMapper;
import com.example.demo.mapper.VideoFileMapper;
import com.example.demo.pojo.entity.ClassExperiment;
import com.example.demo.pojo.entity.DataCollection;
import com.example.demo.pojo.entity.ExperimentalProcedure;
import com.example.demo.pojo.entity.ProcedureTopic;
import com.example.demo.pojo.entity.ProcedureTopicMap;
import com.example.demo.pojo.entity.Topic;
import com.example.demo.pojo.entity.VideoFile;
import com.example.demo.pojo.response.TeacherProcedureDetailResponse;
import com.example.demo.util.ProcedureTimeCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    /**
     * 查询步骤详情(包含类型特定的完整信息)
     *
     * @param procedureId       步骤ID
     * @param classExperimentId 班级实验ID(可选,用于查询时间配置)
     * @return 步骤详情
     */
    public TeacherProcedureDetailResponse getProcedureDetail(Long procedureId, Long classExperimentId) {
        log.info("查询步骤详情，步骤ID: {}, 班级实验ID: {}", procedureId, classExperimentId);

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
        if (classExperimentId != null) {
            ClassExperiment classExperiment = classExperimentMapper.selectById(classExperimentId);
            if (classExperiment != null) {
                // 返回偏移量和持续时间
                response.setOffsetMinutes(procedure.getOffsetMinutes());
                response.setDurationMinutes(procedure.getDurationMinutes());

                // 计算并返回实际时间
                if (procedure.getOffsetMinutes() != null) {
                    LocalDateTime startTime = ProcedureTimeCalculator.calculateStartTime(
                            classExperiment.getStartTime(),
                            procedure.getOffsetMinutes()
                    );
                    LocalDateTime endTime = ProcedureTimeCalculator.calculateEndTime(
                            startTime,
                            procedure.getDurationMinutes()
                    );
                    response.setStartTime(startTime);
                    response.setEndTime(endTime);
                }
            } else {
                response.setOffsetMinutes(procedure.getOffsetMinutes());
                response.setDurationMinutes(procedure.getDurationMinutes());
                response.setStartTime(null);
                response.setEndTime(null);
            }
        } else {
            response.setOffsetMinutes(procedure.getOffsetMinutes());
            response.setDurationMinutes(procedure.getDurationMinutes());
            response.setStartTime(null);
            response.setEndTime(null);
        }

        // 4. 根据步骤类型填充详细信息
        fillProcedureDetailByType(response, procedure);

        return response;
    }

    /**
     * 根据步骤类型查询实验的所有步骤详情
     *
     * @param experimentId      实验ID
     * @param classExperimentId 班级实验ID(可选,用于查询时间配置)
     * @return 步骤详情列表
     */
    public List<TeacherProcedureDetailResponse> getExperimentProcedures(Long experimentId, Long classExperimentId) {
        log.info("查询实验的所有步骤详情，实验ID: {}, 班级实验ID: {}", experimentId, classExperimentId);

        // 1. 查询实验的所有步骤
        List<ExperimentalProcedure> procedures = experimentalProcedureService.getByExperimentId(experimentId);

        // 2. 查询班级实验(如果提供了 classExperimentId)
        ClassExperiment classExperiment = null;
        if (classExperimentId != null) {
            classExperiment = classExperimentMapper.selectById(classExperimentId);
        }

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

            // 计算并返回实际时间
            if (classExperiment != null && procedure.getOffsetMinutes() != null) {
                LocalDateTime startTime = ProcedureTimeCalculator.calculateStartTime(
                        classExperiment.getStartTime(),
                        procedure.getOffsetMinutes()
                );
                LocalDateTime endTime = ProcedureTimeCalculator.calculateEndTime(
                        startTime,
                        procedure.getDurationMinutes()
                );
                response.setStartTime(startTime);
                response.setEndTime(endTime);
            } else {
                response.setStartTime(null);
                response.setEndTime(null);
            }

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
            response.setTopicTags(procedureTopic.getTags());
        }

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
