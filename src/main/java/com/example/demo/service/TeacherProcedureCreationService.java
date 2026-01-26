package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.mapper.DataCollectionMapper;
import com.example.demo.mapper.ProcedureTopicMapMapper;
import com.example.demo.mapper.ProcedureTopicMapper;
import com.example.demo.mapper.TopicMapper;
import com.example.demo.pojo.entity.DataCollection;
import com.example.demo.pojo.entity.ExperimentalProcedure;
import com.example.demo.pojo.entity.ProcedureTopic;
import com.example.demo.pojo.entity.ProcedureTopicMap;
import com.example.demo.pojo.entity.Topic;
import com.example.demo.pojo.request.teacher.CreateDataCollectionProcedureRequest;
import com.example.demo.pojo.request.teacher.CreateTopicProcedureRequest;
import com.example.demo.pojo.request.teacher.CreateVideoProcedureRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 教师创建步骤服务
 * 提供教师创建不同类型实验步骤的业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TeacherProcedureCreationService {

    private final ExperimentalProcedureService experimentalProcedureService;
    private final DataCollectionMapper dataCollectionMapper;
    private final ProcedureTopicMapper procedureTopicMapper;
    private final ProcedureTopicMapMapper procedureTopicMapMapper;
    private final TopicMapper topicMapper;

    /**
     * 创建视频观看步骤
     *
     * @param request 创建视频步骤请求
     * @return 步骤ID
     */
    @Transactional
    public Long createVideoProcedure(CreateVideoProcedureRequest request) {
        log.info("创建视频观看步骤，实验ID: {}, 步骤序号: {}", request.getExperimentId(), request.getNumber());

        // 验证必填字段
        validateCommonFields(request.getExperimentId(), request.getNumber(),
                request.getStartTime(), request.getEndTime());

        if (request.getVideoId() == null) {
            throw new com.example.demo.exception.BusinessException(400, "视频ID不能为空");
        }

        // 创建步骤实体
        ExperimentalProcedure procedure = new ExperimentalProcedure();
        procedure.setExperimentId(request.getExperimentId());
        procedure.setNumber(request.getNumber());
        procedure.setType(1); // 视频观看类型
        procedure.setIsSkip(request.getIsSkip() != null ? request.getIsSkip() : false);
        procedure.setProportion(request.getProportion() != null ? request.getProportion() : 0);
        procedure.setRemark(request.getRemark());
        procedure.setVideoId(request.getVideoId());
        procedure.setStartTime(request.getStartTime());
        procedure.setEndTime(request.getEndTime());
        procedure.setIsDeleted(false);

        // 保存步骤
        experimentalProcedureService.save(procedure);

        log.info("视频观看步骤创建成功，步骤ID: {}", procedure.getId());
        return procedure.getId();
    }

    /**
     * 创建数据收集步骤
     *
     * @param request 创建数据收集步骤请求
     * @return 步骤ID
     */
    @Transactional
    public Long createDataCollectionProcedure(CreateDataCollectionProcedureRequest request) {
        log.info("创建数据收集步骤，实验ID: {}, 步骤序号: {}", request.getExperimentId(), request.getNumber());

        // 验证必填字段
        validateCommonFields(request.getExperimentId(), request.getNumber(),
                request.getStartTime(), request.getEndTime());

        if (request.getDataType() == null) {
            throw new com.example.demo.exception.BusinessException(400, "数据类型不能为空");
        }
        if (request.getDataRemark() == null || request.getDataRemark().trim().isEmpty()) {
            throw new com.example.demo.exception.BusinessException(400, "数据描述不能为空");
        }

        // 1. 先创建步骤实体
        ExperimentalProcedure procedure = new ExperimentalProcedure();
        procedure.setExperimentId(request.getExperimentId());
        procedure.setNumber(request.getNumber());
        procedure.setType(2); // 数据收集类型
        procedure.setIsSkip(request.getIsSkip() != null ? request.getIsSkip() : false);
        procedure.setProportion(request.getProportion() != null ? request.getProportion() : 0);
        procedure.setRemark(request.getRemark());
        procedure.setStartTime(request.getStartTime());
        procedure.setEndTime(request.getEndTime());
        procedure.setIsDeleted(false);

        // 保存步骤
        experimentalProcedureService.save(procedure);
        log.info("数据收集步骤创建成功，步骤ID: {}", procedure.getId());

        // 2. 创建数据收集记录
        DataCollection dataCollection = new DataCollection();
        dataCollection.setExperimentalProcedureId(procedure.getId());
        dataCollection.setType(request.getDataType().longValue());
        dataCollection.setRemark(request.getDataRemark());
        dataCollection.setNeedPhoto(request.getNeedPhoto() != null ? request.getNeedPhoto() : false);
        dataCollection.setNeedDoc(request.getNeedDoc() != null ? request.getNeedDoc() : false);

        dataCollectionMapper.insert(dataCollection);
        log.info("数据收集记录创建成功，记录ID: {}", dataCollection.getId());

        // 3. 更新步骤的数据收集ID
        procedure.setDataCollectionId(dataCollection.getId());
        experimentalProcedureService.updateById(procedure);

        return procedure.getId();
    }

    /**
     * 创建题库练习步骤
     *
     * @param request 创建题库练习步骤请求
     * @return 步骤ID
     */
    @Transactional
    public Long createTopicProcedure(CreateTopicProcedureRequest request) {
        log.info("创建题库练习步骤，实验ID: {}, 步骤序号: {}", request.getExperimentId(), request.getNumber());

        // 验证必填字段
        validateCommonFields(request.getExperimentId(), request.getNumber(),
                request.getStartTime(), request.getEndTime());

        if (request.getIsRandom() == null) {
            throw new com.example.demo.exception.BusinessException(400, "是否随机抽取不能为空");
        }

        // 验证随机模式的字段
        if (Boolean.TRUE.equals(request.getIsRandom())) {
            if (request.getTopicNumber() == null || request.getTopicNumber() <= 0) {
                throw new com.example.demo.exception.BusinessException(400, "随机模式下题目数量必须大于0");
            }
        } else {
            // 验证老师选定模式的字段
            if (request.getTeacherSelectedTopicIds() == null || request.getTeacherSelectedTopicIds().isEmpty()) {
                throw new com.example.demo.exception.BusinessException(400, "非随机模式下必须选择题目");
            }
        }

        // 1. 先创建步骤实体
        ExperimentalProcedure procedure = new ExperimentalProcedure();
        procedure.setExperimentId(request.getExperimentId());
        procedure.setNumber(request.getNumber());
        procedure.setType(3); // 题库练习类型
        procedure.setIsSkip(request.getIsSkip() != null ? request.getIsSkip() : false);
        procedure.setProportion(request.getProportion() != null ? request.getProportion() : 0);
        procedure.setRemark(request.getRemark());
        procedure.setStartTime(request.getStartTime());
        procedure.setEndTime(request.getEndTime());
        procedure.setIsDeleted(false);

        // 保存步骤
        experimentalProcedureService.save(procedure);
        log.info("题库练习步骤创建成功，步骤ID: {}", procedure.getId());

        // 2. 创建题库详情记录
        ProcedureTopic procedureTopic = new ProcedureTopic();
        procedureTopic.setExperimentalProcedureId(procedure.getId());
        procedureTopic.setIsRandom(request.getIsRandom());
        procedureTopic.setNumber(request.getTopicNumber());
        procedureTopic.setTags(request.getTopicTags());

        procedureTopicMapper.insert(procedureTopic);
        log.info("题库详情记录创建成功，记录ID: {}", procedureTopic.getId());

        // 3. 如果是老师选定模式，创建题目映射记录
        if (!Boolean.TRUE.equals(request.getIsRandom())) {
            List<Long> topicIds = request.getTeacherSelectedTopicIds();

            // 验证题目是否存在
            QueryWrapper<Topic> topicQueryWrapper = new QueryWrapper<>();
            topicQueryWrapper.in("id", topicIds)
                    .eq("is_deleted", false);
            long existingTopicCount = topicMapper.selectCount(topicQueryWrapper);

            if (existingTopicCount != topicIds.size()) {
                throw new com.example.demo.exception.BusinessException(400,
                        String.format("有%d道题目不存在或已删除", topicIds.size() - existingTopicCount));
            }

            // 创建题目映射记录
            for (Long topicId : topicIds) {
                ProcedureTopicMap topicMap = new ProcedureTopicMap();
                topicMap.setExperimentalProcedureId(procedure.getId());
                topicMap.setTopicId(topicId);
                topicMap.setProcedureTopicId(procedureTopic.getId());
                procedureTopicMapMapper.insert(topicMap);
            }
            log.info("创建了{}道题目的映射记录", topicIds.size());
        }

        // 4. 更新步骤的题库详情ID
        procedure.setProcedureTopicId(procedureTopic.getId());
        experimentalProcedureService.updateById(procedure);

        return procedure.getId();
    }

    /**
     * 验证公共必填字段
     */
    private void validateCommonFields(Long experimentId, Integer number,
                                       java.time.LocalDateTime startTime,
                                       java.time.LocalDateTime endTime) {
        if (experimentId == null) {
            throw new com.example.demo.exception.BusinessException(400, "实验ID不能为空");
        }
        if (number == null) {
            throw new com.example.demo.exception.BusinessException(400, "步骤序号不能为空");
        }
        if (startTime == null) {
            throw new com.example.demo.exception.BusinessException(400, "步骤开始时间不能为空");
        }
        if (endTime == null) {
            throw new com.example.demo.exception.BusinessException(400, "步骤结束时间不能为空");
        }
        if (startTime.isAfter(endTime)) {
            throw new com.example.demo.exception.BusinessException(400, "开始时间不能晚于结束时间");
        }
    }
}
