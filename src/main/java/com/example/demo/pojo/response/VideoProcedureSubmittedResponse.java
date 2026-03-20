package com.example.demo.pojo.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 视频步骤详情响应（已提交）
 * 步骤类型：type=1（观看视频）
 */
@Data
public class VideoProcedureSubmittedResponse {

    /**
     * 步骤ID
     */
    private Long id;

    /**
     * 步骤序号
     */
    private Integer number;

    /**
     * 步骤类型（1-观看视频）
     */
    private Integer type;

    /**
     * 步骤描述
     */
    private String remark;

    /**
     * 步骤分数占比
     */
    private Integer proportion;

    /**
     * 是否可修改
     * true-可以修改，false-不可修改
     */
    private Boolean isModifiable;

    /**
     * 不可修改的原因（当isModifiable为false时返回）
     */
    private String notModifiableReason;

    /**
     * 提交时间
     */
    private LocalDateTime submissionTime;

    /**
     * 得分
     */
    private BigDecimal score;

    /**
     * 教师评语
     */
    private String teacherComment;

    /**
     * 评分状态（0-未评分，1-教师人工评分，2-系统自动评分）
     */
    private Integer isGraded;

    /**
     * 视频详情
     */
    private VideoDetail videoDetail;

    /**
     * 视频详情
     */
    @Data
    public static class VideoDetail {
        /**
         * 视频ID
         */
        private Long id;

        /**
         * 视频标题
         */
        private String title;

        /**
         * 视频时长（秒）
         */
        private Long seconds;

        /**
         * 视频文件路径
         */
        private String filePath;

        /**
         * 视频文件大小（字节）
         */
        private Long fileSize;
    }
}
