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
@Table(value = "attendance_records", comment = "签到记录表")
@TableName("attendance_records")
@TableIndex(name = "uk_course_student", fields = {"courseId", "studentUsername"}, type = IndexTypeEnum.UNIQUE)
@TableIndex(name = "idx_attendance_course_id", fields = {"courseId"})
@TableIndex(name = "idx_attendance_student_username", fields = {"studentUsername"})
@TableIndex(name = "idx_student_actual_class", fields = {"studentActualClassCode"})
@TableIndex(name = "idx_is_cross_class", fields = {"isCrossClass"})
public class AttendanceRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    @Column(comment = "课程ID", type = "varchar(20)", notNull = true)
    private String courseId;

    @Column(comment = "实验ID", type = "varchar(20)", notNull = true)
    private String experimentId;

    @Column(comment = "学生用户名(学号)", type = "varchar(50)", notNull = true)
    private String studentUsername;

    @Column(comment = "签到时间", type = "datetime", notNull = true)
    private LocalDateTime attendanceTime;

    @Column(comment = "签到状态：正常，补签，迟到，跨班签到", type = "char(20)", defaultValue = "正常")
    private String attendanceStatus;

    @Column(comment = "学生实际所在班级代码", type = "varchar(20)")
    private String studentActualClassCode;

    @Column(comment = "签到时的IP地址", type = "varchar(50)")
    private String ipAddress;

    @Column(comment = "创建时间", type = "datetime", defaultValue = "CURRENT_TIMESTAMP")
    private LocalDateTime createTime;

    @Column(comment = "更新时间", type = "datetime", defaultValue = "CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updateTime;

    @Column(comment = "是否删除", type = "tinyint", defaultValue = "0")
    private Integer isDeleted;
}