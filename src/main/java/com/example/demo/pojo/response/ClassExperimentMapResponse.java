package com.example.demo.pojo.response;

import com.example.demo.pojo.dto.mapvo.CourseExperimentItem;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 班级实验响应
 * 返回按课程ID分组的实验详情列表
 */
@Data
public class ClassExperimentMapResponse {

    /**
     * 课程实验列表
     */
    private List<CourseExperimentItem> courseExperiments = new ArrayList<>();
}
