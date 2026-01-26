package com.example.demo.pojo.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 班级信息响应
 */
@Data
public class ClassInfoResponse {

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
     * 绑定时间
     */
    private LocalDateTime bindTime;

    /**
     * 班级人数
     */
    private Integer studentCount;
}
