package com.example.demo.pojo.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;

/**
 * 考勤记录导出Excel实体类
 * 用于导出考勤记录数据
 */
@Data
@ContentRowHeight(20)
@HeadRowHeight(30)
public class AttendanceRecordExportExcel {

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
     * 实验ID
     */
    @ExcelProperty(value = "实验编号", index = 3)
    @ColumnWidth(15)
    private String experimentId;

    /**
     * 考勤时间
     */
    @ExcelProperty(value = "考勤时间", index = 4)
    @ColumnWidth(20)
    private String attendanceTime;

    /**
     * 考勤状态
     */
    @ExcelProperty(value = "考勤状态", index = 5)
    @ColumnWidth(15)
    private String attendanceStatus;

    /**
     * 学生实际所在班级
     */
    @ExcelProperty(value = "实际班级", index = 6)
    @ColumnWidth(20)
    private String studentActualClassCode;

    /**
     * IP地址
     */
    @ExcelProperty(value = "IP地址", index = 7)
    @ColumnWidth(15)
    private String ipAddress;
}
