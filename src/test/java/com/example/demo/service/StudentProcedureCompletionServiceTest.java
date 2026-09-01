package com.example.demo.service;

import com.example.demo.mapper.ClassExperimentClassRelationMapper;
import com.example.demo.mapper.ClassExperimentMapper;
import com.example.demo.mapper.DataCollectionMapper;
import com.example.demo.mapper.ProcedureTopicMapMapper;
import com.example.demo.mapper.ProcedureTopicMapper;
import com.example.demo.mapper.TimedQuizProcedureMapper;
import com.example.demo.mapper.TopicMapper;
import com.example.demo.pojo.entity.DataCollection;
import com.example.demo.pojo.entity.ExperimentalProcedure;
import com.example.demo.pojo.entity.StudentExperimentalProcedure;
import com.example.demo.util.AnswerMapJSONUntil;
import com.example.demo.util.TimedQuizKeyGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentProcedureCompletionServiceTest {

    @Mock
    private StudentExperimentalProcedureService studentExperimentalProcedureService;
    @Mock
    private ExperimentalProcedureService experimentalProcedureService;
    @Mock
    private ProcedureTopicMapper procedureTopicMapper;
    @Mock
    private ProcedureTopicMapMapper procedureTopicMapMapper;
    @Mock
    private DataCollectionMapper dataCollectionMapper;
    @Mock
    private TimedQuizProcedureMapper timedQuizProcedureMapper;
    @Mock
    private TopicMapper topicMapper;
    @Mock
    private TimedQuizKeyGenerator timedQuizKeyGenerator;
    @Mock
    private ClassExperimentMapper classExperimentMapper;
    @Mock
    private ClassExperimentClassRelationMapper classExperimentClassRelationMapper;

    @InjectMocks
    private StudentProcedureCompletionService service;

    @Test
    void tableGradingIgnoresInjectedFillBlankAnswersAndRangeEncoding() {
        StudentExperimentalProcedure submission = new StudentExperimentalProcedure();
        submission.setId(100L);
        submission.setExperimentalProcedureId(10L);
        submission.setIsGraded(0);
        submission.setAnswer(AnswerMapJSONUntil.toDataCollectionJson(
                Map.of("0-0", "4"),
                Map.of("0-0", "4")));

        ExperimentalProcedure procedure = new ExperimentalProcedure();
        procedure.setId(10L);
        procedure.setType(2);
        procedure.setIsDeleted(false);

        DataCollection dataCollection = new DataCollection();
        dataCollection.setType(2L);
        dataCollection.setRemark("{}");
        dataCollection.setCorrectAnswer("{\"0-0\":\"RANGE|3|5\"}");

        when(studentExperimentalProcedureService.getById(100L)).thenReturn(submission);
        when(studentExperimentalProcedureService.updateById(any())).thenReturn(true);
        when(experimentalProcedureService.getById(10L)).thenReturn(procedure);
        when(dataCollectionMapper.selectOne(any())).thenReturn(dataCollection);

        StudentProcedureCompletionService.AutoGradeExecutionResult result =
                service.autoGradeExistingDataCollectionSubmission(100L);

        assertTrue(result.isSuccess());
        assertEquals(0, submission.getScore().compareTo(new BigDecimal("0.00")));
    }
}
