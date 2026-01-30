package com.example.demo.pojo.response;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 学生视频观看步骤详情响应
 * 用于教师查询学生的视频观看步骤详情
 */
@Data
public class StudentVideoProcedureDetailResponse {

    /**
     * 步骤ID
     */
    private Long id;

    /**
     * 步骤序号
     */
    private Integer number;

    /**
     * 步骤描述
     */
    private String remark;

    /**
     * 步骤分数占比
     */
    private Integer proportion;

    /**
     * 视频信息
     */
    private VideoDetail videoDetail;

    /**
     * 提交时间
     */
    private LocalDateTime submissionTime;

    /**
     * 得分
     */
    private java.math.BigDecimal score;

    /**
     * 教师评语
     */
    private String teacherComment;

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

        /**
         * 视频下载密钥（可选）
         */
        private String downloadKey;
    }
}
