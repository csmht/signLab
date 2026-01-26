package com.example.demo.enums;

import lombok.Getter;

/**
 * 步骤不可做原因枚举
 */
@Getter
public enum ProcedureAccessDeniedReason {

    /**
     * 前置步骤未完成
     */
    PREVIOUS_NOT_COMPLETED("前置步骤未完成"),

    /**
     * 未到开始时间
     */
    NOT_STARTED("未到开始时间"),

    /**
     * 已过结束时间
     */
    EXPIRED("已过结束时间"),

    /**
     * 步骤已完成（可重做场景）
     */
    ALREADY_COMPLETED("已完成"),

    /**
     * 可访问
     */
    ACCESSIBLE("可访问");

    private final String description;

    ProcedureAccessDeniedReason(String description) {
        this.description = description;
    }
}
