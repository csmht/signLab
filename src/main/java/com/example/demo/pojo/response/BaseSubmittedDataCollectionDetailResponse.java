package com.example.demo.pojo.response;

import com.example.demo.pojo.dto.mapvo.FillBlankAnswer;
import com.example.demo.pojo.dto.mapvo.TableCellAnswer;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 带作答结果的数据收集详情基础响应
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BaseSubmittedDataCollectionDetailResponse extends BaseDataCollectionDetailResponse {
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
     * 正确答案
     */
    private String correctAnswer;

    /**
     * 误差范围（用于数值类答案的判分）
     */
    private Double tolerance;

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
        private java.time.LocalDateTime createTime;

        /**
         * 文件下载密钥
         */
        private String downloadKey;
    }
}
