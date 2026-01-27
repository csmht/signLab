package com.example.demo.pojo.request.teacher;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 创建数据收集步骤请求
 */
@Data
public class CreateDataCollectionProcedureRequest {

    /**
     * 实验ID
     */
    private Long experimentId;

    /**
     * 是否可跳过
     */
    private Boolean isSkip;

    /**
     * 步骤分数占比
     */
    private Integer proportion;

    /**
     * 步骤描述
     */
    private String remark;

    /**
     * 数据类型（1-关键数据/填空，2-表格数据）
     */
    private Integer dataType;

    /**
     * 填空类型数据（Map<需要的数据, 数据答案>）
     * 当dataType=1时使用
     */
    private Map<String, String> fillBlanks;

    /**
     * 表格行表头（第一列的标识）
     * 当dataType=2时使用
     */
    private List<String> tableRowHeaders;

    /**
     * 表格列表头（第一行的标识）
     * 当dataType=2时使用
     */
    private List<String> tableColumnHeaders;

    /**
     * 表格单元格答案（Map<表格坐标, 答案>）
     * 坐标格式：行索引,列索引，如"0,0"、"1,2"
     * 当dataType=2时使用
     */
    private Map<String, String> tableCellAnswers;

    /**
     * 正确答案（Map<数据标识, 正确答案>）
     * 用于自动判分，填空时key为数据名称，表格时key为单元格坐标
     */
    private Map<String, String> correctAnswer;

    /**
     * 误差范围（可选）
     * 用于数值类答案的判分，允许的误差范围
     */
    private Double tolerance;

    /**
     * 是否需要提交照片
     */
    private Boolean needPhoto;

    /**
     * 是否需要提交文档
     */
    private Boolean needDoc;

    /**
     * 步骤开始时间
     */
    private LocalDateTime startTime;

    /**
     * 步骤结束时间
     */
    private LocalDateTime endTime;
}
