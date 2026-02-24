package com.example.demo.pojo.request;

import com.example.demo.pojo.dto.mapvo.FillBlankAnswer;
import com.example.demo.pojo.dto.mapvo.TableCellAnswer;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 完成数据收集请求
 */
@Data
public class CompleteDataCollectionProcedureRequest {

    /** 实验步骤ID */
    private Long procedureId;

    /** 班级编号 */
    private String classCode;

    /** 填空类型答案列表，当数据类型为1时使用 */
    private List<FillBlankAnswer> fillBlankAnswers;

    /** 表格类型答案列表，当数据类型为2时使用 */
    private List<TableCellAnswer> tableCellAnswers;

    /** 照片文件列表 */
    private List<MultipartFile> photos;

    /** 文档文件列表 */
    private List<MultipartFile> documents;
}
