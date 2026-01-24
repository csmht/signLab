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

    @TableId(type = IdType.AUTO)
    private Long id;

    @Column(comment = "课程ID", type = "varchar(20)", notNull = true)
    private String courseId;

    @Column(comment = "课程名称", type = "varchar(200)", notNull = true)
    private String courseName;

    @Column(comment = "授课教师用户名", type = "varchar(50)", notNull = true)
    private String teacherUsername;

    @Column(comment = "上课班级编号", type = "varchar(20)", notNull = true)
    private String classCode;

    @Column(comment = "上课地点", type = "varchar(100)")
    private String location;

    @Column(comment = "课程日期", type = "date", notNull = true)
    private String courseDate;

    @Column(comment = "上课时间段，格式如：08:00-09:40", type = "varchar(20)", notNull = true)
    private String timeSlot;

    @Column(comment = "教师工号", type = "varchar(20)")
    private String teacherEmployeeId;

    @Column(comment = "开始时间", type = "time")
    private LocalTime startTime;

    @Column(comment = "结束时间", type = "time")
    private LocalTime endTime;

    @Column(comment = "创建时间", type = "datetime", defaultValue = "CURRENT_TIMESTAMP")
    private LocalDateTime createTime;

    @Column(comment = "更新时间", type = "datetime", defaultValue = "CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updateTime;

    @Column(comment = "是否删除", type = "tinyint", defaultValue = "0")
    private Integer isDeleted;
}