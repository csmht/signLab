package com.example.demo.service;

import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.TagMapper;
import com.example.demo.mapper.TopicTagMapMapper;
import com.example.demo.pojo.entity.Tag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TopicTagMatchServiceTest {

    @Mock
    private TagMapper tagMapper;

    @Mock
    private TopicTagMapMapper topicTagMapMapper;

    @InjectMocks
    private TopicTagMatchService topicTagMatchService;

    private Tag subjectTag1;
    private Tag subjectTag2;
    private Tag difficultyTag;

    @BeforeEach
    void setUp() {
        subjectTag1 = buildTag(1L, "1");
        subjectTag2 = buildTag(2L, "1");
        difficultyTag = buildTag(3L, "2");
    }

    @Test
    void shouldMatchGroupedTagsByIntersectionAcrossTypesAndUnionWithinType() {
        when(tagMapper.selectBatchIds(List.of(1L, 2L, 3L)))
                .thenReturn(List.of(subjectTag1, subjectTag2, difficultyTag));
        when(topicTagMapMapper.selectDistinctTopicIdsByAnyTags(List.of(1L, 2L)))
                .thenReturn(List.of(10L, 11L, 12L));
        when(topicTagMapMapper.selectDistinctTopicIdsByAnyTags(List.of(3L)))
                .thenReturn(List.of(11L, 12L, 13L));

        List<Long> result = topicTagMatchService.selectTopicIdsByGroupedTags(List.of(1L, 2L, 3L));

        assertEquals(List.of(11L, 12L), result);
    }

    @Test
    void shouldDeduplicateTagIdsBeforeQuerying() {
        when(tagMapper.selectBatchIds(List.of(1L, 3L)))
                .thenReturn(List.of(subjectTag1, difficultyTag));
        when(topicTagMapMapper.selectDistinctTopicIdsByAnyTags(List.of(1L)))
                .thenReturn(List.of(100L, 101L));
        when(topicTagMapMapper.selectDistinctTopicIdsByAnyTags(List.of(3L)))
                .thenReturn(List.of(101L));

        List<Long> result = topicTagMatchService.selectTopicIdsByGroupedTags(java.util.Arrays.asList(1L, 1L, 3L, null));

        assertEquals(List.of(101L), result);
        verify(tagMapper).selectBatchIds(List.of(1L, 3L));
    }

    @Test
    void shouldQueryAllTagsWithDeduplicatedIds() {
        when(tagMapper.selectBatchIds(List.of(1L, 2L, 3L)))
                .thenReturn(List.of(subjectTag1, subjectTag2, difficultyTag));
        when(topicTagMapMapper.selectTopicIdsByAllTags(List.of(1L, 2L, 3L), 3))
                .thenReturn(List.of(200L, 201L));

        List<Long> result = topicTagMatchService.selectTopicIdsByAllTags(List.of(1L, 2L, 3L, 2L));

        assertEquals(List.of(200L, 201L), result);
    }

    @Test
    void shouldThrowWhenSomeTagsAreMissing() {
        when(tagMapper.selectBatchIds(List.of(1L, 2L)))
                .thenReturn(List.of(subjectTag1));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> topicTagMatchService.selectTopicIdsByGroupedTags(List.of(1L, 2L)));

        assertEquals("部分标签不存在，无法完成随机抽题", ex.getMessage());
        verify(topicTagMapMapper, never()).selectDistinctTopicIdsByAnyTags(anyList());
    }

    @Test
    void shouldThrowWhenTagTypeIsBlank() {
        Tag invalidTag = buildTag(9L, "");
        when(tagMapper.selectBatchIds(List.of(9L)))
                .thenReturn(List.of(invalidTag));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> topicTagMatchService.selectTopicIdsByGroupedTags(List.of(9L)));

        assertEquals("标签类型缺失，无法完成随机抽题", ex.getMessage());
    }

    @Test
    void shouldThrowWhenTagTypeIsUnknown() {
        Tag invalidTag = buildTag(9L, "9");
        when(tagMapper.selectBatchIds(List.of(9L)))
                .thenReturn(List.of(invalidTag));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> topicTagMatchService.selectTopicIdsByAllTags(List.of(9L)));

        assertEquals("标签类型非法，无法完成随机抽题", ex.getMessage());
        verify(topicTagMapMapper, never()).selectTopicIdsByAllTags(eq(List.of(9L)), eq(1));
    }

    private Tag buildTag(Long id, String type) {
        Tag tag = new Tag();
        tag.setId(id);
        tag.setType(type);
        return tag;
    }
}
