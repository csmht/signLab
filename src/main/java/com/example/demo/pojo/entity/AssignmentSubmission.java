package com.example.demo.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tangzc.autotable.annotation.AutoTable;
import com.tangzc.mpe.autotable.annotation.Column;
import com.tangzc.mpe.autotable.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 作业提交表
 * 记录学生提交的作业文件信息
 */
@Data
@AutoTable
@Table(value = "assignment_submissions", comment = "作业提交表 - 记录学生提交的作业文件信息")
@TableName("assignment_submissions")
public class AssignmentSubmission {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 课程ID */
    @Column(comment = "课程ID", type = "varchar(20)", notNull = true)
    private String courseId;

    /** 实验ID */
    @Column(comment = "实验ID", type = "varchar(20)", notNull = true)
    private String experimentId;

    /** 学生用户名(学号) */
    @Column(comment = "学生用户名(学号)", type = "varchar(50)", notNull = true)
    private String studentUsername;

    /** 提交类型 */
    @Column(comment = "提交类型（实验报告、数据文件等）", type = "varchar(50)", notNull = true)
    private String submissionType;

    /** 文件路径 */
    @Column(comment = "文件存储路径", type = "varchar(500)", notNull = true)
    private String filePath;

    /** 文件名 */
    @Column(comment = "文件名", type = "varchar(200)", notNull = true)
    private String fileName;

    /** 文件大小（字节） */
    @Column(comment = "文件大小（字节）", type = "bigint")
    private Long fileSize;

    /** 提交状态：draft-草稿，submitted-已提交，graded-已批改 */
    @Column(comment = "提交状态", type = "varchar(20)", defaultValue = "draft")
    private String submissionStatus;

    /** 教师评语 */
    @Column(comment = "教师评语", type = "text")
    private String teacherComment;

    /** 评分 */
    @Column(comment = "评分", type = "decimal(5,2)")
    private java.math.BigDecimal score;

    /** 提交时间 */
    @Column(comment = "提交时间", type = "datetime")
    private LocalDateTime submissionTime;

    /** 创建时间 */
    @Column(comment = "创建时间", type = "datetime", defaultValue = "CURRENT_TIMESTAMP")
    private LocalDateTime createTime;

    /** 更新时间 */
    @Column(comment = "更新时间", type = "datetime", defaultValue = "CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updateTime;

    /** 是否删除 */
    @Column(comment = "是否删除", type = "tinyint", defaultValue = "0")
    private Integer isDeleted;
}
