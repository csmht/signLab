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
 * 班级实验步骤时间配置表
 * 为每个班级实验的每个步骤提供独立的时间配置
 */
@Data
@AutoTable
@Table(value = "class_experiment_procedure_time",
       comment = "班级实验步骤时间配置表 - 为每个班级实验的步骤提供独立的时间配置")
@TableName("class_experiment_procedure_time")
public class ClassExperimentProcedureTime {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 班级实验ID（关联 classes_experiment 表） */
    @Column(comment = "班级实验ID", type = "bigint", notNull = true)
    private Long classExperimentId;

    /** 实验步骤ID（关联 experiment_procedure 表） */
    @Column(comment = "实验步骤ID", type = "bigint", notNull = true)
    private Long experimentalProcedureId;

    /** 步骤开始时间 */
    @Column(comment = "步骤开始时间", type = "datetime", notNull = true)
    private LocalDateTime startTime;

    /** 步骤结束时间 */
    @Column(comment = "步骤结束时间", type = "datetime", notNull = true)
    private LocalDateTime endTime;

    /** 创建时间 */
    @Column(comment = "创建时间", type = "datetime", defaultValue = "CURRENT_TIMESTAMP")
    private LocalDateTime createdTime;

    /** 更新时间 */
    @Column(comment = "更新时间", type = "datetime",
            defaultValue = "CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedTime;
}
