package com.example.demo.pojo.dto.mapvo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 接口返回的正确答案范围。
 */
@Data
@AllArgsConstructor
public class AnswerRange {

    /** 最小值（包含边界） */
    private String min;

    /** 最大值（包含边界） */
    private String max;

    public static AnswerRange fromStoredValue(String storedValue) {
        DataField field = new DataField();
        field.applyCorrectAnswerValue(storedValue);
        return new AnswerRange(field.getMin(), field.getMax());
    }

    public static AnswerRange fromExactValue(String storedValue) {
        return new AnswerRange(storedValue, storedValue);
    }
}
