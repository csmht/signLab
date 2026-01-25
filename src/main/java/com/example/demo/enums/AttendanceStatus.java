package com.example.demo.enums;

/**
 * 签到状态枚举
 */
public enum AttendanceStatus {

    NORMAL("正常", "正常签到"),
    LATE("迟到", "迟到签到"),
    MAKEUP("补签", "补签"),
    CROSS_CLASS("跨班签到", "跨班签到");

    private final String code;
    private final String description;

    AttendanceStatus(String code, String description) {
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
     * 根据code获取枚举
     */
    public static AttendanceStatus fromCode(String code) {
        for (AttendanceStatus status : AttendanceStatus.values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知签到状态: " + code);
    }
}