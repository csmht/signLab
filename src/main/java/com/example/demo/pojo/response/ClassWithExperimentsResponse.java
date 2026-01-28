package com.example.demo.pojo.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 班级带实验信息响应DTO
 */
@Data
public class ClassWithExperimentsResponse {

    /**
     * 班级ID
     */
    private Long id;

    /**
     * 班级编号
     */
    private String classCode;

    /**
     * 班级名称
     */
    private String className;



    /**
     * 班级人数
     */
    private Integer studentCount;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 实验列表
     */
    private List<ExperimentInfo> experiments;

    /**
     * 实验信息内部类
     */
    @Data
    public static class ExperimentInfo {
        /**
         * 班级实验ID
         */
        private Long classExperimentId;

        /**
         * 课程ID
         */
        private String courseId;

        /**
         * 实验ID
         */
        private String experimentId;

        /**
         * 实验名称
         */
        private String experimentName;

        /**
         * 上课时间（例如：8:00-14:00）
         */
        private String courseTime;

        /**
         * 实验开始填写时间
         */
        private LocalDateTime startTime;

        /**
         * 实验结束填写时间
         */
        private LocalDateTime endTime;

        /**
         * 实验地点
         */
        private String experimentLocation;

        /**
         * 授课老师用户名
         */
        private String userName;
    }

}