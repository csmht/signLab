package com.example.demo.pojo.dto.mapvo;

import com.example.demo.exception.BusinessException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 数据收集字段
 * 用于替代 Map<String, String> 结构（数据收集字段）
 * Key: 字段名，Value: 最小值和最大值确定的答案范围
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataField {

    private static final String RANGE_PREFIX = "RANGE|";

    /**
     * 字段名
     */
    private String fieldName;

    /**
     * 范围答案最小值（包含边界）。
     */
    private String min;

    /**
     * 范围答案最大值（包含边界）。
     */
    private String max;

    /**
     * 将 List<DataField> 转换为 Map<String, String>
     *
     * @param fields 字段列表
     * @return Map<字段名, 字段值>
     */
    public static Map<String, String> toMap(List<DataField> fields) {
        if (fields == null || fields.isEmpty()) {
            return Map.of();
        }
        try {
            return fields.stream()
                    .collect(Collectors.toMap(DataField::getFieldName, DataField::toCorrectAnswerValue));
        } catch (IllegalStateException e) {
            throw new BusinessException(400,"一个步骤不能有相同名字的数据");
        }
    }

    /**
     * 将 Map<String, String> 转换为 List<DataField>
     *
     * @param map Map<字段名, 字段值>
     * @return 字段列表
     */
    public static List<DataField> fromMap(Map<String, String> map) {
        if (map == null || map.isEmpty()) {
            return new ArrayList<>();
        }
        return map.entrySet().stream()
                .map(entry -> {
                    DataField field = new DataField();
                    field.setFieldName(entry.getKey());
                    field.applyCorrectAnswerValue(entry.getValue());
                    return field;
                })
                .collect(Collectors.toList());
    }

    /**
     * 将字段答案转换为 correct_answer Map 中保存的字符串值。
     */
    public String toCorrectAnswerValue() {
        boolean hasMin = min != null && !min.trim().isEmpty();
        boolean hasMax = max != null && !max.trim().isEmpty();
        if (!hasMin || !hasMax) {
            throw new BusinessException(400, "最小值和最大值必须同时填写");
        }

        BigDecimal minNumber = parseRangeBoundary(min, "最小值");
        BigDecimal maxNumber = parseRangeBoundary(max, "最大值");
        if (minNumber.compareTo(maxNumber) > 0) {
            throw new BusinessException(400, "最小值不能大于最大值");
        }
        return RANGE_PREFIX + min.trim() + "|" + max.trim();
    }

    /**
     * 将 correct_answer 中的字符串值展开到旧值或范围字段。
     */
    public void applyCorrectAnswerValue(String correctAnswerValue) {
        min = null;
        max = null;

        if (correctAnswerValue == null) {
            return;
        }
        if (!correctAnswerValue.startsWith(RANGE_PREFIX)) {
            min = correctAnswerValue;
            max = correctAnswerValue;
            return;
        }

        String[] parts = correctAnswerValue.split("\\|", -1);
        if (parts.length != 3 || parts[1].trim().isEmpty() || parts[2].trim().isEmpty()) {
            throw new IllegalArgumentException("非法的正确答案范围格式");
        }

        String storedMin = parts[1].trim();
        String storedMax = parts[2].trim();
        BigDecimal minNumber;
        BigDecimal maxNumber;
        try {
            minNumber = new BigDecimal(storedMin);
            maxNumber = new BigDecimal(storedMax);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("正确答案范围边界必须是有效数字", e);
        }
        if (minNumber.compareTo(maxNumber) > 0) {
            throw new IllegalArgumentException("正确答案范围最小值不能大于最大值");
        }

        min = storedMin;
        max = storedMax;
    }

    /**
     * 兼容旧 remark 中的 value 字段，反序列化时直接转换为等值范围。
     */
    @JsonSetter("value")
    @JsonProperty(value = "value", access = JsonProperty.Access.WRITE_ONLY)
    public void applyLegacyValue(String legacyValue) {
        if (legacyValue != null && min == null && max == null) {
            min = legacyValue;
            max = legacyValue;
        }
    }

    /**
     * 判断 correct_answer 中的字段值是否为范围格式。
     */
    public static boolean isRangeCorrectAnswer(String correctAnswerValue) {
        return correctAnswerValue != null && correctAnswerValue.startsWith(RANGE_PREFIX);
    }

    /**
     * 使用包含边界的最小值/最大值判断学生答案是否在正确范围内。
     */
    public static boolean isAnswerWithinRange(String studentAnswer, String correctAnswerValue) {
        if (studentAnswer == null || !isRangeCorrectAnswer(correctAnswerValue)) {
            return false;
        }
        String[] parts = correctAnswerValue.split("\\|", -1);
        if (parts.length != 3) {
            return false;
        }
        try {
            BigDecimal student = new BigDecimal(studentAnswer.trim());
            BigDecimal min = new BigDecimal(parts[1].trim());
            BigDecimal max = new BigDecimal(parts[2].trim());
            return min.compareTo(max) <= 0
                    && student.compareTo(min) >= 0
                    && student.compareTo(max) <= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 按填空答案契约比较学生答案：新数据使用范围，旧精确值按等值范围处理。
     */
    public static boolean isFillBlankAnswerCorrect(String studentAnswer, String correctAnswerValue) {
        if (studentAnswer == null || correctAnswerValue == null) {
            return false;
        }
        if (isRangeCorrectAnswer(correctAnswerValue)) {
            return isAnswerWithinRange(studentAnswer, correctAnswerValue);
        }
        try {
            return new BigDecimal(studentAnswer.trim())
                    .compareTo(new BigDecimal(correctAnswerValue.trim())) == 0;
        } catch (NumberFormatException e) {
            return studentAnswer.trim().equalsIgnoreCase(correctAnswerValue.trim());
        }
    }

    private static BigDecimal parseRangeBoundary(String boundary, String fieldLabel) {
        try {
            return new BigDecimal(boundary.trim());
        } catch (NumberFormatException e) {
            throw new BusinessException(400, fieldLabel + "必须是有效数字");
        }
    }
}
