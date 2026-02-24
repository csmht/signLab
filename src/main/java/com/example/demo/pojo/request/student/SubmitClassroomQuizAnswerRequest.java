package com.example.demo.pojo.request.student;

import com.example.demo.pojo.dto.mapvo.TopicAnswerItem;
import lombok.Data;

import java.util.List;

/**
 * 提交课堂小测答案请求
 */
@Data
public class SubmitClassroomQuizAnswerRequest {

    /**
     * 课堂小测ID
     */
    private Long quizId;

    /**
     * 答案内容列表
     */
    private List<TopicAnswerItem> answers;
}
