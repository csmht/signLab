package com.example.demo.util;

import com.example.demo.pojo.dto.remark.FillBlankRemarkDTO;
import com.example.demo.pojo.dto.remark.TableRemarkDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * 数据收集步骤数据转换工具类
 * 用于将结构化的填空和表格数据转换为JSON格式存储到 remark 字段
 *
 * remark JSON 格式（按数据类型区分）：
 * - 填空类型（type=1）：{"fillBlanks":{"field":"value"},"fieldTolerances":{"field":0.1}}
 * - 表格类型（type=2）：{"tableRowHeaders":["A"],"tableColumnHeaders":["1"],"tableCellAnswers":{"A1":"value"},...}
 * - 文件类型（type=3）：{}
 *
 * 注意：dataType 已从 remark 中移除，请通过 DataCollection.type 获取数据类型
 */
@Slf4j
public class DataCollectionDataUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 将填空类型数据转换为JSON（支持字段级误差）
     *
     * @param fillBlanks      填空数据（字段名 -> 值）
     * @param fieldTolerances 字段级误差映射（可选）
     * @return JSON字符串
     */
    public static String convertFillBlanksToJson(Map<String, String> fillBlanks,
                                                  Map<String, Double> fieldTolerances) {
        try {
            FillBlankRemarkDTO dto = FillBlankRemarkDTO.builder()
                    .fillBlanks(fillBlanks)
                    .fieldTolerances(fieldTolerances)
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
     * @param tableCellAnswers   表格单元格答案
     * @param cellTolerances     单元格级误差映射（可选）
     * @param columnTolerances   列级误差映射（可选）
     * @return JSON字符串
     */
    public static String convertTableToJson(List<String> tableRowHeaders,
                                           List<String> tableColumnHeaders,
                                           Map<String, String> tableCellAnswers,
                                           Map<String, Double> cellTolerances,
                                           Map<String, Double> columnTolerances) {
        try {
            TableRemarkDTO dto = TableRemarkDTO.builder()
                    .tableRowHeaders(tableRowHeaders)
                    .tableColumnHeaders(tableColumnHeaders)
                    .tableCellAnswers(tableCellAnswers)
                    .cellTolerances(cellTolerances)
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
        if (dto == null || dto.getFieldTolerances() == null) {
            return Map.of();
        }
        return dto.getFieldTolerances();
    }

    /**
     * 从JSON中解析单元格级误差映射
     *
     * @param json 数据收集JSON字符串
     * @return 单元格级误差映射
     */
    public static Map<String, Double> parseCellTolerancesFromJson(String json) {
        TableRemarkDTO dto = parseTableRemark(json);
        if (dto == null || dto.getCellTolerances() == null) {
            return Map.of();
        }
        return dto.getCellTolerances();
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
        return dto.getColumnTolerances();
    }
}
