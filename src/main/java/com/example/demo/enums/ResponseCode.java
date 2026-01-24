package com.example.demo.enums;

import lombok.Getter;

@Getter
public enum ResponseCode {

    SUCCESS(200, "操作成功"),

    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),
    CONFLICT(409, "资源冲突"),
    VALIDATION_ERROR(422, "参数校验失败"),

    INTERNAL_SERVER_ERROR(500, "服务器内部错误"),
    SERVICE_UNAVAILABLE(503, "服务不可用"),

    USER_NOT_FOUND(1001, "用户不存在"),
    USER_ALREADY_EXISTS(1002, "用户已存在"),
    PASSWORD_ERROR(1003, "密码错误"),
    USER_DISABLED(1004, "用户已被禁用"),
    COURSE_NOT_FOUND(2001, "课程不存在"),
    COURSE_ALREADY_EXISTS(2002, "课程已存在"),
    ATTENDANCE_NOT_FOUND(3001, "签到记录不存在"),
    ATTENDANCE_ALREADY_EXISTS(3002, "签到记录已存在"),
    QR_CODE_EXPIRED(3003, "二维码已过期"),
    QR_CODE_INVALID(3004, "二维码无效"),
    FILE_UPLOAD_FAILED(4001, "文件上传失败"),
    FILE_NOT_FOUND(4002, "文件不存在"),
    EXCEL_IMPORT_FAILED(5001, "Excel导入失败"),
    EXCEL_EXPORT_FAILED(5002, "Excel导出失败");

    private final int code;
    private final String message;

    ResponseCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}