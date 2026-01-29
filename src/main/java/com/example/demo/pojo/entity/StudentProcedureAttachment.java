package com.example.demo.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.tangzc.autotable.annotation.AutoTable;
import com.tangzc.autotable.annotation.TableIndex;
import com.tangzc.mpe.autotable.annotation.Column;
import com.tangzc.mpe.autotable.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学生步骤附件表
 * 统一存储学生提交的照片、文档等文件信息
 */
@Data
@AutoTable
@Table(value = "student_procedure_attachments", comment = "学生步骤附件表 - 统一存储学生提交的照片、文档等文件")
@TableName("student_procedure_attachments")
@TableIndex(name = "idx_procedure_id", fields = {"procedureId"})
public class StudentProcedureAttachment {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 实验步骤ID */
    @Column(comment = "实验步骤ID", type = "bigint")
    private Long procedureId;

    /** 学生用户名 */
    @Column(comment = "学生用户名", type = "varchar(50)")
    private String studentUsername;

    /** 班级编号 */
    @Column(comment = "班级编号", type = "varchar(20)")
    private String classCode;

    /** 班级实验ID（用于合班上课场景） */
    @Column(comment = "班级实验ID（用于合班上课场景）", type = "bigint")
    private Long classExperimentId;

    /** 文件类型（1-照片，2-文档） */
    @Column(comment = "文件类型（1-照片，2-文档）", type = "int")
    private Integer fileType;

    /** 文件格式（如jpg、png、pdf、docx等） */
    @Column(comment = "文件格式", type = "varchar(20)")
    private String fileFormat;

    /** 原始文件名 */
    @Column(comment = "原始文件名", type = "varchar(255)")
    private String originalFileName;

    /** 存储文件名 */
    @Column(comment = "存储文件名", type = "varchar(255)")
    private String storedFileName;

    /** 文件存储路径 */
    @Column(comment = "文件存储路径", type = "varchar(255)")
    private String filePath;

    /** 文件大小（字节） */
    @Column(comment = "文件大小（字节）", type = "bigint")
    private Long fileSize;

    /** 文件备注 */
    @Column(comment = "文件备注", type = "varchar(255)")
    private String remark;

    /** 创建时间 */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @Column(comment = "创建时间", type = "datetime", defaultValue = "CURRENT_TIMESTAMP")
    private LocalDateTime createTime;

    /** 是否删除：0-未删除，1-已删除 */
    @TableLogic
    @Column(comment = "是否删除：0-未删除，1-已删除", type = "int")
    private Integer isDeleted;
}
