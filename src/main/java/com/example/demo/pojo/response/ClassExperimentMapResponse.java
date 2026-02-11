package com.example.demo.pojo.response;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 班级实验Map响应
 * 返回按课程ID分组的实验详情（嵌套Map结构）
 */
@Data
public class ClassExperimentMapResponse {

    /**
     * 课程ID -> 课程实验详情
     * 使用LinkedHashMap保持插入顺序
     */
    private Map<String, CourseExperimentsDetail> courseExperiments = new LinkedHashMap<>();
}
