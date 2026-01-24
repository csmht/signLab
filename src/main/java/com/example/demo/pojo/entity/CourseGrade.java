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
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AutoTable
@Table(value = "course_grades", comment = "课程成绩表")
@TableName("course_grades")
@TableIndex(name = "uk_student_course", fields = {"studentUsername", "courseId"}, type = IndexTypeEnum.UNIQUE)
@TableIndex(name = "idx_course_id", fields = {"courseId"})
@TableIndex(name = "idx_teacher", fields = {"teacherUsername"})
@TableIndex(name = "idx_student", fields = {"studentUsername"})
@TableIndex(name = "idx_grade_time", fields = {"gradeTime"})
@TableIndex(name = "idx_semester", fields = {"semester"})
public class CourseGrade {

    @TableId(type = IdType.AUTO)
    @Column(comment = "成绩ID")
    private Long id;

    @Column(comment = "学生学号", type = "varchar(50)", notNull = true)
    private String studentUsername;

    @Column(comment = "课程ID", type = "varchar(50)", notNull = true)
    private String courseId;

    @Column(comment = "成绩（字母等级或具体分数）", type = "varchar(10)", notNull = true)
    private String grade;

    @Column(comment = "数字成绩（如适用，如90.5分）", type = "decimal(5,2)")
    private BigDecimal gradeNumeric;

    @Column(comment = "成绩类型：字母等级、数字分数、通过/不通过", type = "enum('letter', 'numeric', 'pass_fail')", notNull = true, defaultValue = "'letter'")
    private String gradeType;

    @Column(comment = "满分值", type = "decimal(5,2)", defaultValue = "100.00")
    private BigDecimal maxScore;

    @Column(comment = "打分教师用户名", type = "varchar(50)", notNull = true)
    private String teacherUsername;

    @Column(comment = "教师评语", type = "text")
    private String teacherComment;

    @Column(comment = "成绩打分时间", type = "datetime", defaultValue = "CURRENT_TIMESTAMP")
    private LocalDateTime gradeTime;

    @Column(comment = "学期信息（如：2024-2025春正）", type = "varchar(20)")
    private String semester;

    @Column(comment = "是否已审核", type = "boolean", defaultValue = "false")
    private Boolean isApproved;

    @Column(comment = "审核人用户名", type = "varchar(50)")
    private String approvedBy;

    @Column(comment = "审核时间", type = "datetime")
    private LocalDateTime approvedTime;

    @Column(comment = "创建时间", type = "datetime", defaultValue = "CURRENT_TIMESTAMP")
    private LocalDateTime createdTime;

    @Column(comment = "更新时间", type = "datetime", defaultValue = "CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedTime;
}