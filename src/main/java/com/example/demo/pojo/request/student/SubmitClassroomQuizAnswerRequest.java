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
     * 判断题标准值使用"正确"、"错误"（后端兼容旧值 A/B/T/F）
     */
    private List<TopicAnswerItem> answers;
}
