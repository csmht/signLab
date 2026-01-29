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
 * 班级实验-班级关联表
 * 记录班级实验与班级的多对多关系（支持合班上课）
 */
@Data
@AutoTable
@Table(value = "class_experiment_class_relation", comment = "班级实验-班级关联表 - 支持合班上课")
@TableName("class_experiment_class_relation")
@TableIndex(name = "uk_experiment_class", fields = {"classExperimentId", "classCode"}, type = IndexTypeEnum.UNIQUE)
@TableIndex(name = "idx_class_code", fields = {"classCode"}, type = IndexTypeEnum.NORMAL)
public class ClassExperimentClassRelation {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 班级实验ID
     */
    @Column(comment = "班级实验ID", type = "bigint(20)", notNull = true)
    private Long classExperimentId;

    /**
     * 班级编号
     */
    @Column(comment = "班级编号", type = "varchar(20)", notNull = true)
    private String classCode;

    /**
     * 绑定时间
     */
    @Column(comment = "绑定时间", type = "datetime", defaultValue = "CURRENT_TIMESTAMP")
    private LocalDateTime bindTime;
}
