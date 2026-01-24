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
@Table(value = "multi_class_courses", comment = "多班课程关联表")
@TableName("multi_class_courses")
@TableIndex(name = "uk_course_class", fields = {"courseId", "classCode"}, type = IndexTypeEnum.UNIQUE)
@TableIndex(name = "idx_course_id", fields = {"courseId"})
@TableIndex(name = "idx_class_code", fields = {"classCode"})
@TableIndex(name = "idx_teacher_username", fields = {"teacherUsername"})
public class MultiClassCourse {

    @TableId(type = IdType.AUTO)
    @Column(comment = "主键ID")
    private Long id;

    @Column(comment = "课程ID", type = "varchar(20)", notNull = true)
    private String courseId;

    @Column(comment = "班级代码", type = "varchar(20)", notNull = true)
    private String classCode;

    @Column(comment = "教师用户名", type = "varchar(20)", notNull = true)
    private String teacherUsername;

    @Column(comment = "创建时间", type = "timestamp", defaultValue = "CURRENT_TIMESTAMP")
    private LocalDateTime createTime;

    @Column(comment = "更新时间", type = "timestamp", defaultValue = "CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updateTime;

    @Column(comment = "是否删除：0-未删除，1-已删除", type = "tinyint", defaultValue = "0")
    private Integer isDeleted;
}