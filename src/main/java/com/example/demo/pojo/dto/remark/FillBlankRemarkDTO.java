package com.example.demo.pojo.dto.remark;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 填空类型 remark DTO
 * 用于序列化/反序列化 data_collection.remark 中的填空类型 JSON
 *
 * JSON 格式：
 * {"fillBlanks":{"field":"value"},"fieldTolerances":{"field":0.1}}
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class FillBlankRemarkDTO {

    /** 填空数据（字段名 -> 答案） */
    private Map<String, String> fillBlanks;

    /** 字段级误差映射（字段名 -> 误差百分比） */
    private Map<String, Double> fieldTolerances;
}
