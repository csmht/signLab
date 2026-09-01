package com.example.demo.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tangzc.autotable.annotation.AutoTable;
import com.tangzc.autotable.annotation.TableIndex;
import com.tangzc.autotable.annotation.enums.IndexTypeEnum;
import com.tangzc.mpe.autotable.annotation.Column;
import com.tangzc.mpe.autotable.annotation.Table;
import com.example.demo.pojo.dto.mapvo.AnswerRange;
import com.example.demo.pojo.dto.remark.FillBlankRemarkDTO;
import com.example.demo.util.DataCollectionDataUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 实验步骤-数据收集表
 * 存储实验学生需要完成的数据收集类型步骤的数据信息
 */
@Data
@AutoTable
@Table(value = "data_collection", comment = "实验步骤-数据收集表 - 存储实验学生需要完成的数据收集类型步骤的数据信息")
@TableName("data_collection")
@TableIndex(name = "uk_procedure_data", fields = {"experimentalProcedureId"}, type = IndexTypeEnum.UNIQUE)
public class DataCollection {

    private static final ObjectMapper CORRECT_ANSWER_MAPPER = new ObjectMapper();

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 实验步骤ID */
    @Column(comment = "实验步骤ID", type = "bigint", notNull = true)
    private Long experimentalProcedureId;

    /** 数据类型（1--关键数据，2--表格数据，3--文件数据） */
    @Column(comment = "数据类型（1--关键数据，2--表格数据，3--文件数据）" , type = "int", notNull = true)
    private Long type;

    /** 数据描述（JSON格式，存储填空和表格的结构化数据） */
    @Column(comment = "数据描述（JSON格式，存储填空和表格的结��化数据）", type = "text")
    private String remark;

    /** 是否需要提交照片：0-不需要，1-需要 */
    @Column(comment = "是否需要提交照片：0-不需要，1-需要", type = "bit")
    private Boolean needPhoto;

    /** 是否需要提交文档：0-不需要，1-需要 */
    @Column(comment = "是否需要提交文档：0-不需要，1-需要", type = "bit")
    private Boolean needDoc;

    /** 正确答案（JSON格式，用于自动判分） */
    @JsonIgnore
    @Column(comment = "正确答案（JSON格式，用于自动判分）", type = "text")
    private String correctAnswer;

    /** 表格类型步骤级误差范围（填空范围答案不使用） */
    @Column(comment = "表格类型步骤级误差范围（填空范围答案不使用）", type = "double")
    private Double tolerance;

    /** 创建时间 */
    @Column(comment = "创建时间", type = "datetime", defaultValue = "CURRENT_TIMESTAMP")
    private LocalDateTime createdTime;

    /** 是否删除：0-否，1-是 */
    @Column(comment = "是否删除：0-否，1-是", type = "bit", defaultValue = "0")
    private Boolean isDeleted;

    /**
     * 判分路径：将正确答案 JSON 严格解析为 Map。
     * JSON 非法时抛出异常，由判分流程统一处理。
     */
    public Map<String, String> parseCorrectAnswerMap() {
        if (correctAnswer == null || correctAnswer.trim().isEmpty()) {
            return Map.of();
        }
        try {
            Map<String, String> parsedAnswers = CORRECT_ANSWER_MAPPER.readValue(
                    correctAnswer,
                    new TypeReference<Map<String, String>>() {}
            );
            if (!Long.valueOf(2L).equals(type)) {
                parsedAnswers.values().stream()
                        .filter(com.example.demo.pojo.dto.mapvo.DataField::isRangeCorrectAnswer)
                        .forEach(AnswerRange::fromStoredValue);
            }
            return parsedAnswers;
        } catch (Exception e) {
            throw new IllegalArgumentException("解析数据收集正确答案失败", e);
        }
    }

    /**
     * 填空回显路径：解析 remark 结构，并按答案展示策略决定是否合并正确答案。
     * 旧 remark 中的 value 仅在服务端解析；不允许展示答案时会清除其转换结果。
     */
    public FillBlankRemarkDTO resolveFillBlankRemark(boolean includeCorrectAnswer) {
        Map<String, String> storedAnswers = includeCorrectAnswer
                ? parseCorrectAnswerMap()
                : Map.of();
        return DataCollectionDataUtil.parseFillBlankRemark(
                remark, storedAnswers, includeCorrectAnswer);
    }

    /**
     * 响应路径：将数据库编码转换为直接包含 min/max 的范围 Map。
     */
    public Map<String, AnswerRange> resolveCorrectAnswerRanges() {
        Map<String, String> storedAnswers = parseCorrectAnswerMap();
        if (storedAnswers.isEmpty()) {
            return Map.of();
        }
        Map<String, AnswerRange> ranges = new LinkedHashMap<>();
        boolean fillBlankType = !Long.valueOf(2L).equals(type);
        storedAnswers.forEach((key, value) -> ranges.put(
                key,
                fillBlankType
                        ? AnswerRange.fromStoredValue(value)
                        : AnswerRange.fromExactValue(value)));
        return ranges;
    }
}
