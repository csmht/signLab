package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.enums.ProcedureAccessDeniedReason;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.ClassExperimentMapper;
import com.example.demo.mapper.DataCollectionMapper;
import com.example.demo.mapper.ExperimentMapper;
import com.example.demo.mapper.ProcedureTopicMapMapper;
import com.example.demo.mapper.ProcedureTopicMapper;
import com.example.demo.mapper.TopicMapper;
import com.example.demo.mapper.VideoFileMapper;
import com.example.demo.pojo.entity.ClassExperiment;
import com.example.demo.pojo.entity.DataCollection;
import com.example.demo.pojo.entity.ExperimentalProcedure;
import com.example.demo.pojo.entity.Experiment;
import com.example.demo.pojo.entity.ProcedureTopic;
import com.example.demo.pojo.entity.ProcedureTopicMap;
import com.example.demo.pojo.entity.StudentExperimentalProcedure;
import com.example.demo.pojo.entity.Topic;
import com.example.demo.pojo.entity.VideoFile;
import com.example.demo.pojo.response.StudentExperimentDetailResponse;
import com.example.demo.pojo.response.StudentProcedureDetailResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 学生实验���务
 * 提供学生查询实验详情的业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StudentExperimentService {

    private final ExperimentMapper experimentMapper;
    private final ClassExperimentMapper classExperimentMapper;
    private final ExperimentalProcedureService experimentalProcedureService;
    private final StudentExperimentalProcedureService studentExperimentalProcedureService;
    private final VideoFileMapper videoFileMapper;
    private final DataCollectionMapper dataCollectionMapper;
    private final ProcedureTopicMapper procedureTopicMapper;
    private final ProcedureTopicMapMapper procedureTopicMapMapper;
    private final TopicMapper topicMapper;

    /**
     * 查询学生实验详情（包含步骤列表及可访问性）
     *
     * @param experimentId    实验ID
     * @param classCode       班级编号
     * @param studentUsername 学生用户名
     * @return 实验详情
     */
    public StudentExperimentDetailResponse getStudentExperimentDetail(
            Long experimentId, String classCode, String studentUsername) {

        // 1. 查询实验基本信息
        Experiment experiment = experimentMapper.selectById(experimentId);
        if (experiment == null) {
            throw new BusinessException(404, "实验不存在");
        }

        // 2. 查询班级实验绑定信息
        ClassExperiment classExperiment = classExperimentMapper.selectOne(
                new QueryWrapper<ClassExperiment>()
                        .eq("class_code", classCode)
                        .eq("experiment_id", experimentId.toString())
        );
        if (classExperiment == null) {
            throw new BusinessException(404, "班级未绑定该实验");
        }

        // 3. 查询实验步骤列表
        List<ExperimentalProcedure> procedures = experimentalProcedureService.getByExperimentId(experimentId);
        if (procedures == null || procedures.isEmpty()) {
            throw new BusinessException(404, "实验步骤不存在");
        }

        // 4. 查询学生的步骤答案
        List<StudentExperimentalProcedure> studentProcedures =
                studentExperimentalProcedureService.getByStudentAndExperiment(
                        studentUsername, classCode, experimentId);

        // 5. 构建步骤详情响应列表
        List<StudentProcedureDetailResponse> procedureDetails = procedures.stream()
                .map(procedure -> buildProcedureDetailResponse(
                        procedure,
                        studentUsername,
                        classCode,
                        studentProcedures
                ))
                .collect(Collectors.toList());

        // 6. 计算实验总进度
        long completedCount = procedureDetails.stream()
                .filter(StudentProcedureDetailResponse::getIsCompleted)
                .count();
        String progress = completedCount + "/" + procedureDetails.size();

        // 7. 构建响应
        StudentExperimentDetailResponse response = new StudentExperimentDetailResponse();
        response.setExperimentId(experiment.getId());
        response.setExperimentName(experiment.getExperimentName());
        response.setCourseId(classExperiment.getCourseId());
        response.setClassCode(classCode);
        response.setCourseTime(classExperiment.getCourseTime());
        response.setExperimentLocation(classExperiment.getExperimentLocation());
        response.setStartTime(classExperiment.getStartTime());
        response.setEndTime(classExperiment.getEndTime());
        response.setProcedures(procedureDetails);
        response.setProgress(progress);

        return response;
    }

    /**
     * 构建步骤详情响应
     *
     * @param procedure         步骤信息
     * @param studentUsername   学生用户名
     * @param classCode         班级编号
     * @param studentProcedures 学生的步骤答案列表
     * @return 步骤详情响应
     */
    private StudentProcedureDetailResponse buildProcedureDetailResponse(
            ExperimentalProcedure procedure,
            String studentUsername,
            String classCode,
            List<StudentExperimentalProcedure> studentProcedures) {

        StudentProcedureDetailResponse response = new StudentProcedureDetailResponse();

        // 1. 基本信息
        response.setId(procedure.getId());
        response.setNumber(procedure.getNumber());
        response.setType(procedure.getType());
        response.setRemark(procedure.getRemark());
        response.setIsSkip(procedure.getIsSkip());
        response.setProportion(procedure.getProportion());
        response.setVideoId(procedure.getVideoId());
        response.setDataCollectionId(procedure.getDataCollectionId());
        response.setProcedureTopicId(procedure.getProcedureTopicId());
        response.setStartTime(procedure.getStartTime());
        response.setEndTime(procedure.getEndTime());

        // 2. 学生完成状态
        StudentExperimentalProcedure studentProcedure = studentProcedures.stream()
                .filter(sp -> sp.getExperimentalProcedureId().equals(procedure.getId()))
                .findFirst()
                .orElse(null);

        boolean isCompleted = studentProcedure != null
                && studentProcedure.getAnswer() != null
                && !studentProcedure.getAnswer().trim().isEmpty();

        response.setIsCompleted(isCompleted);

        if (studentProcedure != null) {
            response.setAnswer(studentProcedure.getAnswer());
            response.setScore(studentProcedure.getScore());
            response.setSubmissionTime(studentProcedure.getCreatedTime());
        }

        // 3. 可访问性判断
        ProcedureAccessDeniedReason accessReason = studentExperimentalProcedureService
                .checkProcedureAccessible(
                        procedure.getExperimentId(),
                        classCode,
                        studentUsername,
                        procedure
                );

        response.setIsAccessible(accessReason == ProcedureAccessDeniedReason.ACCESSIBLE);
        response.setInaccessibleReason(accessReason.getDescription());

        // 4. 前置步骤完成状态（用于前端展示）
        response.setIsPreviousCompleted(checkIfPreviousCompleted(
                procedure, studentUsername, classCode));

        // 5. 根据步骤类型查询详细信息
        fillProcedureDetailByType(response, procedure);

        return response;
    }

    /**
     * 根据步骤类型填充详细信息
     *
     * @param response  步骤详情响应
     * @param procedure 步骤信息
     */
    private void fillProcedureDetailByType(StudentProcedureDetailResponse response, ExperimentalProcedure procedure) {
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
    private void fillVideoDetail(StudentProcedureDetailResponse response, ExperimentalProcedure procedure) {
        if (procedure.getVideoId() == null) {
            return;
        }

        VideoFile videoFile = videoFileMapper.selectById(procedure.getVideoId());
        if (videoFile != null) {
            response.setVideoTitle(videoFile.getOriginalFileName());
            response.setVideoSeconds(videoFile.getVideoSeconds());
            response.setVideoFilePath(videoFile.getFilePath());
            response.setVideoFileSize(videoFile.getFileSize());
        }
    }

    /**
     * 填充数据收集详情（类型2）
     */
    private void fillDataCollectionDetail(StudentProcedureDetailResponse response, ExperimentalProcedure procedure) {
        if (procedure.getDataCollectionId() == null) {
            return;
        }

        DataCollection dataCollection = dataCollectionMapper.selectById(procedure.getDataCollectionId());
        if (dataCollection != null) {
            response.setDataCollectionType(dataCollection.getType());
            response.setDataRemark(dataCollection.getRemark());
            response.setDataNeedPhoto(dataCollection.getNeedPhoto());
            response.setDataNeedDoc(dataCollection.getNeedDoc());
        }
    }

    /**
     * 填充题库详情（类型3）
     */
    private void fillTopicDetail(StudentProcedureDetailResponse response, ExperimentalProcedure procedure) {
        if (procedure.getProcedureTopicId() == null) {
            return;
        }

        // 查询题库配置
        ProcedureTopic procedureTopic = procedureTopicMapper.selectById(procedure.getProcedureTopicId());
        if (procedureTopic != null) {
            response.setTopicIsRandom(procedureTopic.getIsRandom());
            response.setTopicNumber(procedureTopic.getNumber());
            response.setTopicTags(procedureTopic.getTags());
        }

        // 查询题目列表
        QueryWrapper<ProcedureTopicMap> topicMapQueryWrapper = new QueryWrapper<>();
        topicMapQueryWrapper.eq("experimental_procedure_id", procedure.getId());
        List<ProcedureTopicMap> topicMaps = procedureTopicMapMapper.selectList(topicMapQueryWrapper);

        if (topicMaps != null && !topicMaps.isEmpty()) {
            // 提取题目ID列表
            List<Long> topicIds = topicMaps.stream()
                    .map(ProcedureTopicMap::getTopicId)
                    .collect(Collectors.toList());

            // 查询题目详情
            QueryWrapper<Topic> topicQueryWrapper = new QueryWrapper<>();
            topicQueryWrapper.in("id", topicIds)
                    .eq("is_deleted", false)
                    .orderByAsc("number");
            List<Topic> topics = topicMapper.selectList(topicQueryWrapper);

            if (topics != null && !topics.isEmpty()) {
                List<StudentProcedureDetailResponse.TopicDetail> topicDetails = new ArrayList<>();
                for (Topic topic : topics) {
                    StudentProcedureDetailResponse.TopicDetail topicDetail = new StudentProcedureDetailResponse.TopicDetail();
                    topicDetail.setId(topic.getId());
                    topicDetail.setNumber(topic.getNumber());
                    topicDetail.setType(topic.getType());
                    topicDetail.setContent(topic.getContent());
                    topicDetail.setChoices(topic.getChoices());
                    topicDetails.add(topicDetail);
                }
                response.setTopics(topicDetails);
            }
        }
    }

    /**
     * 检查前置步骤是否完成
     *
     * @param currentProcedure  当前步骤
     * @param studentUsername   学生用户名
     * @param classCode         班级编号
     * @return 是否完成
     */
    private boolean checkIfPreviousCompleted(
            ExperimentalProcedure currentProcedure,
            String studentUsername,
            String classCode) {

        // 第一个步骤没有前置步骤
        if (currentProcedure.getNumber() == 1) {
            return true;
        }

        // 查询所有步骤
        List<ExperimentalProcedure> allProcedures = experimentalProcedureService
                .getByExperimentId(currentProcedure.getExperimentId());

        // 检查序号小于当前步骤的步骤
        for (ExperimentalProcedure procedure : allProcedures) {
            if (procedure.getNumber() >= currentProcedure.getNumber()) {
                break;
            }

            // 只要有一个不可跳过的步骤未完成，则返回 false
            if (!Boolean.TRUE.equals(procedure.getIsSkip())) {
                boolean isCompleted = studentExperimentalProcedureService
                        .isProcedureCompleted(studentUsername, classCode, procedure.getId());
                if (!isCompleted) {
                    return false;
                }
            }
        }

        return true;
    }
}
