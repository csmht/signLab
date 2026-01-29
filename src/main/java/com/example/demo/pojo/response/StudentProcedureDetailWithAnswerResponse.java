package com.example.demo.pojo.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 学生步骤详情响应（已提交，带答案）
 */
@Data
public class StudentProcedureDetailWithAnswerResponse {

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
     * 是否已过答题时间
     * 如果true，表示当前时间已超过步骤答题时间，可以显示正确答案
     * 如果false，表示还在答题时间内，不显示正确答案
     */
    private Boolean isAfterEndTime;

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
         * 视频文件路径
         */
        private String filePath;

        /**
         * 视频文件大小（字节）
         */
        private Long fileSize;
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

        /**
         * 填空答案（key: 字段名, value: 答案值）
         */
        private Map<String, String> fillBlankAnswers;

        /**
         * 表格答案（key: 单元格位置, value: 答案值）
         */
        private Map<String, String> tableCellAnswers;

        /**
         * 提交的照片文件列表
         */
        private List<AttachmentInfo> photos;

        /**
         * 提交的文档文件列表
         */
        private List<AttachmentInfo> documents;

        /**
         * 正确答案（仅在当前时间超过步骤答题时间后才返回）
         * 格式：JSON字符串，包含fillBlankCorrectAnswer和tableCellCorrectAnswer
         */
        private String correctAnswer;
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
         * 题目列表（带答案）
         * 注意：correctAnswer和isCorrect字段仅在当前时间超过步骤答题时间后才返回
         */
        private List<TopicItem> topics;
    }

    /**
     * 题目项（带答案）
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

        /**
         * 学生答案
         */
        private String studentAnswer;

        /**
         * 正确答案（仅在当前时间超过步骤答题时间后才返回）
         */
        private String correctAnswer;

        /**
         * 是否正确（仅在当前时间超过步骤答题时间后才返回）
         */
        private Boolean isCorrect;

        /**
         * 得分
         */
        private BigDecimal score;
    }

    /**
     * 附件信息
     */
    @Data
    public static class AttachmentInfo {
        /**
         * 附件ID
         */
        private Long id;

        /**
         * 文件类型（1-照片，2-文档）
         */
        private Integer fileType;

        /**
         * 文件格式
         */
        private String fileFormat;

        /**
         * 原始文件名
         */
        private String originalFileName;

        /**
         * 文件大小（字节）
         */
        private Long fileSize;

        /**
         * 文件备注
         */
        private String remark;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;

        /**
         * 文件下载密钥
         */
        private String downloadKey;
    }
}
