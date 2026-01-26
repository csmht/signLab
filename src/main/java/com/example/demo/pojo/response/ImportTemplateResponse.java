package com.example.demo.pojo.response;

import lombok.Data;

import java.util.List;

/**
 * 导入模板响应DTO
 * 提供导入模板的列说明和示例数据
 */
@Data
public class ImportTemplateResponse {

    /**
     * 模板类型
     */
    private String templateType;

    /**
     * 模板名称
     */
    private String templateName;

    /**
     * 模板描述
     */
    private String description;

    /**
     * 列信息列表
     */
    private List<ColumnInfo> columns;

    /**
     * 示例数据
     */
    private List<String> exampleData;

    /**
     * 列信息内部类
     */
    @Data
    public static class ColumnInfo {
        /**
         * 列序号（从0开始）
         */
        private Integer columnIndex;

        /**
         * 列名称
         */
        private String columnName;

        /**
         * 字段名称
         */
        private String fieldName;

        /**
         * 数据类型
         */
        private String dataType;

        /**
         * 是否必填
         */
        private Boolean required;

        /**
         * 示例值
         */
        private String exampleValue;

        /**
         * 说明
         */
        private String description;

        /**
         * 允许的值（枚举类型）
         */
        private String allowedValues;

        /**
         * 格式要求
         */
        private String format;
    }
}