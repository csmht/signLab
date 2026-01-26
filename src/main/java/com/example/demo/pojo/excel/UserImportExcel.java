package com.example.demo.pojo.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;

/**
 * 用户导入Excel实体类
 * 用于规范Excel导入格式
 * 只包含用户基本信息：用户名、姓名、角色、院系、专业
 */
@Data
@ContentRowHeight(20)
@HeadRowHeight(30)
public class UserImportExcel {

    /**
     * 用户名（学号/工号），必须唯一
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
     * 用户角色：student（学生）、teacher（教师）、admin（管理员）
     */
    @ExcelProperty(value = "角色", index = 2)
    @ColumnWidth(15)
    private String role;

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