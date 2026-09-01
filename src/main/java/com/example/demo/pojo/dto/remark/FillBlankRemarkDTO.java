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
 * 范围答案 JSON 格式：
 * {"fillBlanks":[{"fieldName":"Uab","min":"210","max":"230"}]}
 *
 * 旧的精确答案在生成 DTO 时转换为 min 与 max 相同的范围：
 * {"fillBlanks":[{"fieldName":"Uab","min":"220","max":"220"}]}
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class FillBlankRemarkDTO {

    /** 填空数据列表（字段名 + 最小值 + 最大值） */
    private List<DataField> fillBlanks;
}
