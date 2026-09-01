package com.example.demo.service;

import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.DataCollectionMapper;
import com.example.demo.mapper.ProcedureTopicMapMapper;
import com.example.demo.mapper.ProcedureTopicMapper;
import com.example.demo.mapper.TimedQuizProcedureMapper;
import com.example.demo.mapper.TopicMapper;
import com.example.demo.pojo.entity.ExperimentalProcedure;
import com.example.demo.pojo.request.teacher.InsertTopicProcedureRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TeacherProcedureCreationServiceTest {

    @Mock
    private ExperimentalProcedureService experimentalProcedureService;
    @Mock
    private DataCollectionMapper dataCollectionMapper;
    @Mock
    private ProcedureTopicMapper procedureTopicMapper;
    @Mock
    private ProcedureTopicMapMapper procedureTopicMapMapper;
    @Mock
    private TimedQuizProcedureMapper timedQuizProcedureMapper;
    @Mock
    private TopicMapper topicMapper;

    @InjectMocks
    private TeacherProcedureCreationService teacherProcedureCreationService;

    @Test
    void shouldAllowEmptyTagsWhenInsertTopicProcedureIsNotRandom() {
        InsertTopicProcedureRequest request = buildBaseInsertRequest();
        request.setIsRandom(false);
        request.setTeacherSelectedTopicIds(List.of(101L));
        request.setTopicTags(null);

        when(experimentalProcedureService.getOne(any())).thenReturn(buildAfterProcedure());
        when(topicMapper.selectCount(any())).thenReturn(1L);

        assertDoesNotThrow(() -> teacherProcedureCreationService.insertTopicProcedure(request));
    }

    @Test
    void shouldThrowBusinessExceptionWhenInsertTopicProcedureIsRandomAndTagsAreEmpty() {
        InsertTopicProcedureRequest request = buildBaseInsertRequest();
        request.setIsRandom(true);
        request.setTopicNumber(5);
        request.setTopicTags(null);

        when(experimentalProcedureService.getOne(any())).thenReturn(buildAfterProcedure());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> teacherProcedureCreationService.insertTopicProcedure(request));

        assertEquals("随机抽题必须携带标签", ex.getMessage());
    }

    private InsertTopicProcedureRequest buildBaseInsertRequest() {
        InsertTopicProcedureRequest request = new InsertTopicProcedureRequest();
        request.setExperimentId(1L);
        request.setAfterNumber(1);
        request.setDurationMinutes(10);
        request.setOffsetMinutes(0);
        request.setRemark("test");
        request.setProportion(10);
        request.setIsSkip(false);
        return request;
    }

    private ExperimentalProcedure buildAfterProcedure() {
        ExperimentalProcedure procedure = new ExperimentalProcedure();
        procedure.setId(10L);
        procedure.setExperimentId(1L);
        procedure.setNumber(1);
        return procedure;
    }
}
