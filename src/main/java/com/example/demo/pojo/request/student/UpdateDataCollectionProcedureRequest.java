package com.example.demo.pojo.request.student;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

/**
 * 更新数据收集请求
 */
@Data
public class UpdateDataCollectionProcedureRequest {
    /**
     * 实验步骤ID
     */
    private Long procedureId;

    /**
     * 班级编号
     */
    private String classCode;

    /**
     * 填空类型答案
     */
    private Map<String, String> fillBlankAnswers;

    /**
     * 表格类型答案
     */
    private Map<String, String> tableCellAnswers;

    /**
     * 照片文件列表
     */
    private List<MultipartFile> photos;

    /**
     * 文档文件列表
     */
    private List<MultipartFile> documents;

    /**
     * 需要删除的附件ID列表
     */
    private List<Long> attachmentIdsToDelete;
}
