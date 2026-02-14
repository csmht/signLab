package com.example.demo.pojo.request.teacher;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 导出课程数据请求类
 * 用于导出指定课程和班级的成绩或考勤数据
 */
@Data
public class ExportCourseDataRequest {

    /**
     * 课程ID
     */
    @NotBlank(message = "课程ID不能为空")
    private String courseId;

    /**
     * 班级编号列表（支持多选）
     */
    @NotEmpty(message = "班级编号列表不能为空")
    private List<String> classCodes;
}
