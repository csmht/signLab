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

/**
 * 签到记录表
 * 记录学生的签到信息
 */
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

    /** 签到时间 */
    @Column(comment = "签到时间", type = "datetime", notNull = true)
    private LocalDateTime attendanceTime;

    /** 签到状态：正常，补签，迟到，跨班签到 */
    @Column(comment = "签到状态：正常，补签，迟到，跨班签到", type = "char(20)", defaultValue = "正常")
    private String attendanceStatus;

    /** 学生实际所在班级代码 */
    @Column(comment = "学生实际所在班级代码", type = "varchar(20)")
    private String studentActualClassCode;

    /** 签到时的IP地址 */
    @Column(comment = "签到时的IP地址", type = "varchar(50)")
    private String ipAddress;

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