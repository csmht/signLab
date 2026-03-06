package com.example.demo.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tangzc.autotable.annotation.AutoTable;
import com.tangzc.autotable.annotation.TableIndex;
import com.tangzc.autotable.annotation.enums.IndexTypeEnum;
import com.tangzc.mpe.autotable.annotation.Column;
import com.tangzc.mpe.autotable.annotation.Table;
import lombok.Data;

/**
 * 学生步骤时间延长表
 * 记录教师为学生开通的步骤时间延长
 */
@Data
@AutoTable
@Table(value = "student_procedure_extension", comment = "学生步骤时间延长表")
@TableName("student_procedure_extension")
@TableIndex(name = "uk_student_procedure", fields = {"studentUsername", "experimentalProcedureId"}, type = IndexTypeEnum.UNIQUE)
public class StudentProcedureExtension {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 学生用户名(学号) */
    @Column(comment = "学生用户名", type = "varchar(50)", notNull = true)
    private String studentUsername;

    /** 学生姓名（非数据库字段，查询时填充） */
    @TableField(exist = false)
    private String studentName;

    /** 实验步骤ID */
    @Column(comment = "实验步骤ID", type = "bigint", notNull = true)
    private Long experimentalProcedureId;

    /** 延长时间（分钟） */
    @Column(comment = "延长时间（分钟）", type = "int", notNull = true)
    private Integer extendedMinutes;

    /** 开通教师用户名 */
    @Column(comment = "开通教师用户名", type = "varchar(50)", notNull = true)
    private String teacherUsername;
}
