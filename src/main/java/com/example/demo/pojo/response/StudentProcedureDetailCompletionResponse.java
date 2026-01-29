package com.example.demo.pojo.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 学生指定步骤完成详情响应
 */
@Data
public class StudentProcedureDetailCompletionResponse {

    /**
     * 学生用户名
     */
    private String studentUsername;

    /**
     * 班级编号
     */
    private String classCode;

    /**
     * 步骤ID
     */
    private Long procedureId;

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
     * 是否已完成
     */
    private Boolean isCompleted;

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

    // ===== 类型2：数据收集提交内容 =====

    /**
     * 文本答案（类型2、3有效）
     */
    private String answer;

    /**
     * 提交的照片列表（类型2有效）
     */
    private List<AttachmentInfo> photos;

    /**
     * 提交的文档列表（类型2有效）
     */
    private List<AttachmentInfo> documents;

    // ===== 类型3：题库答题提交内容 =====

    /**
     * 题目答案列表（类型3有效）
     */
    private List<TopicAnswer> topicAnswers;

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
         * 文件格式
         */
        private String fileFormat;

        /**
         * 原始文件名
         */
        private String originalFileName;

        /**
         * 存储文件名
         */
        private String storedFileName;

        /**
         * 文件路径
         */
        private String filePath;

        /**
         * 文件大小（字节）
         */
        private Long fileSize;

        /**
         * 上传时间
         */
        private LocalDateTime uploadTime;

        /**
         * 文件下载密钥
         */
        private String downloadKey;
    }

    /**
     * 题目答案
     */
    @Data
    public static class TopicAnswer {
        /**
         * 题目ID
         */
        private Long topicId;

        /**
         * 题号
         */
        private Integer number;

        /**
         * 题目类型
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
         * 学生的答案
         */
        private String studentAnswer;

        /**
         * 正确答案
         */
        private String correctAnswer;

        /**
         * 是否正确
         */
        private Boolean isCorrect;
    }
}
