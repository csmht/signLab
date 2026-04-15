package com.example.demo.pojo.dto.remark;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 表格类型 remark DTO
 * 用于序列化/反序列化 data_collection.remark 中的表格类型 JSON
 *
 * JSON 格式：
 * {"tableRowHeaders":["A"],"tableColumnHeaders":["1"],"tableCellAnswers":{"A1":"value"},
 *  "cellTolerances":{"A1":0.1},"columnTolerances":{"A":0.2}}
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TableRemarkDTO {

    /** 表格行表头 */
    private List<String> tableRowHeaders;

    /** 表格列表头 */
    private List<String> tableColumnHeaders;

    /** 表格单元格答案（坐标 -> 答案） */
    private Map<String, String> tableCellAnswers;

    /** 单元格级误差映射（坐标 -> 误差百分比） */
    private Map<String, Double> cellTolerances;

    /** 列级误差映射（列名 -> 误差百分比） */
    private Map<String, Double> columnTolerances;
}
