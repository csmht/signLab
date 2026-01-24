package com.example.demo.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tangzc.autotable.annotation.AutoTable;
import com.tangzc.autotable.annotation.TableIndex;
import com.tangzc.autotable.annotation.enums.IndexTypeEnum;
import com.tangzc.mpe.autotable.annotation.Column;
import com.tangzc.mpe.autotable.annotation.Table;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AutoTable
@Table(value = "assignment_submissions", comment = "作业提交表")
@TableName("assignment_submissions")
@TableIndex(name = "uk_student_course", fields = {"studentUsername", "courseId"}, type = IndexTypeEnum.UNIQUE)
@TableIndex(name = "idx_course_status", fields = {"courseId", "submissionStatus"})
@TableIndex(name = "idx_teacher", fields = {"teacherUsername"})
@TableIndex(name = "idx_submission_time", fields = {"submissionTime"})
public class AssignmentSubmission {

    @TableId(type = IdType.AUTO)
    @Column(comment = "提交ID")
    private Long id;

    @Column(comment = "学生学号", type = "varchar(50)", notNull = true)
    private String studentUsername;

    @Column(comment = "课程ID", type = "varchar(50)", notNull = true)
    private String courseId;

    @Column(comment = "提交类型：课堂笔记、作业、报告", type = "enum('class_notes', 'homework', 'report')", notNull = true, defaultValue = "'class_notes'")
    private String submissionType;

    @Column(comment = "原始文件名", type = "varchar(255)", notNull = true)
    private String originalFileName;

    @Column(comment = "存储文件名", type = "varchar(255)", notNull = true)
    private String storedFileName;

    @Column(comment = "文件存储路径", type = "varchar(500)", notNull = true)
    private String filePath;

    @Column(comment = "文件大小(字节)", type = "bigint", notNull = true)
    private Long fileSize;

    @Column(comment = "提交状态：草稿、已提交、已批改、已退回", type = "enum('draft', 'submitted', 'reviewed', 'returned')", notNull = true, defaultValue = "'draft'")
    private String submissionStatus;

    @Column(comment = "提交时间", type = "datetime")
    private LocalDateTime submissionTime;

    @Column(comment = "批改时间", type = "datetime")
    private LocalDateTime reviewTime;

    @Column(comment = "批改教师用户名", type = "varchar(50)")
    private String teacherUsername;

    @Column(comment = "教师评语", type = "text")
    private String teacherComment;

    @Column(comment = "创建时间", type = "datetime", defaultValue = "CURRENT_TIMESTAMP")
    private LocalDateTime createdTime;

    @Column(comment = "更新时间", type = "datetime", defaultValue = "CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedTime;
}