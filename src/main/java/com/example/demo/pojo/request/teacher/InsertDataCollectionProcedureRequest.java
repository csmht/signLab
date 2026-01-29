package com.example.demo.pojo.request.teacher;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 插入数据收集步骤请求
 */
@Data
public class InsertDataCollectionProcedureRequest {

    /** 实验ID */
    private Long experimentId;

    /** 插入位置（在该步骤序号后插入） */
    private Integer afterNumber;

    /** 是否可跳过 */
    private Boolean isSkip;

    /** 步骤分数占比 */
    private Integer proportion;

    /** 步骤描述 */
    private String remark;

    /** 数据类型：1-填空类型（关键数据），2-表格类型 */
    private Integer dataType;

    /** 收集数据字段，格式：Map<数据名称, 正确答案>，示例：{"温度": "25", "湿度": "60", "压力": "101.3"}，当dataType=1时使用，只对数字类型数据进行自动判分 */
    private Map<String, String> dataFields;

    /** 表格行表头（第一列的标识），类型：List<String>，示例：["实验1", "实验2", "实验3"]，当dataType=2时使用 */
    private List<String> tableRowHeaders;

    /** 表格列表头（第一行的标识），类型：List<String>，示例：["温度", "湿度", "压力"]，当dataType=2时使用 */
    private List<String> tableColumnHeaders;

    /** 表格单元格答案，类型：Map<String, String>，格式：{"行索引,列索引": "答案值"}，示例：{"0,0": "25", "0,1": "60", "1,0": "26"}，当dataType=2时使用，只对数字类型单元格进行自动判分 */
    private Map<String, String> tableCellAnswers;

    /** 误差范围（可选，用于数值类答案的判分），类型：Double，用途：数值类答案的允许误差，示例：0.5 (表示±0.5的误差)，可以为null，表示精确匹配 */
    private Double tolerance;

    /** 是否需要提交照片 */
    private Boolean needPhoto;

    /** 是否需要提交文档 */
    private Boolean needDoc;
}
