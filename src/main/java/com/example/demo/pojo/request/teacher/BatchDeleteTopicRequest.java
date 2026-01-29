package com.example.demo.pojo.request.teacher;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 批量删除题目请求
 */
@Data
public class BatchDeleteTopicRequest {

    /**
     * 题目ID列表
     */
    @NotEmpty(message = "题目ID列表不能为空")
    private List<Long> topicIds;
}
