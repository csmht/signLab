package com.example.demo.pojo.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 完成数据收集请求
 */
@Data
public class CompleteDataCollectionProcedureRequest {

    /**
     * 实验步骤ID
     */
    private Long procedureId;

    /**
     * 班级编号
     */
    private String classCode;

    /**
     * 数据答案（文本类型）
     */
    private String dataAnswer;

    /**
     * 照片文件列表
     */
    private List<MultipartFile> photos;

    /**
     * 文档文件列表
     */
    private List<MultipartFile> documents;
}
