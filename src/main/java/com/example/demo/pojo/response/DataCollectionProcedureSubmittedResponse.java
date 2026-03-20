package com.example.demo.pojo.response;

import com.example.demo.pojo.dto.mapvo.FillBlankAnswer;
import com.example.demo.pojo.dto.mapvo.TableCellAnswer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 数据收集步骤详情响应（已提交）
 * 步骤类型：type=2（数据收集）
 */
@Data
public class DataCollectionProcedureSubmittedResponse {

    /**
     * 步骤ID
     */
    private Long id;

    /**
     * 步骤序号
     */
    private Integer number;

    /**
     * 步骤类型（2-数据收集）
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
     * 是否已过答题时间
     * 如果true，表示当前时间已超过步骤答题时间，可以显示正确答案
     * 如果false，表示还在答题时间内，不显示正确答案
     */
    private Boolean isAfterEndTime;

    /**
     * 数据收集详情
     */
    private DataCollectionDetail dataCollectionDetail;

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
         * 数据收集类型（1-关键数据，2-表格数据，3-文件）
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
         * 填空答案列表
         */
        private List<FillBlankAnswer> fillBlankAnswers;

        /**
         * 表格答案列表
         */
        private List<TableCellAnswer> tableCellAnswers;

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

        /**
         * 误差范围（用于数值类答案的判分）
         */
        private Double tolerance;
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
