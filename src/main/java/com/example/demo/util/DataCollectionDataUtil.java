package com.example.demo.util;

import com.example.demo.pojo.dto.mapvo.ColumnTolerance;
import com.example.demo.pojo.dto.mapvo.DataField;
import com.example.demo.pojo.dto.mapvo.TableCellAnswer;
import com.example.demo.pojo.dto.remark.FillBlankRemarkDTO;
import com.example.demo.pojo.dto.remark.TableRemarkDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * 数据收集步骤数据转换工具类
 * 用于将结构化的填空和表格数据转换为JSON格式存储到 remark 字段
 *
 * remark JSON 格式（按数据类型区分）：
 * - 填空类型（type=1）：{"fillBlanks":[{"fieldName":"Uab","value":"","tolerance":5.0}]}
 * - 表格类型（type=2）：{"tableRowHeaders":["A","B"],"tableColumnHeaders":["1","2"],
 *   "tableCellAnswers":[{"rowIndex":0,"columnIndex":0,"value":"3.5","tolerance":5.0}],
 *   "columnTolerances":[{"columnIndex":0,"tolerance":3.0}]}
 * - 文件类型（type=3）：{}
 *
 * 注意：dataType 已从 remark 中移除，请通过 DataCollection.type 获取数据类型
 */
@Slf4j
public class DataCollectionDataUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 将填空类型数据转换为JSON
     *
     * @param dataFields 填空数据列表（fieldName + value + tolerance）
     * @return JSON字符串
     */
    public static String convertFillBlanksToJson(List<DataField> dataFields) {
        try {
            FillBlankRemarkDTO dto = FillBlankRemarkDTO.builder()
                    .fillBlanks(dataFields)
                    .build();
            return objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            log.error("转换填空数据为JSON失败", e);
            throw new RuntimeException("转换填空数据为JSON失败", e);
        }
    }

    /**
     * 将表格类型数据转换为JSON（支持单元格级和列级误差）
     *
     * @param tableRowHeaders    表格行表头
     * @param tableColumnHeaders 表格列表头
     * @param tableCellAnswers   表格单元格答案列表（cellPosition + value + tolerance）
     * @param columnTolerances   列级误差列表（columnName + tolerance，可选）
     * @return JSON字符串
     */
    public static String convertTableToJson(List<String> tableRowHeaders,
                                           List<String> tableColumnHeaders,
                                           List<TableCellAnswer> tableCellAnswers,
                                           List<ColumnTolerance> columnTolerances) {
        try {
            TableRemarkDTO dto = TableRemarkDTO.builder()
                    .tableRowHeaders(tableRowHeaders)
                    .tableColumnHeaders(tableColumnHeaders)
                    .tableCellAnswers(tableCellAnswers)
                    .columnTolerances(columnTolerances)
                    .build();
            return objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            log.error("转换表格数据为JSON失败", e);
            throw new RuntimeException("转换表格数据为JSON失败", e);
        }
    }

    /**
     * 将正确答案转换为JSON
     *
     * @param correctAnswer 正确答案
     * @return JSON字符串
     */
    public static String convertCorrectAnswerToJson(Map<String, String> correctAnswer) {
        try {
            return objectMapper.writeValueAsString(correctAnswer);
        } catch (JsonProcessingException e) {
            log.error("转换正确答案为JSON失败", e);
            throw new RuntimeException("转换正确答案为JSON失败", e);
        }
    }

    /**
     * 从 remark JSON 中解析为填空类型 DTO
     *
     * @param remarkJson remark JSON字符串
     * @return 填空类型 DTO，解析失败返回 null
     */
    public static FillBlankRemarkDTO parseFillBlankRemark(String remarkJson) {
        try {
            return objectMapper.readValue(remarkJson, FillBlankRemarkDTO.class);
        } catch (JsonProcessingException e) {
            log.error("解析填空类型 remark 失败", e);
            return null;
        }
    }

    /**
     * 从 remark JSON 和 correctAnswer JSON 中解析填空类型 DTO
     * remark 提供字段结构与误差，correctAnswer 提供字段值
     *
     * @param remarkJson remark JSON字符串
     * @param correctAnswerJson correctAnswer JSON字符串
     * @return 填空类型 DTO，解析失败时尽量返回可用结构
     */
    public static FillBlankRemarkDTO parseFillBlankRemark(String remarkJson, String correctAnswerJson) {
        FillBlankRemarkDTO dto = parseFillBlankRemark(remarkJson);
        Map<String, String> correctAnswerMap = parseCorrectAnswer(correctAnswerJson);

        if (dto != null && dto.getFillBlanks() != null && !dto.getFillBlanks().isEmpty()) {
            if (!correctAnswerMap.isEmpty()) {
                dto.getFillBlanks().forEach(field -> field.setValue(correctAnswerMap.get(field.getFieldName())));
            }
            return dto;
        }

        if (correctAnswerMap.isEmpty()) {
            return dto;
        }

        return FillBlankRemarkDTO.builder()
                .fillBlanks(DataField.fromMap(correctAnswerMap))
                .build();
    }

    /**
     * 从 remark JSON 中解析为表格类型 DTO
     *
     * @param remarkJson remark JSON字符串
     * @return 表格类型 DTO，解析失败返回 null
     */
    public static TableRemarkDTO parseTableRemark(String remarkJson) {
        try {
            return objectMapper.readValue(remarkJson, TableRemarkDTO.class);
        } catch (JsonProcessingException e) {
            log.error("解析表格类型 remark 失败", e);
            return null;
        }
    }

    /**
     * 从JSON中解析字段级误差映射
     *
     * @param json 数据收集JSON字符串
     * @return 字段级误差映射
     */
    public static Map<String, Double> parseFieldTolerancesFromJson(String json) {
        FillBlankRemarkDTO dto = parseFillBlankRemark(json);
        if (dto == null || dto.getFillBlanks() == null) {
            return Map.of();
        }
        return DataField.toToleranceMap(dto.getFillBlanks());
    }

    /**
     * 从JSON中解析单元格级误差映射
     *
     * @param json 数据收集JSON字符串
     * @return 单元格级误差映射
     */
    public static Map<String, Double> parseCellTolerancesFromJson(String json) {
        TableRemarkDTO dto = parseTableRemark(json);
        if (dto == null || dto.getTableCellAnswers() == null) {
            return Map.of();
        }
        return TableCellAnswer.toToleranceMap(dto.getTableCellAnswers());
    }

    /**
     * 从JSON中解析列级误差映射
     *
     * @param json 数据收集JSON字符串
     * @return 列级误差映射
     */
    public static Map<String, Double> parseColumnTolerancesFromJson(String json) {
        TableRemarkDTO dto = parseTableRemark(json);
        if (dto == null || dto.getColumnTolerances() == null) {
            return Map.of();
        }
        return ColumnTolerance.toMap(dto.getColumnTolerances());
    }

    private static Map<String, String> parseCorrectAnswer(String correctAnswerJson) {
        if (correctAnswerJson == null || correctAnswerJson.trim().isEmpty()) {
            return Map.of();
        }

        try {
            return objectMapper.readValue(correctAnswerJson, new TypeReference<Map<String, String>>() {});
        } catch (JsonProcessingException e) {
            log.error("解析填空类型 correctAnswer 失败", e);
            return Map.of();
        }
    }
}
