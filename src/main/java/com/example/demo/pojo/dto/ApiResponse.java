package com.example.demo.pojo.dto;

import lombok.Data;

/**
 * 统一API响应格式
 * 用于标准化所有接口的返回数据结构
 *
 * @param <T> 数据类型
 */
@Data
public class ApiResponse<T> {

    /** 响应状态码 */
    private Integer code;

    /** 响应消息 */
    private String message;

    /** 响应数据 */
    private T data;

    public ApiResponse() {
    }

    public ApiResponse(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 成功响应（默认消息）
     *
     * @param data 响应数据
     * @return 成功响应对象
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "操作成功", data);
    }

    /**
     * 成功响应（自定义消息）
     *
     * @param data 响应数据
     * @param message 响应消息
     * @return 成功响应对象
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(200, message, data);
    }

    /**
     * 错误响应
     *
     * @param code 错误码
     * @param message 错误消息
     * @return 错误响应对象
     */
    public static <T> ApiResponse<T> error(Integer code, String message) {
        return new ApiResponse<>(code, message, null);
    }

    /**
     * 错误响应（默认错误码500）
     *
     * @param message 错误消息
     * @return 错误响应对象
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(500, message, null);
    }
}