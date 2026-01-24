package com.example.demo.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tangzc.autotable.annotation.AutoTable;
import com.tangzc.mpe.autotable.annotation.Column;
import com.tangzc.mpe.autotable.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AutoTable
@Table(value = "classes_experiment", comment = "班级实验表 - 记录班级参加的实验")
@TableName("classes_experiment")
public class ClassExperiment {

    @TableId(type = IdType.AUTO)
    private Long id;

    @Column(comment = "班级编号", type = "varchar(20)", notNull = true)
    private String classCode;

    @Column(comment = "课程ID", type = "varchar(20)", notNull = true)
    private String courseId;

    @Column(comment = "实验ID", type = "varchar(20)", notNull = true)
    private String experimentId;

    @Column(comment = "上课时间 例如：8:00-14:00", type = "varchar(20)", notNull = true)
    private String courseTime;

    @Column(comment = "实验开始填写时间", type = "datetime", notNull = true)
    private LocalDateTime startTime;

    @Column(comment = "实验结束填写时间", type = "datetime", notNull = true)
    private LocalDateTime endTime;

    @Column(comment = "实验地点", type = "datetime", notNull = true)
    private LocalDateTime experimentLocation;

}
