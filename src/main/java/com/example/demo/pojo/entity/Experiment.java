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
 * 实验表
 * 存储课程实验信息
 */
@Data
@AutoTable
@Table(value = "experiment", comment = "实验表 - 存储课程实验信息")
@TableName("experiment")
public class Experiment {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 课程ID */
    @Column(comment = "课程ID", type = "varchar(20)", notNull = true)
    private String courseId;

    /** 实验名称 */
    @Column(comment = "实验名称", type = "varchar(200)", notNull = true)
    private String experimentName;

    @Column(comment = "分数占比,%",type = "int")
    private Integer percentage;

    /** 创建时间 */
    @Column(comment = "创建时间", type = "datetime", defaultValue = "CURRENT_TIMESTAMP")
    private LocalDateTime createdTime;

    /** 实验结束填写时间 */
    @Column(comment = "实验结束填写时间", type = "datetime", notNull = true)
    private LocalDateTime endTime;

    /** 更新时间 */
    @Column(comment = "更新时间", type = "datetime", defaultValue = "CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedTime;

    /** 是否删除 */
    @Column(comment = "是否删除",type = "bit", notNull = true,defaultValue = "0")
    private Boolean isDeleted;
}
