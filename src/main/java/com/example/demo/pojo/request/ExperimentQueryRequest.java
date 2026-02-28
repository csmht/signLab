package com.example.demo.pojo.request;

import lombok.Data;

/**
 * 实验查询请求
 * 用于实验的查询、分页查询
 */
@Data
public class ExperimentQueryRequest {

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
     * 实验名称（模糊查询）
     */
    private String experimentName;

    /**
     * 是否分页查询（true-分页，false-列表）
     */
    private Boolean pageable = true;
}
