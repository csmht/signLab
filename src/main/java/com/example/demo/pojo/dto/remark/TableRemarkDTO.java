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
 * {"tableRowHeaders":["A"],"tableColumnHeaders":["1"],
 *  "tableCellAnswers":[{"cellPosition":"A1","value":"3.5","tolerance":5.0}],
 *  "columnTolerances":[{"columnName":"A","tolerance":3.0}]}
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

    /** 表格单元格答案列表（坐标 + 值 + 误差） */
    private List<TableCellAnswer> tableCellAnswers;

    /** 列级误差列表（列名 + 误差百分比） */
    private List<ColumnTolerance> columnTolerances;
}
