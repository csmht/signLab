package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.TopicMapper;
import com.example.demo.pojo.entity.Tag;
import com.example.demo.pojo.entity.Topic;
import com.example.demo.pojo.entity.TopicTagMap;
import com.example.demo.pojo.request.TopicQueryRequest;
import com.example.demo.pojo.request.teacher.CreateTopicRequest;
import com.example.demo.pojo.request.teacher.UpdateTopicRequest;
import com.example.demo.pojo.response.PageResponse;
import com.example.demo.pojo.response.TopicDetailResponse;
import com.example.demo.pojo.response.TopicResponse;
import com.example.demo.pojo.response.TopicStatisticsResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 题目服务
 * 提供题目的业务逻辑处理
 */
@Slf4j
@Service
public class TopicService extends ServiceImpl<TopicMapper, Topic> {

    @Autowired
    private TopicTagMapService topicTagMapService;

    @Autowired
    private TagService tagService;

    /**
     * 创建题目（含标签关联）
     *
     * @param request 创建题目请求
     * @param createdBy 创建者用户名
     * @return 题目ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createTopic(CreateTopicRequest request, String createdBy) {
        // 1. 创建题目
        Topic topic = new Topic();
        topic.setType(request.getType());
        topic.setContent(request.getContent());
        topic.setChoices(request.getChoices());
        topic.setCorrectAnswer(request.getCorrectAnswer());
        topic.setCreatedBy(createdBy);
        topic.setCreatedTime(LocalDateTime.now());
        topic.setUpdatedTime(LocalDateTime.now());
        topic.setIsDeleted(false);

        save(topic);

        // 2. 关联标签
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            topicTagMapService.addTagsToTopic(topic.getId(), request.getTagIds());
        }

        log.info("创建题目成功，题目ID：{}，创建者：{}", topic.getId(), createdBy);
        return topic.getId();
    }

    /**
     * 更新题目（含标签关联）
     *
     * @param request 更新题目请求
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateTopic(UpdateTopicRequest request) {
        // 1. 查询题目是否存在
        Topic topic = getById(request.getId());
        if (topic == null || topic.getIsDeleted()) {
            throw new BusinessException(404, "题目不存在");
        }

        // 2. 更新题目基础信息
        if (request.getType() != null) {
            topic.setType(request.getType());
        }
        if (request.getContent() != null) {
            topic.setContent(request.getContent());
        }
        if (request.getChoices() != null) {
            topic.setChoices(request.getChoices());
        }
        if (request.getCorrectAnswer() != null) {
            topic.setCorrectAnswer(request.getCorrectAnswer());
        }
        topic.setUpdatedTime(LocalDateTime.now());

        updateById(topic);

        // 3. 更新标签关联（如果提供了tagIds）
        if (request.getTagIds() != null) {
            // 先删除旧关联
            topicTagMapService.removeAllTagsFromTopic(topic.getId());
            // 再添加新关联
            if (!request.getTagIds().isEmpty()) {
                topicTagMapService.addTagsToTopic(topic.getId(), request.getTagIds());
            }
        }

        log.info("更新题目成功，题目ID：{}", request.getId());
    }

    /**
     * 删除题目（软删除，校验创建者）
     *
     * @param topicId 题目ID
     * @param username 当前用户名
     */
    public void deleteTopic(Long topicId, String username) {
        Topic topic = getById(topicId);
        if (topic == null || topic.getIsDeleted()) {
            throw new BusinessException(404, "题目不存在");
        }

        // 校验创建者权限
        if (!username.equals(topic.getCreatedBy())) {
            throw new BusinessException(403, "只能删除自己创建的题目");
        }

        // 软删除
        topic.setIsDeleted(true);
        topic.setUpdatedTime(LocalDateTime.now());
        updateById(topic);

        log.info("删除题目成功，题目ID：{}，操作者：{}", topicId, username);
    }

    /**
     * 批量软删除题目（校验创建者）
     *
     * @param topicIds 题目ID列表
     * @param username 当前用户名
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteTopics(List<Long> topicIds, String username) {
        for (Long topicId : topicIds) {
            deleteTopic(topicId, username);
        }
        log.info("批量删除题目成功，数量：{}，操作者：{}", topicIds.size(), username);
    }

    /**
     * 分页查询题目（多条件筛选）
     *
     * @param request 查询请求
     * @return 分页结果
     */
    public PageResponse<TopicDetailResponse> queryTopics(TopicQueryRequest request) {
        // 1. 构建查询条件
        QueryWrapper<Topic> wrapper = buildQueryWrapper(request);

        // 2. 分页查询
        Page<Topic> page = page(new Page<>(request.getCurrent(), request.getSize()), wrapper);

        // 3. 查询每个题目的标签
        List<TopicDetailResponse> records = page.getRecords().stream()
                .map(this::convertToDetailResponse)
                .collect(Collectors.toList());

        return PageResponse.of(page.getCurrent(), page.getSize(), page.getTotal(), records);
    }

