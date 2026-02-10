package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.mapper.TopicTagMapMapper;
import com.example.demo.pojo.entity.TopicTagMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 标签题目映射表服务
 * 提供标签与题目映射关系的业务逻辑处理
 */
@Slf4j
@Service
public class TopicTagMapService extends ServiceImpl<TopicTagMapMapper, TopicTagMap> {

    /**
     * 根据题目ID查询标签映射列表
     * @param topicId 题目ID
     * @return 标签映射列表
     */
    public List<TopicTagMap> getByTopicId(Long topicId) {
        LambdaQueryWrapper<TopicTagMap> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TopicTagMap::getTopicId, topicId);
        return list(queryWrapper);
    }

    /**
     * 根据标签ID查询题目映射列表
     * @param tagId 标签ID
     * @return 题目映射列表
     */
    public List<TopicTagMap> getByTagId(Long tagId) {
        LambdaQueryWrapper<TopicTagMap> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TopicTagMap::getTagId, tagId);
        return list(queryWrapper);
    }

    /**
     * 为题目添加标签
     * @param topicId 题目ID
     * @param tagId 标签ID
     * @return 是否添加成功
     */
    public boolean addTagToTopic(Long topicId, Long tagId) {
        LambdaQueryWrapper<TopicTagMap> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TopicTagMap::getTopicId, topicId).eq(TopicTagMap::getTagId, tagId);
        if (getOne(queryWrapper) != null) {
            return true; // 已存在
        }

        TopicTagMap topicTagMap = new TopicTagMap();
        topicTagMap.setTopicId(topicId);
        topicTagMap.setTagId(tagId);
        return save(topicTagMap);
    }

    /**
     * 为题目批量添加标签
     * @param topicId 题目ID
     * @param tagIds 标签ID列表
     * @return 是否添加成功
     */
    public boolean addTagsToTopic(Long topicId, List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return true;
        }

        for (Long tagId : tagIds) {
            addTagToTopic(topicId, tagId);
        }
        return true;
    }

    /**
     * 移除题目的指定标签
     * @param topicId 题目ID
     * @param tagId 标签ID
     * @return 是否移除成功
     */
    public boolean removeTagFromTopic(Long topicId, Long tagId) {
        LambdaQueryWrapper<TopicTagMap> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TopicTagMap::getTopicId, topicId).eq(TopicTagMap::getTagId, tagId);
        return remove(queryWrapper);
    }

    /**
     * 移除题目的所有标签
     * @param topicId 题目ID
     * @return 是否移除成功
     */
    public boolean removeAllTagsFromTopic(Long topicId) {
        LambdaQueryWrapper<TopicTagMap> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TopicTagMap::getTopicId, topicId);
        return remove(queryWrapper);
    }

    /**
     * 移除标签的所有关联（删除标签时使用）
     * @param tagId 标签ID
     * @return 是否移除成功
     */
    public boolean removeByTagId(Long tagId) {
        LambdaQueryWrapper<TopicTagMap> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TopicTagMap::getTagId, tagId);
        return remove(queryWrapper);
    }
}