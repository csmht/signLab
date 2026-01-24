package com.example.demo.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tangzc.autotable.annotation.AutoTable;

import com.tangzc.autotable.annotation.TableIndex;
import com.tangzc.mpe.autotable.annotation.Column;
import com.tangzc.mpe.autotable.annotation.Table;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AutoTable
@Table(value = "student_documents", comment = "学生文档表")
@TableName("student_documents")
@TableIndex(name = "idx_documents_course_id", fields = {"courseId"})
@TableIndex(name = "idx_documents_student_username", fields = {"studentUsername"})
public class StudentDocument {

    @TableId(type = IdType.AUTO)
    private Long id;

    @Column(comment = "课程ID", type = "varchar(20)", notNull = true)
    private String courseId;

    @Column(comment = "学生用户名(学号)", type = "varchar(50)", notNull = true)
    private String studentUsername;

    @Column(comment = "文档文件名", type = "varchar(200)", notNull = true)
    private String documentName;

    @Column(comment = "文档存储路径", type = "varchar(500)", notNull = true)
    private String documentPath;

    @Column(comment = "文档文件大小，单位：字节", type = "bigint")
    private Long fileSize;

    @Column(comment = "文档导出时间", type = "datetime", defaultValue = "CURRENT_TIMESTAMP")
    private LocalDateTime exportTime;

    @Column(comment = "创建时间", type = "datetime", defaultValue = "CURRENT_TIMESTAMP")
    private LocalDateTime createTime;

    @Column(comment = "更新时间", type = "datetime", defaultValue = "CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updateTime;

    @Column(comment = "是否删除", type = "tinyint", defaultValue = "0")
    private Integer isDeleted;
}