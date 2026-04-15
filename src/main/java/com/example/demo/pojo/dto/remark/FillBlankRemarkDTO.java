package com.example.demo.pojo.dto.remark;

import com.example.demo.pojo.dto.mapvo.DataField;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 填空类型 remark DTO
 * 用于序列化/反序列化 data_collection.remark 中的填空类型 JSON
 *
 * JSON 格式：
 * {"fillBlanks":[{"fieldName":"Uab","value":"","tolerance":5.0}]}
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class FillBlankRemarkDTO {

    /** 填空数据列表（字段名 + 值 + 误差） */
    private List<DataField> fillBlanks;
}
