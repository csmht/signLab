package com.example.demo.pojo.request.student;

import com.example.demo.pojo.dto.mapvo.TopicAnswerItem;
import lombok.Data;

import java.util.List;

/**
 * 完成限时答题请求
 */
@Data
public class CompleteTimedQuizProcedureRequest {

    /** 步骤ID */
    private Long procedureId;

    /** 班级编号 */
    private String classCode;

    /** 密钥 */
    private String secretKey;

    /**
     * 题目答案列表
     * 判断题标准值使用"正确"、"错误"（后端兼容旧值 A/B/T/F）
     */
    private List<TopicAnswerItem> answers;
}
