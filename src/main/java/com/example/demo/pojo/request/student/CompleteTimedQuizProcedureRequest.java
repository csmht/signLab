package com.example.demo.pojo.request.student;

import lombok.Data;
import java.util.Map;

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

    /** 题目答案Map（题目ID -> 答案） */
    private Map<Long, String> answers;
}
