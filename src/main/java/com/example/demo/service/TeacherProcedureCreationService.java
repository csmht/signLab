package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.demo.mapper.DataCollectionMapper;
import com.example.demo.mapper.ProcedureTopicMapMapper;
import com.example.demo.mapper.ProcedureTopicMapper;
import com.example.demo.mapper.TimedQuizProcedureMapper;
import com.example.demo.mapper.TopicMapper;
import com.example.demo.pojo.entity.DataCollection;
import com.example.demo.pojo.entity.ExperimentalProcedure;
import com.example.demo.pojo.entity.ProcedureTopic;
import com.example.demo.pojo.entity.ProcedureTopicMap;
import com.example.demo.pojo.entity.Topic;
import com.example.demo.pojo.entity.TimedQuizProcedure;
import com.example.demo.pojo.request.teacher.CreateDataCollectionProcedureRequest;
import com.example.demo.pojo.request.teacher.CreateTimedQuizProcedureRequest;
import com.example.demo.pojo.request.teacher.CreateTopicProcedureRequest;
import com.example.demo.pojo.request.teacher.CreateVideoProcedureRequest;
import com.example.demo.pojo.request.teacher.InsertDataCollectionProcedureRequest;
import com.example.demo.pojo.request.teacher.InsertTimedQuizProcedureRequest;
import com.example.demo.pojo.request.teacher.InsertTopicProcedureRequest;
import com.example.demo.pojo.request.teacher.InsertVideoProcedureRequest;
import com.example.demo.pojo.request.teacher.UpdateDataCollectionProcedureRequest;
import com.example.demo.pojo.request.teacher.UpdateTimedQuizProcedureRequest;
import com.example.demo.pojo.request.teacher.UpdateTopicProcedureRequest;
import com.example.demo.pojo.request.teacher.UpdateVideoProcedureRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final TimedQuizProcedureMapper timedQuizProcedureMapper;
    private final TopicMapper topicMapper;
    private final ObjectMapper objectMapper;

    /**
     * 获取实验的最大步骤号
     *
     * @param experimentId 实验ID
     * @return 最大步骤号，如果没有步骤则返回0
     */
    private Integer getMaxProcedureNumber(Long experimentId) {
        QueryWrapper<ExperimentalProcedure> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("experiment_id", experimentId);
        queryWrapper.orderByDesc("number");
        queryWrapper.last("LIMIT 1");
        ExperimentalProcedure procedure = experimentalProcedureService.getOne(queryWrapper);
        return procedure != null ? procedure.getNumber() : 0;
    }

    /**
     * 创建视频观看步骤
     *
     * @param request 创建视频步骤请求
     * @return 步骤ID
     */
    @Transactional
    public Long createVideoProcedure(CreateVideoProcedureRequest request) {
        log.info("创建视频观看步骤,实验ID: {}", request.getExperimentId());

        // 验证必填字段
        if (request.getExperimentId() == null) {
            throw new com.example.demo.exception.BusinessException(400, "实验ID不能为空");
        }

        if (request.getVideoId() == null) {
            throw new com.example.demo.exception.BusinessException(400, "视频ID不能为空");
        }

        // 自动计算步骤号
        Integer newNumber = getMaxProcedureNumber(request.getExperimentId()) + 1;

        // 创建步骤实体
        ExperimentalProcedure procedure = new ExperimentalProcedure();
        procedure.setExperimentId(request.getExperimentId());
        procedure.setNumber(newNumber);
        procedure.setType(1); // 视频观看类型
        procedure.setIsSkip(request.getIsSkip() != null ? request.getIsSkip() : false);
        procedure.setProportion(request.getProportion() != null ? request.getProportion() : 0);
        procedure.setRemark(request.getRemark());
        procedure.setVideoId(request.getVideoId());

        // 设置时间字段
        validateAndSetTimeFields(procedure, request.getOffsetMinutes(), request.getDurationMinutes());

        procedure.setIsDeleted(false);

        // 保存步骤
        experimentalProcedureService.save(procedure);

        log.info("视频观看步骤创建成功,步骤ID: {}", procedure.getId());
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
        log.info("创建数据收集步骤，实验ID: {}", request.getExperimentId());

        // 验证必填字段
        if (request.getExperimentId() == null) {
            throw new com.example.demo.exception.BusinessException(400, "实验ID不能为空");
        }

        if (request.getDataType() == null) {
            throw new com.example.demo.exception.BusinessException(400, "数据类型不能为空");
        }

        // 验证数据类型对应的字段
        if (request.getDataType() == 1) {
            // 填空类型必须提供 dataFields
            if (request.getDataFields() == null || request.getDataFields().isEmpty()) {
                throw new com.example.demo.exception.BusinessException(400, "填空类型必须提供数据字段");
            }
        } else if (request.getDataType() == 2) {
            // 表格类型必须提供表头和单元格答案
            if (request.getTableRowHeaders() == null || request.getTableRowHeaders().isEmpty()) {
                throw new com.example.demo.exception.BusinessException(400, "表格类型必须提供行表头");
            }
            if (request.getTableColumnHeaders() == null || request.getTableColumnHeaders().isEmpty()) {
                throw new com.example.demo.exception.BusinessException(400, "表格类型必须提供列表头");
            }
            if (request.getTableCellAnswers() == null || request.getTableCellAnswers().isEmpty()) {
                throw new com.example.demo.exception.BusinessException(400, "表格类型必须提供单元格答案");
            }
        }

        // 自动计算步骤号
        Integer newNumber = getMaxProcedureNumber(request.getExperimentId()) + 1;

        // 1. 先创建步骤实体
        ExperimentalProcedure procedure = new ExperimentalProcedure();
        procedure.setExperimentId(request.getExperimentId());
        procedure.setNumber(newNumber);
        procedure.setType(2); // 数据收集类型
        procedure.setIsSkip(request.getIsSkip() != null ? request.getIsSkip() : false);
        procedure.setProportion(request.getProportion() != null ? request.getProportion() : 0);
        procedure.setRemark(request.getRemark());

        // 设置时间字段
        validateAndSetTimeFields(procedure, request.getOffsetMinutes(), request.getDurationMinutes());

        procedure.setIsDeleted(false);

        // 保存步骤
        experimentalProcedureService.save(procedure);
        log.info("数据收集步骤创建成功，步骤ID: {}", procedure.getId());

        // 2. 构建数据描述和正确答案JSON
        String remark = buildDataCollectionRemark(request.getDataType(), request.getDataFields(),
                request.getTableRowHeaders(), request.getTableColumnHeaders());
        String correctAnswer = buildCorrectAnswerJson(request.getDataType(), request.getDataFields(),
                request.getTableCellAnswers());

        // 3. 创建数据收集记录
        DataCollection dataCollection = new DataCollection();
        dataCollection.setExperimentalProcedureId(procedure.getId());
        dataCollection.setType(request.getDataType().longValue());
        dataCollection.setRemark(remark);
        dataCollection.setCorrectAnswer(correctAnswer);
        dataCollection.setTolerance(request.getTolerance());
        dataCollection.setNeedPhoto(request.getNeedPhoto() != null ? request.getNeedPhoto() : false);
        dataCollection.setNeedDoc(request.getNeedDoc() != null ? request.getNeedDoc() : false);

        dataCollectionMapper.insert(dataCollection);
        log.info("数据收集记录创建成功，记录ID: {}", dataCollection.getId());

        // 4. 更新步骤的数据收集ID
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
        log.info("创建题库练习步骤，实验ID: {}", request.getExperimentId());

        // 验证必填字段
        if (request.getExperimentId() == null) {
            throw new com.example.demo.exception.BusinessException(400, "实验ID不能为空");
        }

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

        // 自动计算步骤号
        Integer newNumber = getMaxProcedureNumber(request.getExperimentId()) + 1;

        // 1. 先创���步骤实体
        ExperimentalProcedure procedure = new ExperimentalProcedure();
        procedure.setExperimentId(request.getExperimentId());
        procedure.setNumber(newNumber);
        procedure.setType(3); // 题库练习类型
        procedure.setIsSkip(request.getIsSkip() != null ? request.getIsSkip() : false);
        procedure.setProportion(request.getProportion() != null ? request.getProportion() : 0);
        procedure.setRemark(request.getRemark());

        // 设置时间字段
        validateAndSetTimeFields(procedure, request.getOffsetMinutes(), request.getDurationMinutes());

        procedure.setIsDeleted(false);

        // 保存步骤
        experimentalProcedureService.save(procedure);
        log.info("题库练习步骤创建成功，步骤ID: {}", procedure.getId());

        // 2. 创建题库详情记录
        ProcedureTopic procedureTopic = new ProcedureTopic();
        procedureTopic.setExperimentalProcedureId(procedure.getId());
        procedureTopic.setIsRandom(request.getIsRandom());
        procedureTopic.setNumber(request.getTopicNumber());
        procedureTopic.setTags(joinTopicTags(request.getTopicTags()));
        procedureTopic.setTopicTypes(joinTopicTypes(request.getTopicTypes()));

        procedureTopicMapper.insert(procedureTopic);
        log.info("题库详情记录创建成功，记录ID: {}", procedureTopic.getId());

        // 3. 如果是老师选定模式，创建题目映射记录
        if (!Boolean.TRUE.equals(request.getIsRandom())) {
            List<Long> topicIds = request.getTeacherSelectedTopicIds();

            // 验证题目是否存在
            QueryWrapper<Topic> topicQueryWrapper = new QueryWrapper<>();
            topicQueryWrapper.in("id", topicIds);
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
     * 更新视频观看步骤
     *
     * @param request 更新视频步骤请求
     * @return 步骤ID
     */
    @Transactional
    public Long updateVideoProcedure(UpdateVideoProcedureRequest request) {
        log.info("更新视频观看步骤，步骤ID: {}", request.getId());

        // 验证步骤是否存在
        ExperimentalProcedure procedure = experimentalProcedureService.getById(request.getId());
        if (procedure == null) {
            throw new com.example.demo.exception.BusinessException(404, "步骤不存在");
        }

        // 验证步骤类型
        if (procedure.getType() != 1) {
            throw new com.example.demo.exception.BusinessException(400, "步骤类型不匹配");
        }

        // 验证视频ID
        if (request.getVideoId() == null) {
            throw new com.example.demo.exception.BusinessException(400, "视频ID不能为空");
        }

        // 更新字段
        procedure.setIsSkip(request.getIsSkip() != null ? request.getIsSkip() : procedure.getIsSkip());
        procedure.setProportion(request.getProportion() != null ? request.getProportion() : procedure.getProportion());
        procedure.setRemark(request.getRemark());
        procedure.setVideoId(request.getVideoId());

        // 设置时间字段
        validateAndSetTimeFields(procedure, request.getOffsetMinutes(), request.getDurationMinutes());

        experimentalProcedureService.updateById(procedure);
        log.info("视频观看步骤更新成功，步骤ID: {}", procedure.getId());

        return procedure.getId();
    }

    /**
     * 更新数据收集步骤
     *
     * @param request 更新数据收集步骤请求
     * @return 步骤ID
     */
    @Transactional
    public Long updateDataCollectionProcedure(UpdateDataCollectionProcedureRequest request) {
        log.info("更新数据收集步骤，步骤ID: {}", request.getId());

        // 验证步骤是否存在
        ExperimentalProcedure procedure = experimentalProcedureService.getById(request.getId());
        if (procedure == null) {
            throw new com.example.demo.exception.BusinessException(404, "步骤不存在");
        }

        // 验证步骤类型
        if (procedure.getType() != 2) {
            throw new com.example.demo.exception.BusinessException(400, "步骤类型不匹配");
        }

        // 验证必填字段
        if (request.getDataType() == null) {
            throw new com.example.demo.exception.BusinessException(400, "数据类型不能为空");
        }

        // 验证数据类型对应的字段
        if (request.getDataType() == 1) {
            // 填空类型必须提供 dataFields
            if (request.getDataFields() == null || request.getDataFields().isEmpty()) {
                throw new com.example.demo.exception.BusinessException(400, "填空类型必须提供数据字段");
            }
        } else if (request.getDataType() == 2) {
            // 表格类型必须提供表头和单元格答案
            if (request.getTableRowHeaders() == null || request.getTableRowHeaders().isEmpty()) {
                throw new com.example.demo.exception.BusinessException(400, "表格类型必须提供行表头");
            }
            if (request.getTableColumnHeaders() == null || request.getTableColumnHeaders().isEmpty()) {
                throw new com.example.demo.exception.BusinessException(400, "表格类型必须提供列表头");
            }
            if (request.getTableCellAnswers() == null || request.getTableCellAnswers().isEmpty()) {
                throw new com.example.demo.exception.BusinessException(400, "表格类型必须提供单元格答案");
            }
        }

        // 更新步骤字段
        procedure.setIsSkip(request.getIsSkip() != null ? request.getIsSkip() : procedure.getIsSkip());
        procedure.setProportion(request.getProportion() != null ? request.getProportion() : procedure.getProportion());
        procedure.setRemark(request.getRemark());

        // 设置时间字段
        validateAndSetTimeFields(procedure, request.getOffsetMinutes(), request.getDurationMinutes());

        experimentalProcedureService.updateById(procedure);
        log.info("数据收集步骤更新成功，步骤ID: {}", procedure.getId());

        // 更新数据收集记录
        if (procedure.getDataCollectionId() != null) {
            DataCollection dataCollection = dataCollectionMapper.selectById(procedure.getDataCollectionId());
            if (dataCollection != null) {
                dataCollection.setType(request.getDataType().longValue());

                // 构建数据描述和正确答案JSON
                String remark = buildDataCollectionRemark(request.getDataType(), request.getDataFields(),
                        request.getTableRowHeaders(), request.getTableColumnHeaders());
                String correctAnswer = buildCorrectAnswerJson(request.getDataType(), request.getDataFields(),
                        request.getTableCellAnswers());

                dataCollection.setRemark(remark);
                dataCollection.setCorrectAnswer(correctAnswer);
                dataCollection.setTolerance(request.getTolerance());
                dataCollection.setNeedPhoto(request.getNeedPhoto() != null ? request.getNeedPhoto() : dataCollection.getNeedPhoto());
                dataCollection.setNeedDoc(request.getNeedDoc() != null ? request.getNeedDoc() : dataCollection.getNeedDoc());
                dataCollectionMapper.updateById(dataCollection);
                log.info("数据收集记录更新成功，记录ID: {}", dataCollection.getId());
            }
        }

        return procedure.getId();
    }

    /**
     * 更新题库练习步骤
     *
     * @param request 更新题库练习步骤请求
     * @return 步骤ID
     */
    @Transactional
    public Long updateTopicProcedure(UpdateTopicProcedureRequest request) {
        log.info("更新题库练习步骤，步骤ID: {}", request.getId());

        // 验证步骤是否存在
        ExperimentalProcedure procedure = experimentalProcedureService.getById(request.getId());
        if (procedure == null) {
            throw new com.example.demo.exception.BusinessException(404, "步骤不存在");
        }

        // 验证步骤类型
        if (procedure.getType() != 3) {
            throw new com.example.demo.exception.BusinessException(400, "步骤类型不匹配");
        }

        // 验证必填字段
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

        // 更新步骤字段
        procedure.setIsSkip(request.getIsSkip() != null ? request.getIsSkip() : procedure.getIsSkip());
        procedure.setProportion(request.getProportion() != null ? request.getProportion() : procedure.getProportion());
        procedure.setRemark(request.getRemark());

        // 设置时间字段
        validateAndSetTimeFields(procedure, request.getOffsetMinutes(), request.getDurationMinutes());

        experimentalProcedureService.updateById(procedure);
        log.info("题库练习步骤更新成功，步骤ID: {}", procedure.getId());

        // 更新题库详情记录
        if (procedure.getProcedureTopicId() != null) {
            ProcedureTopic procedureTopic = procedureTopicMapper.selectById(procedure.getProcedureTopicId());
            if (procedureTopic != null) {
                procedureTopic.setIsRandom(request.getIsRandom());
                procedureTopic.setNumber(request.getTopicNumber());
                procedureTopic.setTags(joinTopicTags(request.getTopicTags()));
                procedureTopic.setTopicTypes(joinTopicTypes(request.getTopicTypes()));
                procedureTopicMapper.updateById(procedureTopic);
                log.info("题库详情记录更新成功，记录ID: {}", procedureTopic.getId());

                // 如果是非随机模式，重新创建题目映射记录
                if (!Boolean.TRUE.equals(request.getIsRandom())) {
                    // 删除旧的映射记录
                    QueryWrapper<ProcedureTopicMap> deleteWrapper = new QueryWrapper<>();
                    deleteWrapper.eq("experimental_procedure_id", procedure.getId());
                    procedureTopicMapMapper.delete(deleteWrapper);

                    // 创建新的映射记录
                    List<Long> topicIds = request.getTeacherSelectedTopicIds();

                    // 验证题目是否存在
                    QueryWrapper<Topic> topicQueryWrapper = new QueryWrapper<>();
                    topicQueryWrapper.in("id", topicIds);
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
                    log.info("重新创建了{}道题目的映射记录", topicIds.size());
                }
            }
        }

        return procedure.getId();
    }

    /**
     * 插入视频观看步骤
     *
     * @param request 插入视频步骤请求
     * @return 步骤ID
     */
    @Transactional
    public Long insertVideoProcedure(InsertVideoProcedureRequest request) {
        log.info("插入视频观看步骤，实验ID: {}, 插入位置afterNumber: {}", request.getExperimentId(), request.getAfterNumber());

        // 验证必填字段
        if (request.getExperimentId() == null) {
            throw new com.example.demo.exception.BusinessException(400, "实验ID不能为空");
        }

        if (request.getVideoId() == null) {
            throw new com.example.demo.exception.BusinessException(400, "视频ID不能为空");
        }

        // 验证afterNumber是否存在
        QueryWrapper<ExperimentalProcedure> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("experiment_id", request.getExperimentId());
        queryWrapper.eq("number", request.getAfterNumber());
        ExperimentalProcedure afterProcedure = experimentalProcedureService.getOne(queryWrapper);
        if (afterProcedure == null) {
            throw new com.example.demo.exception.BusinessException(400, "插入位置的步骤不存在");
        }

        // 新步骤的number = afterNumber + 1
        Integer newNumber = request.getAfterNumber() + 1;

        // 更新所有 number > afterNumber 的步骤，将其 number + 1
        UpdateWrapper<ExperimentalProcedure> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("experiment_id", request.getExperimentId());
        updateWrapper.gt("number", request.getAfterNumber());
        updateWrapper.setSql("number = number + 1");
        experimentalProcedureService.update(updateWrapper);

        // 创建新步骤
        ExperimentalProcedure procedure = new ExperimentalProcedure();
        procedure.setExperimentId(request.getExperimentId());
        procedure.setNumber(newNumber);
        procedure.setType(1); // 视频观看类型
        procedure.setIsSkip(request.getIsSkip() != null ? request.getIsSkip() : false);
        procedure.setProportion(request.getProportion() != null ? request.getProportion() : 0);
        procedure.setRemark(request.getRemark());
        procedure.setVideoId(request.getVideoId());

        // 设置时间字段
        validateAndSetTimeFields(procedure, request.getOffsetMinutes(), request.getDurationMinutes());

        procedure.setIsDeleted(false);

        experimentalProcedureService.save(procedure);
        log.info("视频观看步骤插入成功，步骤ID: {}", procedure.getId());

        return procedure.getId();
    }

    /**
     * 插入数据收集步骤
     *
     * @param request 插入数据收集步骤请求
     * @return 步骤ID
     */
    @Transactional
    public Long insertDataCollectionProcedure(InsertDataCollectionProcedureRequest request) {
        log.info("插入数据收集步骤，实验ID: {}, 插入位置afterNumber: {}", request.getExperimentId(), request.getAfterNumber());

        // 验证必填字段
        if (request.getExperimentId() == null) {
            throw new com.example.demo.exception.BusinessException(400, "实验ID不能为空");
        }

        if (request.getDataType() == null) {
            throw new com.example.demo.exception.BusinessException(400, "数据类型不能为空");
        }

        // 验证数据类型对应的字段
        if (request.getDataType() == 1) {
            // 填空类型必须提供 dataFields
            if (request.getDataFields() == null || request.getDataFields().isEmpty()) {
                throw new com.example.demo.exception.BusinessException(400, "填空类型必须提供数据字段");
            }
        } else if (request.getDataType() == 2) {
            // 表格类型必须提供表头和单元格答案
            if (request.getTableRowHeaders() == null || request.getTableRowHeaders().isEmpty()) {
                throw new com.example.demo.exception.BusinessException(400, "表格类型必须提供行表头");
            }
            if (request.getTableColumnHeaders() == null || request.getTableColumnHeaders().isEmpty()) {
                throw new com.example.demo.exception.BusinessException(400, "表格类型必须提供列表头");
            }
            if (request.getTableCellAnswers() == null || request.getTableCellAnswers().isEmpty()) {
                throw new com.example.demo.exception.BusinessException(400, "表格类型必须提供单元格答案");
            }
        }

        // 验证afterNumber是否存在
        QueryWrapper<ExperimentalProcedure> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("experiment_id", request.getExperimentId());
        queryWrapper.eq("number", request.getAfterNumber());
        ExperimentalProcedure afterProcedure = experimentalProcedureService.getOne(queryWrapper);
        if (afterProcedure == null) {
            throw new com.example.demo.exception.BusinessException(400, "插入位置的步骤不存在");
        }

        // 新步骤的number = afterNumber + 1
        Integer newNumber = request.getAfterNumber() + 1;

        // 更新所有 number > afterNumber 的步骤，将其 number + 1
        UpdateWrapper<ExperimentalProcedure> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("experiment_id", request.getExperimentId());
        updateWrapper.gt("number", request.getAfterNumber());
        updateWrapper.setSql("number = number + 1");
        experimentalProcedureService.update(updateWrapper);

        // 创建新步骤
        ExperimentalProcedure procedure = new ExperimentalProcedure();
        procedure.setExperimentId(request.getExperimentId());
        procedure.setNumber(newNumber);
        procedure.setType(2); // 数据收集类型
        procedure.setIsSkip(request.getIsSkip() != null ? request.getIsSkip() : false);
        procedure.setProportion(request.getProportion() != null ? request.getProportion() : 0);
        procedure.setRemark(request.getRemark());

        // 设置时间字段
        validateAndSetTimeFields(procedure, request.getOffsetMinutes(), request.getDurationMinutes());

        procedure.setIsDeleted(false);

        experimentalProcedureService.save(procedure);
        log.info("数据收集步骤插入成功，步骤ID: {}", procedure.getId());

        // 构建数据描述和正确答案JSON
        String remark = buildDataCollectionRemark(request.getDataType(), request.getDataFields(),
                request.getTableRowHeaders(), request.getTableColumnHeaders());
        String correctAnswer = buildCorrectAnswerJson(request.getDataType(), request.getDataFields(),
                request.getTableCellAnswers());

        // 创建数据收集记录
        DataCollection dataCollection = new DataCollection();
        dataCollection.setExperimentalProcedureId(procedure.getId());
        dataCollection.setType(request.getDataType().longValue());
        dataCollection.setRemark(remark);
        dataCollection.setCorrectAnswer(correctAnswer);
        dataCollection.setTolerance(request.getTolerance());
        dataCollection.setNeedPhoto(request.getNeedPhoto() != null ? request.getNeedPhoto() : false);
        dataCollection.setNeedDoc(request.getNeedDoc() != null ? request.getNeedDoc() : false);

        dataCollectionMapper.insert(dataCollection);
        log.info("数据收集记录创建成功，记录ID: {}", dataCollection.getId());

        // 更新步骤的数据收集ID
        procedure.setDataCollectionId(dataCollection.getId());
        experimentalProcedureService.updateById(procedure);

        return procedure.getId();
    }

    /**
     * 插入题库练习步骤
     *
     * @param request 插入题库练习步骤请求
     * @return 步骤ID
     */
    @Transactional
    public Long insertTopicProcedure(InsertTopicProcedureRequest request) {
        log.info("插入题库练习步骤，实验ID: {}, 插入位置afterNumber: {}", request.getExperimentId(), request.getAfterNumber());

        // 验证必填字段
        if (request.getExperimentId() == null) {
            throw new com.example.demo.exception.BusinessException(400, "实验ID不能为空");
        }

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

        // 验证afterNumber是否存在
        QueryWrapper<ExperimentalProcedure> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("experiment_id", request.getExperimentId());
        queryWrapper.eq("number", request.getAfterNumber());
        ExperimentalProcedure afterProcedure = experimentalProcedureService.getOne(queryWrapper);
        if (afterProcedure == null) {
            throw new com.example.demo.exception.BusinessException(400, "插入位置的步骤不存在");
        }

        // 新步骤的number = afterNumber + 1
        Integer newNumber = request.getAfterNumber() + 1;

        // 更新所有 number > afterNumber 的步骤，将其 number + 1
        UpdateWrapper<ExperimentalProcedure> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("experiment_id", request.getExperimentId());
        updateWrapper.gt("number", request.getAfterNumber());
        updateWrapper.setSql("number = number + 1");
        experimentalProcedureService.update(updateWrapper);

        // 创建新步骤
        ExperimentalProcedure procedure = new ExperimentalProcedure();
        procedure.setExperimentId(request.getExperimentId());
        procedure.setNumber(newNumber);
        procedure.setType(3); // 题库练习类型
        procedure.setIsSkip(request.getIsSkip() != null ? request.getIsSkip() : false);
        procedure.setProportion(request.getProportion() != null ? request.getProportion() : 0);
        procedure.setRemark(request.getRemark());

        // 设置时��字段
        validateAndSetTimeFields(procedure, request.getOffsetMinutes(), request.getDurationMinutes());

        procedure.setIsDeleted(false);

        experimentalProcedureService.save(procedure);
        log.info("题库练习步骤插入成功，步骤ID: {}", procedure.getId());

        // 创建题库详情记录
        ProcedureTopic procedureTopic = new ProcedureTopic();
        procedureTopic.setExperimentalProcedureId(procedure.getId());
        procedureTopic.setIsRandom(request.getIsRandom());
        procedureTopic.setNumber(request.getTopicNumber());
        procedureTopic.setTags(request.getTopicTagsToString());
        procedureTopic.setTopicTypes(request.getTopicTypesToString());

        procedureTopicMapper.insert(procedureTopic);
        log.info("题库详情记录创建成功，记录ID: {}", procedureTopic.getId());

        // 如果是老师选定模式，创建题目映射记录
        if (!Boolean.TRUE.equals(request.getIsRandom())) {
            List<Long> topicIds = request.getTeacherSelectedTopicIds();

            // 验证题目是否存在
            QueryWrapper<Topic> topicQueryWrapper = new QueryWrapper<>();
            topicQueryWrapper.in("id", topicIds);
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

        // 更新步骤的题库详情ID
        procedure.setProcedureTopicId(procedureTopic.getId());
        experimentalProcedureService.updateById(procedure);

        return procedure.getId();
    }

    /**
     * 验证公共必填字段（包含number验证）
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

    /**
     * 构建数据收集的remark（数据描述JSON）
     */
    private String buildDataCollectionRemark(Integer dataType,
                                             Map<String, String> dataFields,
                                             List<String> tableRowHeaders,
                                             List<String> tableColumnHeaders) {
        try {
            Map<String, Object> remarkData = new HashMap<>();

            if (dataType == 1) {
                // 填空类型：保存数据字段名称列表
                remarkData.put("dataFields", dataFields != null ? dataFields.keySet() : List.of());
            } else if (dataType == 2) {
                // 表格类型：保存表格���构
                remarkData.put("tableRowHeaders", tableRowHeaders);
                remarkData.put("tableColumnHeaders", tableColumnHeaders);
            }

            return objectMapper.writeValueAsString(remarkData);
        } catch (Exception e) {
            log.error("构建数据描述JSON失败", e);
            throw new com.example.demo.exception.BusinessException(500, "构建数据描述失败");
        }
    }

    /**
     * 构建数据收集的correctAnswer（正确答案JSON）
     */
    private String buildCorrectAnswerJson(Integer dataType,
                                          Map<String, String> dataFields,
                                          Map<String, String> tableCellAnswers) {
        try {
            if (dataType == 1) {
                // 填空类型：使用 dataFields 作为正确答案
                return objectMapper.writeValueAsString(dataFields);
            } else if (dataType == 2) {
                // 表格类型：使用 tableCellAnswers 作为正确答案
                return objectMapper.writeValueAsString(tableCellAnswers);
            }
            return null;
        } catch (Exception e) {
            log.error("构建正确答案JSON失败", e);
            throw new com.example.demo.exception.BusinessException(500, "构建正确答案失败");
        }
    }

    /**
     * 将标签ID列表转换为逗号分隔的字符串
     *
     * @param topicTags 标签ID列表
     * @return 逗号分隔的字符串，如 "id1,id2,id3"
     */
    private String joinTopicTags(List<String> topicTags) {
        if (topicTags == null || topicTags.isEmpty()) {
            return null;
        }
        return String.join(",", topicTags);
    }

    /**
     * 将题目类型列表转换为逗号分隔的字符串
     *
     * @param topicTypes 题目类型列表
     * @return 逗号分隔的字符串，如 "1,2,3"
     */
    private String joinTopicTypes(List<Integer> topicTypes) {
        if (topicTypes == null || topicTypes.isEmpty()) {
            return null;
        }
        return topicTypes.stream()
                .map(String::valueOf)
                .collect(java.util.stream.Collectors.joining(","));
    }

    /**
     * 验证并设置步骤时间字段
     *
     * @param procedure     步骤实体
     * @param offsetMinutes 偏移时间(分钟)
     * @param durationMinutes 持续时间(分钟)
     */
    private void validateAndSetTimeFields(ExperimentalProcedure procedure,
                                          Integer offsetMinutes,
                                          Integer durationMinutes) {
        // 设置默认值并验证偏移时间
        if (offsetMinutes == null) {
            offsetMinutes = 0;
        }
        procedure.setOffsetMinutes(offsetMinutes);

        // 验证持续时间
        if (durationMinutes == null || durationMinutes <= 0) {
            throw new com.example.demo.exception.BusinessException(400, "持续时间必须为正数");
        }
        procedure.setDurationMinutes(durationMinutes);
    }

    /**
     * 创建限时答题步骤
     *
     * @param request 创建限时答题步骤请求
     * @return 步骤ID
     */
    @Transactional
    public Long createTimedQuizProcedure(CreateTimedQuizProcedureRequest request) {
        log.info("创建限时答题步骤，实验ID: {}", request.getExperimentId());

        // 1. 验证必填字段
        if (request.getExperimentId() == null) {
            throw new com.example.demo.exception.BusinessException(400, "实验ID不能为空");
        }

        if (request.getIsRandom() == null) {
            throw new com.example.demo.exception.BusinessException(400, "是否随机抽取不能为空");
        }

        if (request.getQuizTimeLimit() == null || request.getQuizTimeLimit() <= 0) {
            throw new com.example.demo.exception.BusinessException(400, "答题时间限制必须大于0");
        }

        // 2. 验证题目配置
        if (Boolean.TRUE.equals(request.getIsRandom())) {
            if (request.getTopicNumber() == null || request.getTopicNumber() <= 0) {
                throw new com.example.demo.exception.BusinessException(400, "随机模式下题目数量必须大于0");
            }
        } else {
            if (request.getTeacherSelectedTopicIds() == null || request.getTeacherSelectedTopicIds().isEmpty()) {
                throw new com.example.demo.exception.BusinessException(400, "非随机模式下必须选择题目");
            }
        }

        // 3. 自动计算步骤号
        Integer newNumber = getMaxProcedureNumber(request.getExperimentId()) + 1;

        // 4. 创建步骤实体
        ExperimentalProcedure procedure = new ExperimentalProcedure();
        procedure.setExperimentId(request.getExperimentId());
        procedure.setNumber(newNumber);
        procedure.setType(5); // 限时答题类型
        procedure.setIsSkip(request.getIsSkip() != null ? request.getIsSkip() : false);
        procedure.setProportion(request.getProportion() != null ? request.getProportion() : 0);
        procedure.setRemark(request.getRemark());

        // 设置时间字段
        validateAndSetTimeFields(procedure, request.getOffsetMinutes(), request.getDurationMinutes());

        procedure.setIsDeleted(false);

        // 保存步骤
        experimentalProcedureService.save(procedure);
        log.info("限时答题步骤创建成功，步骤ID: {}", procedure.getId());

        // 5. 创建限时答题配置记录
        TimedQuizProcedure timedQuizProcedure = new TimedQuizProcedure();
        timedQuizProcedure.setExperimentalProcedureId(procedure.getId());
        timedQuizProcedure.setIsRandom(request.getIsRandom());
        timedQuizProcedure.setTopicNumber(request.getTopicNumber());
        timedQuizProcedure.setTopicTags(joinTopicTags(request.getTopicTags()));
        timedQuizProcedure.setTopicTypes(joinTopicTypes(request.getTopicTypes()));
        timedQuizProcedure.setQuizTimeLimit(request.getQuizTimeLimit());

        timedQuizProcedureMapper.insert(timedQuizProcedure);
        log.info("限时答题配置记录创建成功，记录ID: {}", timedQuizProcedure.getId());

        // 6. 如果是老师选定模式，创建题目映射记录
        if (!Boolean.TRUE.equals(request.getIsRandom())) {
            List<Long> topicIds = request.getTeacherSelectedTopicIds();

            // 验证题目是否存在
            QueryWrapper<Topic> topicQueryWrapper = new QueryWrapper<>();
            topicQueryWrapper.in("id", topicIds);
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
                topicMap.setProcedureTopicId(timedQuizProcedure.getId());
                procedureTopicMapMapper.insert(topicMap);
            }
            log.info("创建了{}道题目的映射记录", topicIds.size());
        }

        // 7. 更新步骤的限时答题配置ID
        procedure.setTimedQuizId(timedQuizProcedure.getId());
        experimentalProcedureService.updateById(procedure);

        return procedure.getId();
    }

    /**
     * 更新限时答题步骤
     *
     * @param request 更新限时答题步骤请求
     * @return 步骤ID
     */
    @Transactional
    public Long updateTimedQuizProcedure(UpdateTimedQuizProcedureRequest request) {
        log.info("更新限时答题步骤，步骤ID: {}", request.getId());

        // 1. 验证步骤是否存在
        ExperimentalProcedure procedure = experimentalProcedureService.getById(request.getId());
        if (procedure == null) {
            throw new com.example.demo.exception.BusinessException(404, "步骤不存在");
        }

        // 2. 验证步骤类型
        if (!Integer.valueOf(5).equals(procedure.getType())) {
            throw new com.example.demo.exception.BusinessException(400, "步骤类型不匹配");
        }

        // 3. 验证必填字段
        if (request.getIsRandom() == null) {
            throw new com.example.demo.exception.BusinessException(400, "是否随机抽取不能为空");
        }

        if (request.getQuizTimeLimit() == null || request.getQuizTimeLimit() <= 0) {
            throw new com.example.demo.exception.BusinessException(400, "答题时间限制必须大于0");
        }

        // 4. 更新步骤字段
        if (request.getIsSkip() != null) {
            procedure.setIsSkip(request.getIsSkip());
        }
        if (request.getProportion() != null) {
            procedure.setProportion(request.getProportion());
        }
        if (request.getRemark() != null) {
            procedure.setRemark(request.getRemark());
        }

        // 设置时间字段
        validateAndSetTimeFields(procedure, request.getOffsetMinutes(), request.getDurationMinutes());

        experimentalProcedureService.updateById(procedure);

        // 5. 更新限时答题配置记录
        if (procedure.getTimedQuizId() != null) {
            TimedQuizProcedure timedQuiz = timedQuizProcedureMapper.selectById(procedure.getTimedQuizId());
            if (timedQuiz != null) {
                timedQuiz.setIsRandom(request.getIsRandom());
                timedQuiz.setTopicNumber(request.getTopicNumber());
                timedQuiz.setTopicTags(joinTopicTags(request.getTopicTags()));
                timedQuiz.setTopicTypes(joinTopicTypes(request.getTopicTypes()));
                timedQuiz.setQuizTimeLimit(request.getQuizTimeLimit());

                timedQuizProcedureMapper.updateById(timedQuiz);

                // 如果是非随机模式，重新创建题目映射记录
                if (!Boolean.TRUE.equals(request.getIsRandom())) {
                    // 删除旧的映射记录
                    QueryWrapper<ProcedureTopicMap> deleteWrapper = new QueryWrapper<>();
                    deleteWrapper.eq("experimental_procedure_id", procedure.getId());
                    procedureTopicMapMapper.delete(deleteWrapper);

                    // 创建新的映射记录
                    List<Long> topicIds = request.getTeacherSelectedTopicIds();

                    if (topicIds != null && !topicIds.isEmpty()) {
                        // 验证题目是否存在
                        QueryWrapper<Topic> topicQueryWrapper = new QueryWrapper<>();
                        topicQueryWrapper.in("id", topicIds);
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
                            topicMap.setProcedureTopicId(timedQuiz.getId());
                            procedureTopicMapMapper.insert(topicMap);
                        }
                    }
                }
            }
        }

        return procedure.getId();
    }

    /**
     * 插入限时答题步骤
     *
     * @param request 插入限时答题步骤请求
     * @return 步骤ID
     */
    @Transactional
    public Long insertTimedQuizProcedure(InsertTimedQuizProcedureRequest request) {
        log.info("插入限时答题步骤，实验ID: {}, 插入位置afterNumber: {}",
                request.getExperimentId(), request.getAfterNumber());

        // 1. 验证必填字段（与create相同）
        if (request.getExperimentId() == null) {
            throw new com.example.demo.exception.BusinessException(400, "实验ID不能为空");
        }

        if (request.getIsRandom() == null) {
            throw new com.example.demo.exception.BusinessException(400, "是否随机抽取不能为空");
        }

        if (request.getQuizTimeLimit() == null || request.getQuizTimeLimit() <= 0) {
            throw new com.example.demo.exception.BusinessException(400, "答题时间限制必须大于0");
        }

        // 2. 验证题目配置
        if (Boolean.TRUE.equals(request.getIsRandom())) {
            if (request.getTopicNumber() == null || request.getTopicNumber() <= 0) {
                throw new com.example.demo.exception.BusinessException(400, "随机模式下题目数量必须大于0");
            }
        } else {
            if (request.getTeacherSelectedTopicIds() == null || request.getTeacherSelectedTopicIds().isEmpty()) {
                throw new com.example.demo.exception.BusinessException(400, "非随机模式下必须选择题目");
            }
        }

        // 3. 验证afterNumber是否存在
        QueryWrapper<ExperimentalProcedure> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("experiment_id", request.getExperimentId());
        queryWrapper.eq("number", request.getAfterNumber());
        ExperimentalProcedure afterProcedure = experimentalProcedureService.getOne(queryWrapper);
        if (afterProcedure == null) {
            throw new com.example.demo.exception.BusinessException(400, "插入位置的步骤不存在");
        }

        // 4. 新步骤的number = afterNumber + 1
        Integer newNumber = request.getAfterNumber() + 1;

        // 5. 更新所有 number > afterNumber 的步骤，将其 number + 1
        com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<ExperimentalProcedure> updateWrapper =
            new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<>();
        updateWrapper.eq("experiment_id", request.getExperimentId());
        updateWrapper.gt("number", request.getAfterNumber());
        updateWrapper.setSql("number = number + 1");
        experimentalProcedureService.update(updateWrapper);

        // 6. 创建新步骤
        ExperimentalProcedure procedure = new ExperimentalProcedure();
        procedure.setExperimentId(request.getExperimentId());
        procedure.setNumber(newNumber);
        procedure.setType(5); // 限时答题类型
        procedure.setIsSkip(request.getIsSkip() != null ? request.getIsSkip() : false);
        procedure.setProportion(request.getProportion() != null ? request.getProportion() : 0);
        procedure.setRemark(request.getRemark());

        // 设置时间字段
        validateAndSetTimeFields(procedure, request.getOffsetMinutes(), request.getDurationMinutes());

        procedure.setIsDeleted(false);

        experimentalProcedureService.save(procedure);

        // 7. 创建限时答题配置记录
        TimedQuizProcedure timedQuizProcedure = new TimedQuizProcedure();
        timedQuizProcedure.setExperimentalProcedureId(procedure.getId());
        timedQuizProcedure.setIsRandom(request.getIsRandom());
        timedQuizProcedure.setTopicNumber(request.getTopicNumber());
        timedQuizProcedure.setTopicTags(joinTopicTags(request.getTopicTags()));
        timedQuizProcedure.setTopicTypes(joinTopicTypes(request.getTopicTypes()));
        timedQuizProcedure.setQuizTimeLimit(request.getQuizTimeLimit());

        timedQuizProcedureMapper.insert(timedQuizProcedure);

        // 8. 如果是老师选定模式，创建题目映射记录
        if (!Boolean.TRUE.equals(request.getIsRandom())) {
            List<Long> topicIds = request.getTeacherSelectedTopicIds();

            if (topicIds != null && !topicIds.isEmpty()) {
                // 验证题目是否存在
                QueryWrapper<Topic> topicQueryWrapper = new QueryWrapper<>();
                topicQueryWrapper.in("id", topicIds);
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
                    topicMap.setProcedureTopicId(timedQuizProcedure.getId());
                    procedureTopicMapMapper.insert(topicMap);
                }
            }
        }

        // 9. 更新步骤的限时答题配置ID
        procedure.setTimedQuizId(timedQuizProcedure.getId());
        experimentalProcedureService.updateById(procedure);

        return procedure.getId();
    }

    /**
     * 删除指定步骤
     *
     * @param procedureId 步骤ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteProcedure(Long procedureId) {
        log.info("开始删除步骤，步骤ID: {}", procedureId);

        // 第一步：验证步骤是否存在
        ExperimentalProcedure procedure = experimentalProcedureService.getById(procedureId);
        if (procedure == null) {
            log.warn("步骤删除失败：步骤不存在, procedureId={}", procedureId);
            throw new com.example.demo.exception.BusinessException(404, "步骤不存在");
        }

        // 第二步：级联逻辑删除所有关联的子表数据
        cascadeDeleteRelatedData(procedureId);

        // 第三步：删除步骤主表（Mybatis-Plus自动处理is_deleted字段）
        boolean removed = experimentalProcedureService.removeById(procedureId);

        if (!removed) {
            log.error("步骤主表删除失败: procedureId={}", procedureId);
            throw new com.example.demo.exception.BusinessException(500, "步骤删除失败");
        }

        log.info("步骤删除成功，步骤ID: {}", procedureId);
    }

    /**
     * 级联删除步骤相关的所有子表数据
     *
     * @param procedureId 步骤ID
     */
    private void cascadeDeleteRelatedData(Long procedureId) {
        log.info("开始级联删除子表数据，步骤ID: {}", procedureId);

        // 1. 删除数据收集记录（通过 experimental_procedure_id 关联）
        deleteDataCollections(procedureId);

        // 2. 删除题库详情记录（通过 experimental_procedure_id 关联）
        deleteProcedureTopics(procedureId);

        // 3. 删除限时答题配置（通过 experimental_procedure_id 关联）
        deleteTimedQuizProcedures(procedureId);

        // 4. 删除题目映射关系（通过 experimental_procedure_id 关联）
        deleteProcedureTopicMaps(procedureId);

        log.info("子表数据级联删除完成，步骤ID: {}", procedureId);
    }

    /**
     * 删除数据收集记录
     *
     * @param procedureId 步骤ID
     */
    private void deleteDataCollections(Long procedureId) {
        // 使用UpdateWrapper批量更新is_deleted字段（Mybatis-Plus自动处理）
        UpdateWrapper<DataCollection> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("experimental_procedure_id", procedureId)
                   .set("is_deleted", 1);
        int rows = dataCollectionMapper.update(null, updateWrapper);
        if (rows > 0) {
            log.info("数据收集记录逻辑删除成功: procedureId={}, count={}", procedureId, rows);
        }
    }

    /**
     * 删除题库详情记录
     *
     * @param procedureId 步骤ID
     */
    private void deleteProcedureTopics(Long procedureId) {
        // 使用UpdateWrapper批量更新is_deleted字段（Mybatis-Plus自动处理）
        UpdateWrapper<ProcedureTopic> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("experimental_procedure_id", procedureId)
                   .set("is_deleted", 1);
        int rows = procedureTopicMapper.update(null, updateWrapper);
        if (rows > 0) {
            log.info("题库详情记录逻辑删除成功: procedureId={}, count={}", procedureId, rows);
        }
    }

    /**
     * 删除限时答题配置
     *
     * @param procedureId 步骤ID
     */
    private void deleteTimedQuizProcedures(Long procedureId) {
        // 使用UpdateWrapper批量更新is_deleted字段（Mybatis-Plus自动处理）
        UpdateWrapper<TimedQuizProcedure> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("experimental_procedure_id", procedureId)
                   .set("is_deleted", 1);
        int rows = timedQuizProcedureMapper.update(null, updateWrapper);
        if (rows > 0) {
            log.info("限时答题配置逻辑删除成功: procedureId={}, count={}", procedureId, rows);
        }
    }

    /**
     * 删除题目映射关系
     *
     * @param procedureId 步骤ID
     */
    private void deleteProcedureTopicMaps(Long procedureId) {
        // 使用UpdateWrapper批量更新is_deleted字段（Mybatis-Plus自动处理）
        UpdateWrapper<ProcedureTopicMap> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("experimental_procedure_id", procedureId)
                   .set("is_deleted", 1);
        int rows = procedureTopicMapMapper.update(null, updateWrapper);
        if (rows > 0) {
            log.info("题目映射关系逻辑删除成功: procedureId={}, count={}", procedureId, rows);
        }
    }
}
