package com.example.demo.enums;

/**
 * 标签类型枚举
 * 定义题目标签的分类
 */
public enum TagType {
    SUBJECT("1", "学科标签"),
    DIFFICULTY("2", "难度标签"),
    QUESTION_TYPE("3", "题型标签"),
    CUSTOM("4", "自定义标签");

    private final String code;
    private final String description;

    TagType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据代码获取标签类型
     *
     * @param code 类型代码
     * @return 标签类型
     */
    public static TagType fromCode(String code) {
        for (TagType type : TagType.values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的标签类型: " + code);
    }

    /**
     * 根据代码获取标签类型描述
     *
     * @param code 类型代码
     * @return 类型描述
     */
    public static String getDescriptionByCode(String code) {
        try {
            return fromCode(code).getDescription();
        } catch (IllegalArgumentException e) {
            return "未知类型";
        }
    }
}
