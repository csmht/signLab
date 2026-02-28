package com.example.demo.pojo.ao;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ClassCourseExperimentAo {
    /**
     * 日期
     */
    private LocalDate data;

    /**
     * 班级
     */
    private String className;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 项目(实验)名称
     */
    private String experimentName;

    /**
     * 实验地点
     */
    private String laboratory;

    /**
     * 教师 用户名
     */
    private String teacherUsername;

    /**
     * 时间
     */
    private LocalDateTime time;
}
