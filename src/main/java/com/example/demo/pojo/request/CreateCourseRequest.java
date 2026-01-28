package com.example.demo.pojo.request;

import lombok.Data;

/**
 * 创建课程请求
 */
@Data
public class CreateCourseRequest {

    /**
     * 课程ID
     */
    private String courseId;

    /**
     * 课程名称
     */
    private String courseName;
}
