package com.example.demo.pojo.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 题目基础响应
 */
@Data
public class TopicResponse {

    /**
     * 题目ID
     */
    private Long id;

    /**
     * 题目类型：1-单选题，2-多选题，3-判断题，4-填空题，6-其他
     */
    private Integer type;

    /**
     * 题目内容
     */
    private String content;

    /**
     * 选项内容
     */
    private String choices;

    /**
     * 正确答案（学生端不返回）
     */
    private String correctAnswer;

    /**
     * 创建者用户名
     */
    private String createdBy;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;

    /**
     * 题型名称（如"单选题"）
     */
    private String typeName;

    /**
     * 获取题型名称
     */
    public static String getTypeName(Integer type) {
        if (type == null) {
            return "未知";
        }
        switch (type) {
            case 1:
                return "单选题";
            case 2:
                return "多选题";
            case 3:
                return "判断题";
            case 4:
                return "填空题";
            case 5:
                return "简答题";
            case 6:
                return "其他";
            default:
                return "未知";
        }
    }
}
