package com.example.demo.pojo.request.student;

import com.example.demo.pojo.dto.mapvo.TopicAnswerItem;
import lombok.Data;
import java.util.List;

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
     * 题目答案列表
     */
    private List<TopicAnswerItem> answers;
}
