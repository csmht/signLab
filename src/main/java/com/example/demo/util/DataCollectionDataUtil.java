package com.example.demo.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * 数据收集步骤数据转换工具类
 * 用于将结构化的填空和表格数据转换为JSON格式存储
 */
@Slf4j
public class DataCollectionDataUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 数据收集数据结构
     */
    public static class DataCollectionData {
        /** 数据类型（1-填空，2-表格） */
        private Integer dataType;

        /** 填空数据（Map<需要的数据, 数据答案>） */
        private Map<String, String> fillBlanks;

        /** 表格行表头 */
        private List<String> tableRowHeaders;

        /** 表格列表头 */
        private List<String> tableColumnHeaders;

        /** 表格单元格答案（Map<表格坐标, 答案>） */
        private Map<String, String> tableCellAnswers;

        public Integer getDataType() {
            return dataType;
        }

        public void setDataType(Integer dataType) {
            this.dataType = dataType;
        }

        public Map<String, String> getFillBlanks() {
            return fillBlanks;
        }

        public void setFillBlanks(Map<String, String> fillBlanks) {
            this.fillBlanks = fillBlanks;
        }

        public List<String> getTableRowHeaders() {
            return tableRowHeaders;
        }

        public void setTableRowHeaders(List<String> tableRowHeaders) {
            this.tableRowHeaders = tableRowHeaders;
        }

        public List<String> getTableColumnHeaders() {
            return tableColumnHeaders;
        }

        public void setTableColumnHeaders(List<String> tableColumnHeaders) {
            this.tableColumnHeaders = tableColumnHeaders;
        }

        public Map<String, String> getTableCellAnswers() {
            return tableCellAnswers;
        }

        public void setTableCellAnswers(Map<String, String> tableCellAnswers) {
            this.tableCellAnswers = tableCellAnswers;
        }
    }

    /**
     * 将填空类型数据转换为JSON
     *
     * @param fillBlanks 填空数据
     * @param correctAnswer 正确答案
     * @param tolerance 步骤级误差百分比（单位：%）
     * @return JSON字符串
     */
    public static String convertFillBlanksToJson(Map<String, String> fillBlanks,
                                                  Map<String, String> correctAnswer,
                                                  Double tolerance) {
        return convertFillBlanksToJson(fillBlanks, correctAnswer, tolerance, null);
    }

    /**
     * 将填空类型数据转换为JSON（支持字段级误差）
     *
     * @param fillBlanks 填空数据
     * @param correctAnswer 正确答案
     * @param tolerance 步骤级误差百分比（单位：%）
     * @param fieldTolerances 字段级误差映射
     * @return JSON字符串
     */
    public static String convertFillBlanksToJson(Map<String, String> fillBlanks,
                                                  Map<String, String> correctAnswer,
                                                  Double tolerance,
                                                  Map<String, Double> fieldTolerances) {
        DataCollectionData data = new DataCollectionData();
        data.setDataType(1);
        data.setFillBlanks(fillBlanks);

        try {
            // 将正确答案和误差也存储到同一个JSON中
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("dataType", 1);
            result.put("fillBlanks", fillBlanks);
            result.put("correctAnswer", correctAnswer);
            result.put("tolerance", tolerance);
            result.put("fieldTolerances", fieldTolerances);

            return objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            log.error("转换填空数据为JSON失败", e);
            throw new RuntimeException("转换填空数据为JSON失败", e);
        }
    }

    /**
     * 将表格类型数据转换为JSON
     *
     * @param tableRowHeaders 表格行表头
     * @param tableColumnHeaders 表格列表头
     * @param tableCellAnswers 表格单元格答案
     * @param correctAnswer 正确答案
     * @param tolerance 步骤级误差百分比（单位：%）
     * @return JSON字符串
     */
    public static String convertTableToJson(List<String> tableRowHeaders,
                                           List<String> tableColumnHeaders,
                                           Map<String, String> tableCellAnswers,
                                           Map<String, String> correctAnswer,
                                           Double tolerance) {
        return convertTableToJson(tableRowHeaders, tableColumnHeaders, tableCellAnswers, correctAnswer, tolerance, null);
    }

    /**
     * 将表格类型数据转换为JSON（支持单元格级误差）
     *
     * @param tableRowHeaders 表格行表头
     * @param tableColumnHeaders 表格列表头
     * @param tableCellAnswers 表格单元格答案
     * @param correctAnswer 正确答案
     * @param tolerance 步骤级误差百分比（单位：%）
     * @param cellTolerances 单元格级误差映射
     * @return JSON字符串
     */
    public static String convertTableToJson(List<String> tableRowHeaders,
                                           List<String> tableColumnHeaders,
                                           Map<String, String> tableCellAnswers,
                                           Map<String, String> correctAnswer,
                                           Double tolerance,
                                           Map<String, Double> cellTolerances) {
        return convertTableToJson(tableRowHeaders, tableColumnHeaders, tableCellAnswers, correctAnswer, tolerance, cellTolerances, null);
    }

    /**
     * 将表格类型数据转换为JSON（支持单元格级和列级误差）
     *
     * @param tableRowHeaders 表格行表头
     * @param tableColumnHeaders 表格列表头
     * @param tableCellAnswers 表格单元格答案
     * @param correctAnswer 正确答案
     * @param tolerance 步骤级误差百分比（单位：%）
     * @param cellTolerances 单元格级误差映射
     * @param columnTolerances 列级误差映射
     * @return JSON字符串
     */
    public static String convertTableToJson(List<String> tableRowHeaders,
                                           List<String> tableColumnHeaders,
                                           Map<String, String> tableCellAnswers,
                                           Map<String, String> correctAnswer,
                                           Double tolerance,
                                           Map<String, Double> cellTolerances,
                                           Map<String, Double> columnTolerances) {
        try {
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("dataType", 2);
            result.put("tableRowHeaders", tableRowHeaders);
            result.put("tableColumnHeaders", tableColumnHeaders);
            result.put("tableCellAnswers", tableCellAnswers);
            result.put("correctAnswer", correctAnswer);
            result.put("tolerance", tolerance);
            result.put("cellTolerances", cellTolerances);
            result.put("columnTolerances", columnTolerances);

            return objectMapper.writeValueAsString(result);
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
     * 从JSON中解析字段级误差映射
     *
     * @param json 数据收集JSON字符串
     * @return 字段级误差映射
     */
    public static Map<String, Double> parseFieldTolerancesFromJson(String json) {
        try {
            com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(json);
            com.fasterxml.jackson.databind.JsonNode fieldTolerancesNode = root.get("fieldTolerances");
            if (fieldTolerancesNode == null || fieldTolerancesNode.isNull()) {
                return Map.of();
            }
            return objectMapper.convertValue(fieldTolerancesNode,
                    new com.fasterxml.jackson.core.type.TypeReference<Map<String, Double>>() {});
        } catch (JsonProcessingException e) {
            log.error("解析字段级误差失败", e);
            return Map.of();
        }
    }

    /**
     * 从JSON中解析单元格级误差映射
     *
     * @param json 数据收集JSON字符串
     * @return 单元格级误差映射
     */
    public static Map<String, Double> parseCellTolerancesFromJson(String json) {
        try {
            com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(json);
            com.fasterxml.jackson.databind.JsonNode cellTolerancesNode = root.get("cellTolerances");
            if (cellTolerancesNode == null || cellTolerancesNode.isNull()) {
                return Map.of();
            }
            return objectMapper.convertValue(cellTolerancesNode,
                    new com.fasterxml.jackson.core.type.TypeReference<Map<String, Double>>() {});
        } catch (JsonProcessingException e) {
            log.error("解析单元格级误差失败", e);
            return Map.of();
        }
    }

    /**
     * 从JSON中解析列级误差映射
     *
     * @param json 数据收集JSON字符串
     * @return 列级误差映射
     */
    public static Map<String, Double> parseColumnTolerancesFromJson(String json) {
        try {
            com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(json);
            com.fasterxml.jackson.databind.JsonNode columnTolerancesNode = root.get("columnTolerances");
            if (columnTolerancesNode == null || columnTolerancesNode.isNull()) {
                return Map.of();
            }
            return objectMapper.convertValue(columnTolerancesNode,
                    new com.fasterxml.jackson.core.type.TypeReference<Map<String, Double>>() {});
        } catch (JsonProcessingException e) {
            log.error("解析列级误差失败", e);
            return Map.of();
        }
    }
}
