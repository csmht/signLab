package com.example.demo.service;

import com.example.demo.enums.TagType;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.TagMapper;
import com.example.demo.mapper.TopicTagMapMapper;
import com.example.demo.pojo.entity.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 标签匹配服务
 * 随机抽题时按标签类型分组：类型之间取交集，类型内取并集
 */
@Service
@RequiredArgsConstructor
public class TopicTagMatchService {

    private final TagMapper tagMapper;
    private final TopicTagMapMapper topicTagMapMapper;

    /**
     * 根据标签ID列表查询符合“类型间与、类型内或”规则的题目ID
     *
     * @param tagIds 标签ID列表
     * @return 题目ID列表
     */
    public List<Long> selectTopicIdsByGroupedTags(List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> distinctTagIds = tagIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (distinctTagIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Tag> tags = tagMapper.selectBatchIds(distinctTagIds);
        if (tags == null || tags.isEmpty() || tags.size() != distinctTagIds.size()) {
            throw new BusinessException(400, "部分标签不存在，无法完成随机抽题");
        }

        Map<TagType, List<Long>> tagIdsByType = buildTagIdsByType(tags);
        if (tagIdsByType.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> matchedTopicIds = null;
        for (List<Long> sameTypeTagIds : tagIdsByType.values()) {
            if (sameTypeTagIds == null || sameTypeTagIds.isEmpty()) {
                continue;
            }
            List<Long> currentTypeTopicIds = topicTagMapMapper.selectDistinctTopicIdsByAnyTags(sameTypeTagIds);
            if (currentTypeTopicIds == null || currentTypeTopicIds.isEmpty()) {
                return Collections.emptyList();
            }
            if (matchedTopicIds == null) {
                matchedTopicIds = new LinkedHashSet<>(currentTypeTopicIds);
            } else {
                matchedTopicIds.retainAll(currentTypeTopicIds);
                if (matchedTopicIds.isEmpty()) {
                    return Collections.emptyList();
                }
            }
        }

        if (matchedTopicIds == null || matchedTopicIds.isEmpty()) {
            return Collections.emptyList();
        }
        return new ArrayList<>(matchedTopicIds);
    }

    public List<Long> selectTopicIdsByAllTags(List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> distinctTagIds = tagIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (distinctTagIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Tag> tags = tagMapper.selectBatchIds(distinctTagIds);
        if (tags == null || tags.isEmpty() || tags.size() != distinctTagIds.size()) {
            throw new BusinessException(400, "部分标签不存在，无法完成随机抽题");
        }
        validateTags(tags);
        return topicTagMapMapper.selectTopicIdsByAllTags(distinctTagIds, distinctTagIds.size());
    }

    private Map<TagType, List<Long>> buildTagIdsByType(List<Tag> tags) {
        validateTags(tags);

        Map<TagType, List<Long>> tagIdsByType = new LinkedHashMap<>();
        for (Tag tag : tags) {
            TagType tagType = TagType.fromCode(tag.getType());
            tagIdsByType.computeIfAbsent(tagType, key -> new ArrayList<>()).add(tag.getId());
        }
        return tagIdsByType;
    }

    private void validateTags(List<Tag> tags) {
        for (Tag tag : tags) {
            if (tag == null || tag.getId() == null) {
                throw new BusinessException(400, "标签数据异常，无法完成随机抽题");
            }
            if (tag.getType() == null || tag.getType().isEmpty()) {
                throw new BusinessException(400, "标签类型缺失，无法完成随机抽题");
            }
            try {
                TagType.fromCode(tag.getType());
            } catch (IllegalArgumentException ex) {
                throw new BusinessException(400, "标签类型非法，无法完成随机抽题");
            }
        }
    }
}
