package com.example.demo.pojo.response;

import lombok.Data;

import java.util.List;

/**
 * 学生步骤详情响应（未提交）
 */
@Data
public class StudentProcedureDetailWithoutAnswerResponse {

    /**
     * 步骤ID
     */
    private Long id;

    /**
     * 步骤序号
     */
    private Integer number;

    /**
     * 步骤类型（1-观看视频，2-数据收集，3-题库答题）
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
     * 视频详情（类型1）
     */
    private VideoDetail videoDetail;

    /**
     * 数据收集详情（类型2）
     */
    private DataCollectionDetail dataCollectionDetail;

    /**
     * 题库详情（类型3）
     */
    private TopicDetail topicDetail;

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

    /**
     * 数据收集详情
     */
    @Data
    public static class DataCollectionDetail {
        /**
         * 数据收集ID
         */
        private Long id;

        /**
         * 数据收集类型（1-关键数据，2-表格数据）
         */
        private Integer type;

        /**
         * 数据描述
         */
        private String remark;

        /**
         * 是否需要提交照片
         */
        private Boolean needPhoto;

        /**
         * 是否需要提交文档
         */
        private Boolean needDoc;
    }

    /**
     * 题库详情
     */
    @Data
    public static class TopicDetail {
        /**
         * 题库详情ID
         */
        private Long id;

        /**
         * 是否随机抽取题目
         */
        private Boolean isRandom;

        /**
         * 题目数量
         */
        private Integer number;

        /**
         * 标签限制
         */
        private String tags;

        /**
         * 题目列表（不含答案）
         */
        private List<TopicItem> topics;
    }

    /**
     * 题目项（不含答案）
     */
    @Data
    public static class TopicItem {
        /**
         * 题目ID
         */
        private Long id;

        /**
         * 题号
         */
        private Integer number;

        /**
         * 题目类型（1-单选题，2-多选题，3-判断题，4-填空题，5-简答题，6-其他）
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
    }
}
