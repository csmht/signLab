package com.example.demo.pojo.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;

/**
 * 题目导入Excel实体类
 * 用于规范Excel导入格式
 * 支持单选题、多选题、判断题的批量导入
 */
@Data
@ContentRowHeight(25)
@HeadRowHeight(30)
public class TopicImportExcel {

    /**
     * 课程标签（学科标签）
     */
    @ExcelProperty(value = "课程标签", index = 0)
    @ColumnWidth(20)
    private String courseTag;

    /**
     * 难度标签
     */
    @ExcelProperty(value = "难度标签", index = 1)
    @ColumnWidth(15)
    private String difficultyTag;

    /**
     * 自定义标签
     */
    @ExcelProperty(value = "自定义标签", index = 2)
    @ColumnWidth(20)
    private String customTag;

    /**
     * 题目类型：单选题、多选题、判断题
     */
    @ExcelProperty(value = "题目类型", index = 3)
    @ColumnWidth(15)
    private String topicType;

    /**
     * 题目内容
     */
    @ExcelProperty(value = "题目内容", index = 4)
    @ColumnWidth(40)
    private String content;

    /**
     * 题目答案
     * 单选题：A/B/C/D
     * 多选题：A-B-C-D（用横杠分隔）
     * 判断题：A（正确）/B（错误）
     */
    @ExcelProperty(value = "题目答案", index = 5)
    @ColumnWidth(15)
    private String correctAnswer;

    /**
     * 选项（JSON格式）
     * 格式：{"A":"选项A","B":"选项B","C":"选项C","D":"选项D"}
     */
    @ExcelProperty(value = "选项", index = 6)
    @ColumnWidth(60)
    private String choices;
}