    /**
     * 根据ID查询题目详情（含标签）
     *
     * @param topicId 题目ID
     * @return 题目详情
     */
    public TopicDetailResponse getTopicDetail(Long topicId) {
        Topic topic = getById(topicId);
        if (topic == null || topic.getIsDeleted()) {
            throw new BusinessException(404, "题目不存在");
        }

        return convertToDetailResponse(topic);
    }

    /**
     * 获取题目统计信息
     *
     * @return 统计信息
     */
    public TopicStatisticsResponse getStatistics() {
        TopicStatisticsResponse response = new TopicStatisticsResponse();

        // 1. 总题目数
        QueryWrapper<Topic> wrapper = new QueryWrapper<>();
        wrapper.eq("is_deleted", false);
        Long totalCount = count(wrapper);
        response.setTotalCount(totalCount);

        // 2. 按题型统计
        Map<Integer, Long> typeCount = new HashMap<>();
        for (int i = 1; i <= 6; i++) {
            QueryWrapper<Topic> typeWrapper = new QueryWrapper<>();
            typeWrapper.eq("is_deleted", false);
            typeWrapper.eq("type", i);
            long count = count(typeWrapper);
            if (count > 0) {
                typeCount.put(i, count);
            }
        }
        response.setTypeCount(typeCount);

        // 3. 按标签统计（需要通过TopicTagMap关联查询）
        List<Tag> allTags = tagService.list();
        Map<String, Long> subjectTagCount = new HashMap<>();
        Map<String, Long> difficultyTagCount = new HashMap<>();
        Map<String, Long> customTagCount = new HashMap<>();

        for (Tag tag : allTags) {
            // 查询该标签下的题目数量
            List<TopicTagMap> mappings = topicTagMapService.getByTagId(tag.getId());
            long count = mappings.stream()
                    .map(TopicTagMap::getTopicId)
                    .distinct()
                    .filter(topicId -> {
                        Topic t = getById(topicId);
                        return t != null && !t.getIsDeleted();
                    })
                    .count();

            if (count > 0) {
                switch (tag.getType()) {
                    case "1": // 学科标签
                        subjectTagCount.put(tag.getTagName(), count);
                        break;
                    case "2": // 难度标签
                        difficultyTagCount.put(tag.getTagName(), count);
                        break;
                    case "4": // 自定义标签
                        customTagCount.put(tag.getTagName(), count);
                        break;
                }
            }
        }

        response.setSubjectTagCount(subjectTagCount);
        response.setDifficultyTagCount(difficultyTagCount);
        response.setCustomTagCount(customTagCount);

        return response;
    }

    /**
     * 根据标签ID列表查询题目
     *
     * @param tagIds 标签ID列表
     * @param limit 限制数量
     * @return 题目列表
     */
    public List<Topic> getTopicsByTagIds(List<Long> tagIds, Integer limit) {
        // 查询包含任一标签的题目ID
        Set<Long> topicIds = new HashSet<>();
        for (Long tagId : tagIds) {
            List<TopicTagMap> mappings = topicTagMapService.getByTagId(tagId);
            mappings.forEach(m -> topicIds.add(m.getTopicId()));
        }

        // 查询题目详情
        if (topicIds.isEmpty()) {
            return new ArrayList<>();
        }

        QueryWrapper<Topic> wrapper = new QueryWrapper<>();
        wrapper.in("id", topicIds);
        wrapper.eq("is_deleted", false);
        wrapper.last(limit != null ? "LIMIT " + limit : "");

        return list(wrapper);
    }

