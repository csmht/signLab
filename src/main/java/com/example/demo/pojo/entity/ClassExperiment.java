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
 * 班级实验表
 * 记录班级参加的实验（即课次）
 */
@Data
@AutoTable
@Table(value = "classes_experiment", comment = "班级实验表 - 记录班级参加的实验（即课次）")
@TableName("classes_experiment")
public class ClassExperiment {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 班级编号 */
    @Column(comment = "班级编号", type = "varchar(20)", notNull = true)
    private String classCode;

    /** 课程ID */
    @Column(comment = "课程ID", type = "varchar(20)", notNull = true)
    private String courseId;

    /** 实验ID */
    @Column(comment = "实验ID", type = "varchar(20)", notNull = true)
    private String experimentId;

    /** 上课时间 例如：8:00-14:00 */
    @Column(comment = "上课时间 例如：8:00-14:00", type = "varchar(20)", notNull = true)
    private String courseTime;

    /** 实验结束填写时间 */
    @Column(comment = "实验结束填写时间", type = "datetime", notNull = true)
    private LocalDateTime endTime;

    /** 实验地点 */
    @Column(comment = "实验地点", type = "varchar(100)", notNull = true)
    private String experimentLocation;

}
