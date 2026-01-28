package com.example.demo.pojo.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课程信息响应
 */
@Data
public class CourseResponse {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 课程ID
     */
    private String courseId;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 授课教师用户名
     */
    private String teacherUsername;

    /**
     * 教师工号
     */
    private String teacherEmployeeId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
