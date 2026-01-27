package com.example.demo.pojo.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 完成数据收集请求
 */
@Data
public class CompleteDataCollectionProcedureRequest {

    /** 实验步骤ID */
    private Long procedureId;

    /** 班级编号 */
    private String classCode;

    /** 填空类型答案，类型：Map<数据名称, 学生答案>，当数据类型为1时使用 */
    private Map<String, String> fillBlankAnswers;

    /** 表格类型答案，类型：Map<表格坐标, 学生答案>，坐标格式：行索引,列索引，如"0,0"、"1,2"，当数据类型为2时使用 */
    private Map<String, String> tableCellAnswers;

    /** 照片文件列表 */
    private List<MultipartFile> photos;

    /** 文档文件列表 */
    private List<MultipartFile> documents;
}
