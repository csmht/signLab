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
     * @param tolerance 误差范围
     * @return JSON字符串
     */
    public static String convertFillBlanksToJson(Map<String, String> fillBlanks,
                                                  Map<String, String> correctAnswer,
                                                  Double tolerance) {
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
     * @param tolerance 误差范围
     * @return JSON字符串
     */
    public static String convertTableToJson(List<String> tableRowHeaders,
                                           List<String> tableColumnHeaders,
                                           Map<String, String> tableCellAnswers,
                                           Map<String, String> correctAnswer,
                                           Double tolerance) {
        try {
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("dataType", 2);
            result.put("tableRowHeaders", tableRowHeaders);
            result.put("tableColumnHeaders", tableColumnHeaders);
            result.put("tableCellAnswers", tableCellAnswers);
            result.put("correctAnswer", correctAnswer);
            result.put("tolerance", tolerance);

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
}
