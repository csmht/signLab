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

    /** 实验ID */
    private Long experimentId;

    /** 是否可跳过 */
    private Boolean isSkip;

    /** 步骤分数占比 */
    private Integer proportion;

    /** 步骤描述 */
    private String remark;

    /** 数据类型：1-填空类型（关键数据），2-表格类型 */
    private Integer dataType;

    /** 填空类型数据，类型：Map<String, String>，格式：{"数据名称": "答案值"}，示例：{"温度": "25", "湿度": "60", "压力": "101.3"}，当dataType=1时使用 */
    private Map<String, String> fillBlanks;

    /** 表格行表头（第一列的标识），类型：List<String>，示例：["实验1", "实验2", "实验3"]，当dataType=2时使用 */
    private List<String> tableRowHeaders;

    /** 表格列表头（第一行的标识），类型：List<String>，示例：["温度", "湿度", "压力"]，当dataType=2时使用 */
    private List<String> tableColumnHeaders;

    /** 表格单元格答案，类型：Map<String, String>，格式：{"行索引,列索引": "答案值"}，示例：{"0,0": "25", "0,1": "60", "1,0": "26"}，当dataType=2时使用 */
    private Map<String, String> tableCellAnswers;

    /** 正确答案（用于自动判分），类型：Map<String, String>，填空类型：{"数据名称": "正确答案"}，表格类型：{"单元格坐标": "正确答案"} */
    private Map<String, String> correctAnswer;

    /** 误差范围（可选，用于数值类答案的判分），类型：Double，用途：数值类答案的允许误差，示例：0.5 (表示±0.5的误差)，可以为null，表示精确匹配 */
    private Double tolerance;

    /** 是否需要提交照片 */
    private Boolean needPhoto;

    /** 是否需要提交文档 */
    private Boolean needDoc;

    /** 步骤开始时间 */
    private LocalDateTime startTime;

    /** 步骤结束时间 */
    private LocalDateTime endTime;
}
