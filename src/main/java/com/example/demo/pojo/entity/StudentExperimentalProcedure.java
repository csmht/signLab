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
 * 学生步骤答案表
 * 记录学生完成实验步骤的答案信息
 */
@Data
@AutoTable
@Table(value = "student_experimental_procedure", comment = "学生步骤答案表")
@TableName("student_experimental_procedure")
public class StudentExperimentalProcedure {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 实验ID */
    @Column(comment = "实验ID", type = "int", notNull = true)
    private Long experimentId;

    /** 学生用户名(学号) */
    @Column(comment = "学生用户名(学号)", type = "varchar(50)", notNull = true)
    private String studentUsername;

    /** 班级编号 */
    @Column(comment = "班级编号", type = "varchar(20)", notNull = true)
    private String classCode;

    /** 班级实验ID（用于合班上课场景） */
    @Column(comment = "班级实验ID（用于合班上课场景）", type = "bigint")
    private Long classExperimentId;

    /** 实验步骤ID */
    @Column(comment = "实验步骤ID", type = "int", notNull = true)
    private Long experimentalProcedureId;

    /** 实验步骤序号 */
    @Column(comment = "实验步骤序号",type = "int", notNull = true)
    private Integer number;

    /** 答案内容（文本类型） */
    @Column(comment = "答案内容（文本类型）", type = "text")
    private String answer;

    /** 得分 */
    @Column(comment = "得分", type = "decimal(5,2)", defaultValue = "0.00")
    private java.math.BigDecimal score;

    /** 评分状态：0-未评分，1-教师人工评分，2-系统自动评分 */
    @Column(comment = "评分状态", type = "tinyint", defaultValue = "0")
    private Integer isGraded;

    /** 是否已锁定(限时答题提交后不可修改) */
    @Column(comment = "是否已锁定(限时答题提交后不可修改)", type = "bit", defaultValue = "0")
    private Boolean isLocked;

    /** 教师评语 */
    @Column(comment = "教师评语", type = "text")
    private String teacherComment;

    /** 创建时间 */
    @Column(comment = "创建时间", type = "datetime", defaultValue = "CURRENT_TIMESTAMP")
    private LocalDateTime createdTime;

    /** 更新时间 */
    @Column(comment = "更新时间", type = "datetime", defaultValue = "CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedTime;

}
