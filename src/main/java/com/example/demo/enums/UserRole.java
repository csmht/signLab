package com.example.demo.enums;

public enum UserRole {
    ADMIN("admin", "管理员"),
    TEACHER("teacher", "教师"),
    STUDENT("student", "学生");

    private final String code;
    private final String description;

    UserRole(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static UserRole fromCode(String code) {
        for (UserRole role : UserRole.values()) {
            if (role.code.equals(code)) {
                return role;
            }
        }
        throw new IllegalArgumentException("未知角色: " + code);
    }

    public String getAuthority() {
        return "ROLE_" + name();
    }
}