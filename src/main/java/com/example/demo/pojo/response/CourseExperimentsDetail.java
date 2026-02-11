package com.example.demo.pojo.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 课程实验详情响应
 * 包含课程信息及该课程下的实验列表
 */
@Data
public class CourseExperimentsDetail {

    /**
     * 课程信息
     */
    private CourseInfo courseInfo;

    /**
     * 该课程下的实验列表
     */
    private List<ExperimentDetailItem> experiments;

    /**
     * 课程信息
     */
    @Data
    public static class CourseInfo {
        /**
         * 课程ID
         */
        private String courseId;

        /**
         * 课程名称
         */
        private String courseName;

        /**
         * 教师用户名
         */
        private String teacherUsername;
    }

    /**
     * 实验详情项
     */
    @Data
    public static class ExperimentDetailItem {
        /**
         * 班级实验ID
         */
        private Long classExperimentId;

        /**
         * 实验ID
         */
        private Long experimentId;

        /**
         * 实验名称
         */
        private String experimentName;

        /**
         * 实验占比
         */
        private Integer percentage;

        /**
         * 上课时间（例如：8:00-14:00）
         */
        private String courseTime;

        /**
         * 实验开始时间
         */
        private LocalDateTime startTime;

        /**
         * 实验结束时间
         */
        private LocalDateTime endTime;

        /**
         * 实验地点
         */
        private String experimentLocation;

        /**
         * 授课教师用户名
         */
        private String userName;

        /**
         * 授课教师姓名
         */
        private String teacherName;
    }
}
