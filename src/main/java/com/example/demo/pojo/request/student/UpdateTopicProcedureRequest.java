package com.example.demo.pojo.request.student;

import lombok.Data;

/**
 * 更新题库练习请求
 */
@Data
public class UpdateTopicProcedureRequest {
    /**
     * 实验步骤ID
     */
    private Long procedureId;

    /**
     * 班级编号
     */
    private String classCode;

    /**
     * 题目答案（JSON字符串格式，题目ID -> 答案）
     * 例如: {"1": "A", "2": "B-C", "3": "对"}
     */
    private String answers;
}
