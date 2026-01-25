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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 课程表
 * 存储课程信息，包含教师工号和精确时间
 */
@Data
@AutoTable
@Table(value = "courses", comment = "课程表 - 存储课程信息，包含教师工号和精确时间")
@TableName("courses")
@TableIndex(name = "uk_course_id", fields = {"courseId"}, type = IndexTypeEnum.UNIQUE)
@TableIndex(name = "idx_courses_teacher_username", fields = {"teacherUsername"})
@TableIndex(name = "idx_courses_class_code", fields = {"classCode"})
@TableIndex(name = "idx_courses_course_date", fields = {"courseDate"})
@TableIndex(name = "idx_courses_teacher_employee_id", fields = {"teacherEmployeeId"})
@TableIndex(name = "idx_courses_start_time", fields = {"startTime"})
@TableIndex(name = "idx_courses_end_time", fields = {"endTime"})
public class Course {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 课程ID */
    @Column(comment = "课程ID", type = "varchar(20)", notNull = true)
    private String courseId;

    /** 课程名称 */
    @Column(comment = "课程名称", type = "varchar(200)", notNull = true)
    private String courseName;

    /** 授课教师用户名 */
    @Column(comment = "授课教师用户名", type = "varchar(50)", notNull = true)
    private String teacherUsername;

    /** 教师工号 */
    @Column(comment = "教师工号", type = "varchar(20)")
    private String teacherEmployeeId;

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