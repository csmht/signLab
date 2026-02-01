package com.example.demo.pojo.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;

/**
 * 学生班级导入Excel实体类
 * 用于规范Excel导入格式，同时导入学生和班级信息
 * 班级编号由系统自动生成，无需在Excel中填写
 */
@Data
@ContentRowHeight(20)
@HeadRowHeight(30)
public class StudentClassImportExcel {

    /**
     * 用户名（学号），必须唯一
     */
    @ExcelProperty(value = "用户名", index = 0)
    @ColumnWidth(20)
    private String username;

    /**
     * 用户姓名
     */
    @ExcelProperty(value = "姓名", index = 1)
    @ColumnWidth(15)
    private String name;

    /**
     * 班级名称
     */
    @ExcelProperty(value = "班级名称", index = 2)
    @ColumnWidth(25)
    private String className;

    /**
     * 院系
     */
    @ExcelProperty(value = "院系", index = 3)
    @ColumnWidth(20)
    private String department;

    /**
     * 专业
     */
    @ExcelProperty(value = "专业", index = 4)
    @ColumnWidth(20)
    private String major;
}
