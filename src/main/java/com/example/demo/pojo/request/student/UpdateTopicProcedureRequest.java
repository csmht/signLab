package com.example.demo.pojo.request.student;

import lombok.Data;
import java.util.Map;

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
     * 题目答案Map（题目ID -> 答案）
     */
    private Map<Long, String> answers;
}
