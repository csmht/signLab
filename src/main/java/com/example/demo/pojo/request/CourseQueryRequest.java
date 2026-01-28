package com.example.demo.pojo.request;

import lombok.Data;

/**
 * 课程查询请求
 * 用于课程的查询、分页查询
 */
@Data
public class CourseQueryRequest {

    /**
     * 当前页码
     */
    private Integer current = 1;

    /**
     * 每页条数
     */
    private Integer size = 10;

    /**
     * 课程ID（精确查询）
     */
    private String courseId;

    /**
     * 课程名称（模糊查询）
     */
    private String courseName;

    /**
     * 是否只查询当前教师的课程
     */
    private Boolean myOnly = false;

    /**
     * 是否分页查询（true-分页，false-列表）
     */
    private Boolean pageable = true;
}
