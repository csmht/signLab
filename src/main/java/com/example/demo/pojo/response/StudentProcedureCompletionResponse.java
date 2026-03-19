package com.example.demo.pojo.response;

import com.example.demo.pojo.dto.mapvo.FillBlankAnswer;
import com.example.demo.pojo.dto.mapvo.TableCellAnswer;
import com.example.demo.pojo.dto.mapvo.TopicChoice;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 学生实验步骤完成情况响应
 */
@Data
public class StudentProcedureCompletionResponse {

    /**
     * 学生用户名
     */
    private String studentUsername;

    /**
     * 班级编号
     */
    private String classCode;

    /**
     * 实验ID
     */
    private Long experimentId;

    /**
     * 实验名称
     */
    private String experimentName;

    /**
     * 步骤完成列表
     */
    private List<ProcedureCompletion> procedures;

    /**
     * 总进度
     */
    private String progress;

    /**
     * 总得分
     */
    private BigDecimal totalScore;

    /**
     * 步骤完成详情
     */
    @Data
    public static class ProcedureCompletion {
        /**
         * 步骤ID
         */
        private Long id;

        /**
         * 步骤序号
         */
        private Integer number;

        /**
         * 步骤类型（1-观看视频，2-数据收集，3-题库答题，5-限时答题）
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
         * 步骤分数占比
         */
        private Integer proportion;

        /**
         * 提交记录ID（学生完成该步骤后才有值）
         */
        private Long submissionId;

        /**
         * 评分状态（0-未评分，1-教师人工评分，2-系统自动评分）
         * 学生完成该步骤后才有值
         */
        private Integer isGraded;

        /**
         * 教师评语
         */
        private String teacherComment;

        // ===== 类型2：数据收集提交内容 =====

        /**
         * 填空答案列表（类型2有效）
         */
        private List<FillBlankAnswer> fillBlankAnswers;

        /**
         * 表格答案列表（类型2有效）
         */
        private List<TableCellAnswer> tableCellAnswers;

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

        // ===== 类型5：限时答题提交内容 =====

        /**
         * 限时答题详情（类型5有效）
         */
        private TimedQuizDetail timedQuizDetail;
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
        private List<TopicChoice> choices;

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

        /**
         * 从JSON字符串解析选项
         */
        public void setChoices(String choices) {
            if (choices == null || choices.trim().isEmpty()) {
                this.choices = null;
                return;
            }
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                java.util.Map<String, String> map = mapper.readValue(choices,
                    new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, String>>() {});
                this.choices = TopicChoice.fromMap(map);
            } catch (Exception e) {
                this.choices = null;
            }
        }
    }

    /**
     * 限时答题详情
     */
    @Data
    public static class TimedQuizDetail {
        /**
         * 限时答题配置ID
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
         * 答题时间限制（分钟）
         */
        private Integer quizTimeLimit;

        /**
         * 是否已锁定
         */
        private Boolean isLocked;

        /**
         * 标签限制列表（随机模式有效）
         */
        private List<TagInfo> tags;

        /**
         * 题目类型限制（随机模式有效，逗号分隔）
         */
        private String topicTypes;

        /**
         * 题目列表（带答案，仅非随机模式有效）
         */
        private List<TopicItem> topics;
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
         * 选项内容列表
         */
        private List<TopicChoice> choices;

        /**
         * 学生答案
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

        /**
         * 得分
         */
        private BigDecimal score;

        /**
         * 从JSON字符串解析选项
         */
        public void setChoices(String choices) {
            if (choices == null || choices.trim().isEmpty()) {
                this.choices = null;
                return;
            }
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                java.util.Map<String, String> map = mapper.readValue(choices,
                    new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, String>>() {});
                this.choices = TopicChoice.fromMap(map);
            } catch (Exception e) {
                this.choices = null;
            }
        }
    }
}
