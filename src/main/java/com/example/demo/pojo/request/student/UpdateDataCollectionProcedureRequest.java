package com.example.demo.pojo.request.student;

import com.example.demo.pojo.dto.mapvo.FillBlankAnswer;
import com.example.demo.pojo.dto.mapvo.TableCellAnswer;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

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
     * 填空类型答案列表
     */
    private List<FillBlankAnswer> fillBlankAnswers;

    /**
     * 表格类型答案列表
     */
    private List<TableCellAnswer> tableCellAnswers;

    /**
     * 附件文件列表（不区分照片和文档）
     */
    private List<MultipartFile> attachments;

    /**
     * 需要删除的附件ID列表
     */
    private List<Long> attachmentIdsToDelete;
}
