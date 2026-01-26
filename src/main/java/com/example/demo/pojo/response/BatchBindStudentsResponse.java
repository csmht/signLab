package com.example.demo.pojo.response;

import lombok.Data;

import java.util.List;

/**
 * 批量绑定学生到班级响应DTO
 */
@Data
public class BatchBindStudentsResponse {

    /**
     * 班级编号
     */
    private String classCode;

    /**
     * 班级名称
     */
    private String className;

    /**
     * 成功绑定的学生数量
     */
    private Integer successCount;

    /**
     * 失败的学生数量
     */
    private Integer failCount;

    /**
     * 成功绑定的学生列表
     */
    private List<StudentResult> successList;

    /**
     * 失败的学生列表
     */
    private List<StudentResult> failList;

    /**
     * 学生结果内部类
     */
    @Data
    public static class StudentResult {
        /**
         * 学生用户名（学号）
         */
        private String studentUsername;

        /**
         * 学生姓名
         */
        private String studentName;

        /**
         * 是否成功
         */
        private Boolean success;

        /**
         * 错误信息（失败时）
         */
        private String message;
    }

}