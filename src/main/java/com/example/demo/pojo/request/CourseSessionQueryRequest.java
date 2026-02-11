package com.example.demo.pojo.request;

import lombok.Data;

/**
 * 课次查询请求
 * 用于课次的查询、分页查询
 */
@Data
public class CourseSessionQueryRequest {

    /**
     * 当前页码
     */
    private Integer current = 1;

    /**
     * 每页条数
     */
    private Integer size = 10;

    /**
     * 班级编号（精确查询）
     */
    private String classCode;

    /**
     * 课程ID（精确查询）
     */
    private String courseId;

    /**
     * 是否分页查询（true-分页，false-列表）
     */
    private Boolean pageable = true;

    /**
     * 实验开始时间-开始日期（yyyy-MM-dd）
     */
    private String startDate;

    /**
     * 实验开始时间-结束日期（yyyy-MM-dd）
     */
    private String endDate;
}
