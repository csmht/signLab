package com.example.demo.pojo.response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 学生数据收集步骤详情响应
 * 用于教师查询学生的数据收集步骤详情
 */
@Data
public class StudentDataCollectionProcedureDetailResponse {

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
     * 数据收集详情
     */
    private DataCollectionDetail dataCollectionDetail;

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
         * 填空答案（key: 字段名, value: 学生答案值）
         */
        private Map<String, String> fillBlankAnswers;

        /**
         * 表格答案（key: 单元格位置, value: 学生答案值）
         */
        private Map<String, String> tableCellAnswers;

        /**
         * 正确答案（JSON格式）
         */
        private String correctAnswer;

        /**
         * 提交的照片文件列表
         */
        private List<AttachmentInfo> photos;

        /**
         * 提交的文档文件列表
         */
        private List<AttachmentInfo> documents;
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
