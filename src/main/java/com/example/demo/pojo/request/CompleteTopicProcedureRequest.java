package com.example.demo.pojo.request;

import lombok.Data;

/**
 * 完成题库练习请求
 */
@Data
public class CompleteTopicProcedureRequest {

    /**
     * 实验步骤ID
     */
    private Long procedureId;

    /**
     * 班级编号
     */
    private String classCode;

    /**
     * 题目答案列表（JSON字符串格式）
     * key: 题目ID
     * value: 答案（根据题目类型不同格式不同）
     *   - 单选题/判断题：选项字母（如"A"、"B"）
     *   - 多选题：选项字母组合（如"A-B-C"）
     *   - 填空题：答案内容
     *   - 简答题：文字回答
     * 例如: {"1": "A", "2": "B-C", "3": "对"}
     */
    private String answers;
}
