package com.example.demo.pojo.dto.remark;

import com.example.demo.pojo.dto.mapvo.ColumnTolerance;
import com.example.demo.pojo.dto.mapvo.TableCellAnswer;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 表格类型 remark DTO
 * 用于序列化/反序列化 data_collection.remark 中的表格类型 JSON
 *
 * JSON 格式：
 * {"tableRowHeaders":["A","B"],"tableColumnHeaders":["1","2"],
 *  "tableCellAnswers":[{"rowIndex":0,"columnIndex":0,"value":"3.5","tolerance":5.0}],
 *  "columnTolerances":[{"columnIndex":0,"tolerance":3.0}]}
 *
 * rowIndex/columnIndex 从 0 开始，与 tableRowHeaders/tableColumnHeaders 数组索引对应
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

    /** 表格单元格答案列表（行索引 + 列索引 + 值 + 误差） */
    private List<TableCellAnswer> tableCellAnswers;

    /** 列级误差列表（列索引 + 误差百分比） */
    private List<ColumnTolerance> columnTolerances;
}
