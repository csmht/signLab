package com.example.demo.pojo.request.teacher;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 创建题目请求
 */
@Data
public class CreateTopicRequest {

    /**
     * 题目类型：1-单选题，2-多选题，3-判断题，4-填空题，6-其他
     */
    @NotNull(message = "题目类型不能为空")
    private Integer type;

    /**
     * 题目内容
     */
    @NotBlank(message = "题目内容不能为空")
    private String content;

    /**
     * 选项内容
     * 格式："A:选项A$B:选项B"
     */
    private String choices;

    /**
     * 正确答案
     */
    @NotBlank(message = "正确答案不能为空")
    private String correctAnswer;

    /**
     * 标签ID列表（可包含难度标签、知识点标签等）
     */
    private List<Long> tagIds;
}
