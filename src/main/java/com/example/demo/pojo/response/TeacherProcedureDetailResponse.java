package com.example.demo.pojo.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 教师实验步骤详情响应
 */
@Data
public class TeacherProcedureDetailResponse {

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
     * 步骤类型（1-观看视频，2-数据收集，3-题库答题）
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

    // ===== 类型1：观看视频详细信息 =====

    /**
     * 视频ID（类型1时有效）
     */
    private Long videoId;

    /**
     * 视频标题（类型1时有效）
     */
    private String videoTitle;

    /**
     * 视频时长（秒）（类型1时有效）
     */
    private Long videoSeconds;

    /**
     * 视频文件路径（类型1时有效）
     */
    private String videoFilePath;

    /**
     * 视频文件大小（字节）（类型1时有效）
     */
    private Long videoFileSize;

    // ===== 类型2：数据收集详细信息 =====

    /**
     * 数据收集ID（类型2时有效）
     */
    private Long dataCollectionId;

    /**
     * 数据收集类型（1-关键数据，2-表格数据）（类型2时有效）
     */
    private Long dataCollectionType;

    /**
     * 数据描述（类型2时有效）
     */
    private String dataRemark;

    /**
     * 是否需要提交照片（类型2时有效）
     */
    private Boolean dataNeedPhoto;

    /**
     * 是否需要提交文档（类型2时有效）
     */
    private Boolean dataNeedDoc;

    // ===== 类型3：题库答题详细信息 =====

    /**
     * 题库详情ID（类型3时有效）
     */
    private Long procedureTopicId;

    /**
     * 是否随机抽取题目（类型3时有效）
     */
    private Boolean topicIsRandom;

    /**
     * 题目数量（类型3时有效）
     */
    private Integer topicNumber;

    /**
     * 标签限制列表（类型3、随机模式时有效）
     */
    private List<TagInfo> topicTags;

    /**
     * 题目ID列表（类型3、非随机模式时有效）
     */
    private List<Long> topicIds;

    /**
     * 随机抽题题型（类型3、随机模式时有效）
     */
    private String topicTypes;

    /**
     * 题目详情列表（类型3、非随机模式时有效）
     */
    private List<TopicDetail> topics;

    // ===== 类型5：限时答题详细信息 =====

    /**
     * 限时答题配置ID（类型5时有效）
     */
    private Long timedQuizId;

    /**
     * 是否随机抽取题目（类型5时有效）
     */
    private Boolean timedQuizIsRandom;

    /**
     * 题目数量（类型5时有效）
     */
    private Integer timedQuizNumber;

    /**
     * 答题时间限制（分钟）（类型5时有效）
     */
    private Integer timedQuizTimeLimit;

    /**
     * 标签限制列表（类型5、随机模式时有效）
     */
    private List<TagInfo> timedQuizTags;

    /**
     * 题目类型限制（类型5、随机模式时有效，逗号分隔）
     */
    private String timedQuizTopicTypes;

    /**
     * 题目详情列表（类型5、非随机模式时有效）
     */
    private List<TimedQuizTopicDetail> timedQuizTopics;

    /**
     * 题目详情
     */
    @Data
    public static class TopicDetail {
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
         * 正确答案
         */
        private String correctAnswer;
    }

    /**
     * 限时答题题目详情
     */
    @Data
    public static class TimedQuizTopicDetail {
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
         * 正确答案
         */
        private String correctAnswer;
    }

    /**
     * 标签信息
     */
    @Data
    public static class TagInfo {
        /**
         * 标签ID
         */
        private Long id;

        /**
         * 标签名称
         */
        private String tagName;

        /**
         * 标签类型
         */
        private String type;

        /**
         * 标签描述
         */
        private String description;
    }
}
