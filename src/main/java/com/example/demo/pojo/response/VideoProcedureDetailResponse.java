package com.example.demo.pojo.response;

import lombok.Data;

/**
 * 视频步骤详情响应（未提交）
 * 步骤类型：type=1（观看视频）
 */
@Data
public class VideoProcedureDetailResponse {

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
         * 视频播放密钥
         */
        private String playKey;
    }
}
