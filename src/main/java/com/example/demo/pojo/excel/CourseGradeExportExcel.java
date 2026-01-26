package com.example.demo.pojo.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 课程成绩导出Excel实体类
 * 用于导出课程成绩数据
 */
@Data
@ContentRowHeight(20)
@HeadRowHeight(30)
public class CourseGradeExportExcel {

    /**
     * 学生学号
     */
    @ExcelProperty(value = "学号", index = 0)
    @ColumnWidth(20)
    private String studentUsername;

    /**
     * 学生姓名
     */
    @ExcelProperty(value = "姓名", index = 1)
    @ColumnWidth(15)
    private String studentName;

    /**
     * 课程ID
     */
    @ExcelProperty(value = "课程编号", index = 2)
    @ColumnWidth(15)
    private String courseId;

    /**
     * 课程名称
     */
    @ExcelProperty(value = "课程名称", index = 3)
    @ColumnWidth(20)
    private String courseName;

    /**
     * 成绩
     */
    @ExcelProperty(value = "成绩", index = 4)
    @ColumnWidth(10)
    private String grade;

    /**
     * 数字成绩
     */
    @ExcelProperty(value = "分数", index = 5)
    @ColumnWidth(10)
    private BigDecimal gradeNumeric;

    /**
     * 成绩类型
     */
    @ExcelProperty(value = "成绩类型", index = 6)
    @ColumnWidth(15)
    private String gradeType;

    /**
     * 教师工号
     */
    @ExcelProperty(value = "教师工号", index = 7)
    @ColumnWidth(15)
    private String teacherUsername;

    /**
     * 教师姓名
     */
    @ExcelProperty(value = "任课教师", index = 8)
    @ColumnWidth(15)
    private String teacherName;

    /**
     * 教师评语
     */
    @ExcelProperty(value = "教师评语", index = 9)
    @ColumnWidth(30)
    private String teacherComment;

    /**
     * 学期
     */
    @ExcelProperty(value = "学期", index = 10)
    @ColumnWidth(15)
    private String semester;

    /**
     * 成绩打分时间
     */
    @ExcelProperty(value = "打分时间", index = 11)
    @ColumnWidth(20)
    private String gradeTime;

    /**
     * 是否已审核
     */
    @ExcelProperty(value = "是否审核", index = 12)
    @ColumnWidth(10)
    private String isApproved;
}
