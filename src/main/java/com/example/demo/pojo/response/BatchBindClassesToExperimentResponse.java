package com.example.demo.pojo.response;

import lombok.Data;

import java.util.List;

/**
 * 批量绑定班级到实验响应DTO
 */
@Data
public class BatchBindClassesToExperimentResponse {

    /**
     * 课程ID
     */
    private String courseId;

    /**
     * 实验ID
     */
    private String experimentId;

    /**
     * 成功绑定的班级数量
     */
    private Integer successCount;

    /**
     * 失败的班级数量
     */
    private Integer failCount;

    /**
     * 成功绑定的班级列表
     */
    private List<ClassResult> successList;

    /**
     * 失败的班级列表
     */
    private List<ClassResult> failList;

    /**
     * 班级结果内部类
     */
    @Data
    public static class ClassResult {
        /**
         * 班级编号
         */
        private String classCode;

        /**
         * 班级名称
         */
        private String className;

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