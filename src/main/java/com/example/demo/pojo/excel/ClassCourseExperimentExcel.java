package com.example.demo.pojo.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;

@Data
@ContentRowHeight(20)
@HeadRowHeight(30)
public class ClassCourseExperimentExcel {

    /**
     * 日期
     */
    @ExcelProperty(value = "日期", index = 0)
    @ColumnWidth(20)
    private String data;

    /**
     * 班级
     */
    @ExcelProperty(value = "班级名称", index = 1)
    @ColumnWidth(20)
    private String className;


    /**
     * 课程名称
     */
    @ExcelProperty(value = "课程名称",index = 2)
    @ColumnWidth(30)
    private String courseName;

    /**
     * 项目(实验)名称
     */
    @ExcelProperty(value = "项目(实验)名称", index = 3)
    @ColumnWidth(30)
    private String experimentName;

    /**
     * 实验楼
     */
    @ExcelProperty(value = "实验楼" ,index = 4)
    @ColumnWidth(20)
    private String laboratory;

    /**
     * 实验室
     */
    @ExcelProperty(value = "实验室",index = 5)
    @ColumnWidth(20)
    private String laboratory2;

    /**
     * 教师名字
     */
    @ExcelProperty(value = "教师名字",index = 6)
    @ColumnWidth(20)
    private String teacherName;

    /**
     * 时间
     */
    @ExcelProperty(value = "时间",index = 7)
    @ColumnWidth(20)
    private String time;

}
