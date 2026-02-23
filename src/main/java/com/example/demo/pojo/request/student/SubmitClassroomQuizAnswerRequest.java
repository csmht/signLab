package com.example.demo.pojo.request.student;

import lombok.Data;

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
     * 答案内容（JSON字符串格式）
     * 格式: {"topicId": "answer"}
     * 例如: {"1": "A", "2": "B-C", "3": "对"}
     */
    private String answers;
}