    /**
     * 随机查询题目
     *
     * @param type 题型（可选）
     * @param tagIds 标签ID列表（可选）
     * @param count 数量
     * @return 题目列表
     */
    public List<Topic> getRandomTopics(Integer type, List<Long> tagIds, Integer count) {
        QueryWrapper<Topic> wrapper = new QueryWrapper<>();
        wrapper.eq("is_deleted", false);

        if (type != null) {
            wrapper.eq("type", type);
        }

        // 如果指定了标签，查询包含这些标签的题目
        if (tagIds != null && !tagIds.isEmpty()) {
            Set<Long> topicIds = new HashSet<>();
            for (Long tagId : tagIds) {
                List<TopicTagMap> mappings = topicTagMapService.getByTagId(tagId);
                mappings.forEach(m -> topicIds.add(m.getTopicId()));
            }

            if (topicIds.isEmpty()) {
                return new ArrayList<>();
            }

            wrapper.in("id", topicIds);
        }

        // 随机排序并限制数量
        wrapper.orderByAsc("RAND()");
        if (count != null && count > 0) {
            wrapper.last("LIMIT " + count);
        }

        return list(wrapper);
    }

    /**
     * 构建查询条件
     */
    private QueryWrapper<Topic> buildQueryWrapper(TopicQueryRequest request) {
        QueryWrapper<Topic> wrapper = new QueryWrapper<>();
        wrapper.eq("is_deleted", false);

        if (request.getType() != null) {
            wrapper.eq("type", request.getType());
        }

        if (request.getKeyword() != null && !request.getKeyword().trim().isEmpty()) {
            wrapper.like("content", request.getKeyword().trim());
        }

        if (request.getCreatedBy() != null) {
            wrapper.eq("created_by", request.getCreatedBy());
        }

        // 标签筛选（需要通过TopicTagMap关联查询）
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            // 查询包含这些标签的题目ID
            Set<Long> topicIds = new HashSet<>();
            for (Long tagId : request.getTagIds()) {
                List<TopicTagMap> mappings = topicTagMapService.getByTagId(tagId);
                mappings.forEach(m -> topicIds.add(m.getTopicId()));
            }

            if (!topicIds.isEmpty()) {
                wrapper.in("id", topicIds);
            } else {
                // 如果没有匹配的题目，返回空结果
                wrapper.eq("id", -1);
            }
        }

        // 难度标签筛选
        if (request.getDifficultyTagIds() != null && !request.getDifficultyTagIds().isEmpty()) {
            Set<Long> topicIds = new HashSet<>();
            for (Long tagId : request.getDifficultyTagIds()) {
                List<TopicTagMap> mappings = topicTagMapService.getByTagId(tagId);
                mappings.forEach(m -> topicIds.add(m.getTopicId()));
            }

            if (!topicIds.isEmpty()) {
                wrapper.in("id", topicIds);
            }
        }

        // 学科标签筛选
        if (request.getSubjectTagIds() != null && !request.getSubjectTagIds().isEmpty()) {
            Set<Long> topicIds = new HashSet<>();
            for (Long tagId : request.getSubjectTagIds()) {
                List<TopicTagMap> mappings = topicTagMapService.getByTagId(tagId);
                mappings.forEach(m -> topicIds.add(m.getTopicId()));
            }

            if (!topicIds.isEmpty()) {
                wrapper.in("id", topicIds);
            }
        }

        wrapper.orderByDesc("created_time");
        return wrapper;
    }

    /**
     * 转换为详情响应
     */
    private TopicDetailResponse convertToDetailResponse(Topic topic) {
        TopicDetailResponse response = new TopicDetailResponse();
        response.setId(topic.getId());
        response.setType(topic.getType());
        response.setContent(topic.getContent());
        response.setChoices(topic.getChoices());
        response.setCorrectAnswer(topic.getCorrectAnswer());
        response.setCreatedBy(topic.getCreatedBy());
        response.setCreatedTime(topic.getCreatedTime());
        response.setUpdatedTime(topic.getUpdatedTime());
        response.setTypeName(TopicResponse.getTypeName(topic.getType()));

        // 查询标签
        List<TopicTagMap> tagMaps = topicTagMapService.getByTopicId(topic.getId());
        List<TopicDetailResponse.TagInfo> tags = new ArrayList<>();
        for (TopicTagMap tagMap : tagMaps) {
            Tag tag = tagService.getById(tagMap.getTagId());
            if (tag != null) {
                TopicDetailResponse.TagInfo tagInfo = new TopicDetailResponse.TagInfo();
                tagInfo.setTagId(tag.getId());
                tagInfo.setTagName(tag.getTagName());
                tagInfo.setTagType(tag.getType());
                tagInfo.setDescription(tag.getDescription());
                tags.add(tagInfo);
            }
        }
        response.setTags(tags);

        return response;
    }
}