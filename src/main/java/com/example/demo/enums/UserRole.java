package com.example.demo.enums;

public enum UserRole {
    ADMIN("admin", "管理员", 3),
    TEACHER("teacher", "教师", 2),
    STUDENT("student", "学生", 1);

    private final String code;
    private final String description;
    private final int level;

    UserRole(String code, String description, int level) {
        this.code = code;
        this.description = description;
        this.level = level;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public int getLevel() {
        return level;
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

    /**
     * 检查当前角色是否满足要求的角色（层级权限检查）
     * 规则：当前角色级别 >= 要求角色级别，则满足权限
     *
     * @param requiredRole 要求的角色
     * @return 是否满足权限
     */
    public boolean hasPermission(UserRole requiredRole) {
        return this.level >= requiredRole.level;
    }
}