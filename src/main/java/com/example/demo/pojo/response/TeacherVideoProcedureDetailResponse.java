package com.example.demo.pojo.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 教师视频步骤详情响应
 * 步骤类型：type=1（观看视频）
 */
@Data
public class TeacherVideoProcedureDetailResponse {

    /**
     * 步骤ID
     */
    private Long id;

    /**
     * 实验ID
     */
    private Long experimentId;

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
     * 是否可跳过
     */
    private Boolean isSkip;

    /**
     * 步骤分数占比
     */
    private Integer proportion;

    /**
     * 步骤开始时间偏移量(分钟)
     */
    private Integer offsetMinutes;

    /**
     * 步骤持续时间(分钟)
     */
    private Integer durationMinutes;

    /**
     * 视频ID
     */
    private Long videoId;

    /**
     * 视频标题
     */
    private String videoTitle;

    /**
     * 视频时长（秒）
     */
    private Long videoSeconds;

    /**
     * 视频文件路径
     */
    private String videoFilePath;

    /**
     * 视频文件大小（字节）
     */
    private Long videoFileSize;
}
